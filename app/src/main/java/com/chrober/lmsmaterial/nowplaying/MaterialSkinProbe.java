package com.chrober.lmsmaterial.nowplaying;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Read-only availability probe for Material Skin's /material/np-only page.
 * Material Skin must be installed on the server, not as a separate Android app.
 */
final class MaterialSkinProbe {
    enum Status {
        AVAILABLE, MISSING, UNEXPECTED_PAGE, UNAUTHORIZED, HTTP_ERROR, CONNECTION_ERROR
    }

    static final class Result {
        final Status status;
        final int httpCode;
        Result(Status status, int httpCode) {
            this.status = status;
            this.httpCode = httpCode;
        }
    }

    interface Callback { void completed(Result result); }

    static void check(String nowPlayingUrl, Callback callback) {
        new Thread(() -> {
            Result result = request(nowPlayingUrl);
            new Handler(Looper.getMainLooper()).post(() -> callback.completed(result));
        }, "lyrion-material-skin-probe").start();
    }

    private static Result request(String url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3500);
            connection.setReadTimeout(4500);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(false);
            int status = connection.getResponseCode();
            if (status != 200) return classify(status, "");

            // A generic Lyrion 404/login page can also come back with HTTP 200.
            // Verify recognizable markers from Material Skin's np-only template.
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            try (InputStream stream = connection.getInputStream()) {
                byte[] chunk = new byte[4096];
                int n;
                while (content.size() < 64 * 1024
                        && (n = stream.read(chunk, 0, Math.min(chunk.length,
                                64 * 1024 - content.size()))) != -1) {
                    content.write(chunk, 0, n);
                }
            }
            return classify(status, new String(content.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return new Result(Status.CONNECTION_ERROR, 0);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /** Pure response classifier, covered by local JVM tests. */
    static Result classify(int httpStatus, String body) {
        if (httpStatus == 404 || httpStatus == 410)
            return new Result(Status.MISSING, httpStatus);
        if (httpStatus == 401 || httpStatus == 403)
            return new Result(Status.UNAUTHORIZED, httpStatus);
        if (httpStatus != 200)
            return new Result(Status.HTTP_ERROR, httpStatus);
        if (body != null && (body.contains("msk--np-only")
                || (body.contains("npAutoClose=false") && body.contains("player"))))
            return new Result(Status.AVAILABLE, httpStatus);
        return new Result(Status.UNEXPECTED_PAGE, httpStatus);
    }

    private MaterialSkinProbe() { }
}
