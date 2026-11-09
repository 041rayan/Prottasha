package com.prottasha.usage.browser;

import java.net.IDN;
import java.net.URI;
import java.util.Locale;

public final class DomainNormalizer {
    private DomainNormalizer() { }

    public static String normalize(String value) {
        if (value == null || value.indexOf(' ') >= 0) return null;
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return null;
            }
            String host = uri.getHost();
            if (host == null) {
                host = uri.getAuthority();
                if (host != null) {
                    int userInfo = host.lastIndexOf('@');
                    if (userInfo >= 0) host = host.substring(userInfo + 1);
                    int port = host.lastIndexOf(':');
                    if (port > 0) host = host.substring(0, port);
                }
            }
            if (host == null || host.equalsIgnoreCase("localhost") || !host.contains(".")) {
                return null;
            }
            return IDN.toASCII(host.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }
}
