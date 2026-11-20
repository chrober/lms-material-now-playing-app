package com.chrober.lmsmaterial.nowplaying;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class LocalLyrionHttpIntegrationTest {
    private HttpServer server;
    private String baseUrl;

    @Before public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(null);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @After public void stopServer() {
        if (server != null) server.stop(0);
    }

    private static void respond(HttpExchange request, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        request.sendResponseHeaders(status, bytes.length);
        try (java.io.OutputStream out = request.getResponseBody()) { out.write(bytes); }
        request.close();
    }

    @Test public void fetchPlayersSendsExpectedReadOnlyJsonRpcRequest() {
        AtomicInteger seen = new AtomicInteger();
        server.createContext("/jsonrpc.js", request -> {
            assertEquals("POST", request.getRequestMethod());
            String posted = new String(request.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(posted.contains("\"players\""));
            assertTrue(posted.contains("\"100\""));
            seen.incrementAndGet();
            respond(request, 200,
                    "{\"result\":{\"players_loop\":[{\"playerid\":\"ab\",\"name\":\"Kitchen\"}]}}");
        });
        server.start();
        LyrionPlayers.FetchResult fetched = LyrionPlayers.fetchBlocking(baseUrl);
        assertNull(fetched.error);
        assertEquals(1, seen.get());
        assertEquals("Kitchen", fetched.players.get(0).name);
    }

    @Test public void playerFetchReportsHttpErrorsWithoutInventingPlayers() {
        server.createContext("/jsonrpc.js", request -> respond(request, 503, "Unavailable"));
        server.start();
        LyrionPlayers.FetchResult result = LyrionPlayers.fetchBlocking(baseUrl);
        assertTrue(result.players.isEmpty());
        assertEquals("HTTP 503", result.error);
    }

    @Test public void materialProbeDistinguishesExpectedPageFromLoginAndUnavailable() {
        server.createContext("/ok", request -> respond(request, 200,
                "<script>location.href='/material/?npAutoClose=false&player=Kitchen';</script>"));
        server.createContext("/login", request -> respond(request, 200, "<html>Login</html>"));
        server.createContext("/missing", request -> respond(request, 404, "Not found"));
        server.start();
        assertEquals(MaterialSkinProbe.Status.AVAILABLE,
                MaterialSkinProbe.request(baseUrl + "/ok").status);
        assertEquals(MaterialSkinProbe.Status.UNEXPECTED_PAGE,
                MaterialSkinProbe.request(baseUrl + "/login").status);
        assertEquals(MaterialSkinProbe.Status.MISSING,
                MaterialSkinProbe.request(baseUrl + "/missing").status);
    }

    @Test public void catalogFetchReadsThemesAndOptionalDefaultsViaJsonRpc() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/jsonrpc.js", request -> {
            String posted = new String(request.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.incrementAndGet();
            if (posted.contains("material-skin")) {
                respond(request, 200, "{\"result\":{\"themes\":[{\"key\":\"dark\",\"label\":\"Dark\"}],"
                        + "\"colors\":[{\"key\":\"lyrion\",\"label\":\"Lyrion\",\"color\":\"#112233\"}]}}");
            } else if (posted.contains("plugin.material-skin:defaults")) {
                JSONObject payload = new JSONObject().put("result",
                        new JSONObject().put("_p2", "{\"theme\":\"dark\"}"));
                respond(request, 200, payload.toString());
            } else {
                respond(request, 400, "Unknown RPC");
            }
        });
        server.start();
        MaterialSkinCatalog.Result result = MaterialSkinCatalog.fetchBlocking(baseUrl);
        assertEquals(2, requests.get());
        assertEquals(1, result.themes.size());
        assertEquals(1, result.colors.size());
        assertEquals("dark", result.defaults.getString("theme"));
    }

    @Test public void catalogGracefullyFallsBackOnUnexpectedServerResponses() {
        server.createContext("/jsonrpc.js", request -> respond(request, 200, "{}"));
        server.start();
        MaterialSkinCatalog.Result result = MaterialSkinCatalog.fetchBlocking(baseUrl);
        assertTrue(result.themes.isEmpty());
        assertTrue(result.colors.isEmpty());
        assertNull(result.defaults);
    }
}
