package com.prottasha.support.data;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public final class SupportDirectoryRepository {
    public List<DirectoryEntryEntity> entries() {
        return Collections.singletonList(new DirectoryEntryEntity(
                "Bangladesh National Emergency Service",
                "999",
                "24 hours",
                "Bangladesh Police — https://www.police.gov.bd/",
                "2026-11-10",
                "VERIFIED"));
    }

    public static boolean isSafeHttps(String value) {
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }
}
