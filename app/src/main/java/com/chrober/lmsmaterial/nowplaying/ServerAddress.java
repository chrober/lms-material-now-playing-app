package com.chrober.lmsmaterial.nowplaying;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Strictly validates and normalizes an LMS server origin, not an arbitrary web URL. */
final class ServerAddress {
    static String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String input = raw.trim();
        if (!input.contains("://")) input = "http://" + input;
        try {
            URI uri = new URI(input);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) return null;
            if (uri.getHost() == null || uri.getHost().isEmpty() || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty()
                        && !uri.getRawPath().equals("/"))) return null;
            int port = uri.getPort();
            if (port == 0 || port > 65535 || port < -1) return null;
            if (port == -1 && scheme.equals("http")) port = 9000;
            return new URI(scheme, null, uri.getHost(), port, null, null, null).toASCIIString();
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return null;
        }
    }
    private ServerAddress() { }
}
