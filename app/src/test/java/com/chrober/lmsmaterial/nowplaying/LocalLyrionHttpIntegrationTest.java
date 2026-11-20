package com.chrober.lmsmaterial.nowplaying;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** End-to-end localhost HTTP tests without network or Android runtime dependencies. */
public class LocalLyrionHttpIntegrationTest {
    private ServerSocket listener;
    private Thread worker;
    private String baseUrl;
    private final AtomicReference<Throwable> serverFailure = new AtomicReference<>();

    private interface Route {
        Reply handle(String method, String path, String body) throws Exception;
    }

    private static final class Reply {
        final int status;
        final String body;
        Reply(int status, String body) { this.status = status; this.body = body; }
    }

    @Before public void startServer() throws Exception {
        listener = new ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"));
        baseUrl = "http://127.0.0.1:" + listener.getLocalPort();
    }

    private void serve(Route handler) {
        worker = new Thread(() -> {
            while (!listener.isClosed()) {
                try (Socket client = listener.accept()) {
                    client.setSoTimeout(2500);
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                    String requestLine = input.readLine();
                    if (requestLine == null) continue;
                    String[] request = requestLine.split(" ");
                    int bodyLength = 0;
                    String header;
                    while ((header = input.readLine()) != null && !header.isEmpty()) {
                        if (header.toLowerCase(java.util.Locale.ROOT).startsWith("content-length:"))
                            bodyLength = Integer.parseInt(header.split(":", 2)[1].trim());
                    }
                    if (bodyLength > 100_000) throw new IllegalArgumentException("Oversized test request");
                    char[] chars = new char[bodyLength];
                    int count = 0;
                    while (count < chars.length) {
                        int n = input.read(chars, count, chars.length - count);
                        if (n < 0) throw new IllegalStateException("Premature request EOF");
                        count += n;
                    }
                    Reply reply = handler.handle(request[0], request[1], new String(chars));
                    byte[] bytes = reply.body.getBytes(StandardCharsets.UTF_8);
                    String status = reply.status == 200 ? "OK" : "Error";
                    String headers = "HTTP/1.1 " + reply.status + " " + status + "\r\n"
                            + "Content-Type: application/json\r\n"
                            + "Content-Length: " + bytes.length + "\r\n"
                            + "Connection: close\r\n\r\n";
                    OutputStream out = client.getOutputStream();
                    out.write(headers.getBytes(StandardCharsets.US_ASCII));
                    out.write(bytes);
                    out.flush();
                } catch (java.net.SocketException closed) {
                    if (!listener.isClosed()) serverFailure.compareAndSet(null, closed);
                } catch (Throwable failure) {
                    serverFailure.compareAndSet(null, failure);
                    break;
                }
            }
        }, "test-lyrion-http-server");
        worker.setDaemon(true);
        worker.start();
    }

    private void assertServerOkay() {
        if (serverFailure.get() != null)
            throw new AssertionError("Mock LMS server failed", serverFailure.get());
    }

    @After public void stopServer() throws Exception {
        if (listener != null) listener.close();
        if (worker != null) worker.join(3000);
        assertServerOkay();
    }

    @Test public void fetchPlayersSendsExpectedReadOnlyJsonRpcRequest() {
        AtomicInteger seen = new AtomicInteger();
        serve((method, path, body) -> {
            assertEquals("POST", method);
            assertEquals("/jsonrpc.js", path);
            assertTrue(body.contains("\"players\""));
            assertTrue(body.contains("\"100\""));
            seen.incrementAndGet();
            return new Reply(200,
                    "{\"result\":{\"players_loop\":[{\"playerid\":\"ab\",\"name\":\"Kitchen\"}]}}");
        });
        LyrionPlayers.FetchResult fetched = LyrionPlayers.fetchBlocking(baseUrl);
        assertServerOkay();
        assertNull(fetched.error);
        assertEquals(1, seen.get());
        assertEquals("Kitchen", fetched.players.get(0).name);
    }

    @Test public void playerFetchReportsHttpErrorsWithoutInventingPlayers() {
        serve((method, path, body) -> new Reply(503, "Unavailable"));
        LyrionPlayers.FetchResult result = LyrionPlayers.fetchBlocking(baseUrl);
        assertServerOkay();
        assertTrue(result.players.isEmpty());
        assertEquals("HTTP 503", result.error);
    }

    @Test public void materialProbeDistinguishesExpectedPageFromLoginAndUnavailable() {
        serve((method, path, body) -> {
            if ("/ok".equals(path))
                return new Reply(200,
                        "<script>location.href='/material/?npAutoClose=false&player=Kitchen';</script>");
            if ("/login".equals(path)) return new Reply(200, "<html>Login</html>");
            return new Reply(404, "Not found");
        });
        assertEquals(MaterialSkinProbe.Status.AVAILABLE,
                MaterialSkinProbe.request(baseUrl + "/ok").status);
        assertEquals(MaterialSkinProbe.Status.UNEXPECTED_PAGE,
                MaterialSkinProbe.request(baseUrl + "/login").status);
        assertEquals(MaterialSkinProbe.Status.MISSING,
                MaterialSkinProbe.request(baseUrl + "/missing").status);
        assertServerOkay();
    }

    @Test public void catalogFetchReadsThemesAndOptionalDefaultsViaJsonRpc() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        serve((method, path, body) -> {
            assertEquals("POST", method);
            assertEquals("/jsonrpc.js", path);
            requests.incrementAndGet();
            if (body.contains("plugin.material-skin:defaults")) {
                JSONObject payload = new JSONObject().put("result",
                        new JSONObject().put("_p2", "{\"theme\":\"dark\"}"));
                return new Reply(200, payload.toString());
            }
            if (body.contains("material-skin")) {
                return new Reply(200, "{\"result\":{\"themes\":[{\"key\":\"dark\",\"label\":\"Dark\"}],"
                        + "\"colors\":[{\"key\":\"lyrion\",\"label\":\"Lyrion\",\"color\":\"#112233\"}]}}");
            }
            return new Reply(400, "Unknown RPC");
        });
        MaterialSkinCatalog.Result result = MaterialSkinCatalog.fetchBlocking(baseUrl);
        assertServerOkay();
        assertEquals(2, requests.get());
        assertEquals(1, result.themes.size());
        assertEquals(1, result.colors.size());
        assertEquals("dark", result.defaults.getString("theme"));
    }

    @Test public void catalogGracefullyFallsBackOnUnexpectedServerResponses() {
        serve((method, path, body) -> new Reply(200, "{}"));
        MaterialSkinCatalog.Result result = MaterialSkinCatalog.fetchBlocking(baseUrl);
        assertServerOkay();
        assertTrue(result.themes.isEmpty());
        assertTrue(result.colors.isEmpty());
        assertNull(result.defaults);
    }
}
