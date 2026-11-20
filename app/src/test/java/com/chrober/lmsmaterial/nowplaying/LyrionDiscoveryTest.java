package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

public class LyrionDiscoveryTest {
    private final InetAddress sender = address();

    private static InetAddress address() {
        try { return InetAddress.getByName("192.0.2.42"); }
        catch (Exception ex) { throw new AssertionError(ex); }
    }

    private static byte[] reply(String name, String port) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write('E');
        writeField(bytes, "NAME", name);
        writeField(bytes, "JSON", port);
        return bytes.toByteArray();
    }

    private static void writeField(ByteArrayOutputStream out, String key, String value)
            throws Exception {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        out.write(key.getBytes(StandardCharsets.US_ASCII));
        out.write(data.length);
        out.write(data);
    }

    @Test public void decodesServerNamePortAndSender() throws Exception {
        byte[] bytes = reply("Living Room", "9020");
        LyrionDiscovery.Server result = LyrionDiscovery.parseReply(bytes, bytes.length, sender);
        assertNotNull(result);
        assertEquals("Living Room", result.name);
        assertEquals("http://192.0.2.42:9020", result.url());
    }

    @Test public void fallsBackToDefaultPortOnUnsafeValues() throws Exception {
        for (String value : new String[]{"invalid", "0", "65536", "-1"}) {
            byte[] bytes = reply("Room", value);
            assertEquals(9000, LyrionDiscovery.parseReply(bytes, bytes.length, sender).port);
        }
    }

    @Test public void rejectsBadHeadersAndLengths() throws Exception {
        assertNull(LyrionDiscovery.parseReply(new byte[]{'X', 1}, 2, sender));
        assertNull(LyrionDiscovery.parseReply(new byte[]{'E'}, 1, sender));
        assertNull(LyrionDiscovery.parseReply(new byte[]{'E', 0}, 5, sender));
        assertNull(LyrionDiscovery.parseReply(null, 3, sender));
    }

    @Test public void truncatedTlvCannotReadPastDatagram() throws Exception {
        byte[] truncated = {'E', 'N', 'A', 'M', 'E', 100, 'x'};
        LyrionDiscovery.Server server = LyrionDiscovery.parseReply(truncated, truncated.length, sender);
        assertNotNull(server);
        assertEquals("", server.name);
        assertEquals(9000, server.port);
    }
}
