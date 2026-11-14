package com.chrober.lmsmaterial.nowplaying;

import org.junit.Test;
import static org.junit.Assert.*;

public class ServerAddressTest {
    @Test public void normalizesLyrionDefaultPort() {
        assertEquals("http://192.168.1.30:9000", ServerAddress.normalize("192.168.1.30"));
        assertEquals("http://lyrion.local:9000", ServerAddress.normalize("http://lyrion.local/"));
        assertEquals("https://music.example.org", ServerAddress.normalize("https://music.example.org"));
    }

    @Test public void keepsExplicitPort() {
        assertEquals("http://nas.local:9010", ServerAddress.normalize("nas.local:9010"));
    }

    @Test public void rejectsInvalidAndUnsafeOrigins() {
        assertNull(ServerAddress.normalize(""));
        assertNull(ServerAddress.normalize("ftp://server:9000"));
        assertNull(ServerAddress.normalize("http://name:password@server:9000"));
        assertNull(ServerAddress.normalize("http://server/path"));
        assertNull(ServerAddress.normalize("http://server:9000?x=y"));
        assertNull(ServerAddress.normalize("http://server:70000"));
        assertNull(ServerAddress.normalize("http://server:0"));
    }
}
