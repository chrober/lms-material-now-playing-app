package com.chrober.lmsmaterial.nowplaying;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only Material Skin catalog and optional server UI defaults.
 * Uses the same JSON-RPC commands as Material Skin's original settings dialog.
 */
final class MaterialSkinCatalog {
    static final class Result {
        final List<MaterialTheme.Option> themes;
        final List<MaterialTheme.ColorOption> colors;
        final JSONObject defaults;
        Result(List<MaterialTheme.Option> themes, List<MaterialTheme.ColorOption> colors,
                JSONObject defaults) {
            this.themes = themes; this.colors = colors; this.defaults = defaults;
        }
    }
    interface Callback { void completed(Result result); }

    static void fetch(String server, Callback callback) {
        new Thread(() -> {
            List<MaterialTheme.Option> themes = new ArrayList<>();
            List<MaterialTheme.ColorOption> colors = new ArrayList<>();
            JSONObject defaults = null;
            try {
                JSONObject result = request(server, new JSONArray()
                        .put("material-skin").put("themes").put("platform:android"));
                JSONArray available = result.optJSONArray("themes");
                if (available != null) for (int i=0; i<available.length(); i++) {
                    JSONObject theme = available.optJSONObject(i);
                    if (theme == null) continue;
                    String key = theme.optString("key", "");
                    if (MaterialTheme.safeKey(key))
                        themes.add(new MaterialTheme.Option(key, theme.optString("label", key).replace('-', ' ')));
                }
                JSONArray extraColors = result.optJSONArray("colors");
                if (extraColors != null) for (int i=0; i<extraColors.length(); i++) {
                    JSONObject color = extraColors.optJSONObject(i);
                    if (color == null) continue;
                    String key = color.optString("key", "");
                    String value = color.optString("color", "");
                    if (MaterialTheme.safeKey(key) && MaterialTheme.parseHex(value, 0) != 0) {
                        colors.add(new MaterialTheme.ColorOption(key, color.optString("label", key),
                                value, value));
                    }
                }
            } catch (Exception ignored) { // Older server/offline: built-ins remain usable.
            }
            try {
                JSONObject result = request(server, new JSONArray()
                        .put("pref").put("plugin.material-skin:defaults").put("?"));
                String raw = result.optString("_p2", "");
                if (raw.startsWith("{")) defaults = new JSONObject(raw);
            } catch (Exception ignored) { // Server UI defaults are optional.
            }
            Result result = new Result(themes, colors, defaults);
            new Handler(Looper.getMainLooper()).post(() -> callback.completed(result));
        }, "lyrion-material-theme-catalog").start();
    }

    private static JSONObject request(String server, JSONArray command) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(server + "/jsonrpc.js").openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3500);
            connection.setReadTimeout(4500);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            JSONObject payload = new JSONObject();
            payload.put("id", 1);
            payload.put("method", "slim.request");
            payload.put("params", new JSONArray().put("").put(command));
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }
            if (connection.getResponseCode() != 200) throw new java.io.IOException("HTTP error");
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            try (InputStream in = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    data.write(buffer, 0, count);
                    if (data.size() > 128 * 1024) throw new java.io.IOException("Response too large");
                }
            }
            JSONObject root = new JSONObject(data.toString("UTF-8"));
            JSONObject result = root.optJSONObject("result");
            if (result == null) throw new java.io.IOException("Missing JSON-RPC result");
            return result;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
    private MaterialSkinCatalog() { }
}
