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

/** Read-only Lyrion JSON-RPC player enumeration. */
final class LyrionPlayers {
    interface Callback { void completed(List<Player> players, String error); }

    static final class Player {
        final String id;
        final String name;
        Player(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name.isEmpty() ? id : name; }
    }

    static void fetch(String server, Callback callback) {
        new Thread(() -> {
            List<Player> players = new ArrayList<>();
            String error = null;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(server + "/jsonrpc.js").openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(4500);
                conn.setReadTimeout(5500);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] payload = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"\",[\"players\",\"0\",\"100\"]]}"
                        .getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = conn.getOutputStream()) { out.write(payload); }
                if (conn.getResponseCode() != 200)
                    throw new java.io.IOException("HTTP " + conn.getResponseCode());
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                try (InputStream in = conn.getInputStream()) {
                    byte[] chunk = new byte[4096];
                    int n;
                    while ((n = in.read(chunk)) != -1) {
                        buffer.write(chunk, 0, n);
                        if (buffer.size() > 1024 * 1024)
                            throw new java.io.IOException("Server response too large");
                    }
                }
                JSONObject root = new JSONObject(buffer.toString("UTF-8"));
                if (root.has("error") && !root.isNull("error"))
                    throw new java.io.IOException(root.get("error").toString());
                JSONObject result = root.optJSONObject("result");
                if (result == null) throw new java.io.IOException("Not a Lyrion JSON-RPC response");
                JSONArray list = result.optJSONArray("players_loop");
                if (list != null) {
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject player = list.optJSONObject(i);
                        if (player == null) continue;
                        String id = player.optString("playerid", "");
                        if (!id.isEmpty())
                            players.add(new Player(id, player.optString("name", id)));
                    }
                }
            } catch (Exception ex) {
                error = ex.getMessage() == null ? "Connection failed" : ex.getMessage();
            } finally {
                if (conn != null) conn.disconnect();
            }
            final String finalError = error;
            new Handler(Looper.getMainLooper()).post(() -> callback.completed(players, finalError));
        }, "lyrion-player-lookup").start();
    }

    private LyrionPlayers() { }
}
