package com.prottasha.support.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SupportDirectoryRepositoryTest {
    @Test
    public void bundledDirectoryShowsSourceDateHoursAndDialableNumber() {
        DirectoryEntryEntity entry = new SupportDirectoryRepository().entries().get(0);
        assertEquals("VERIFIED", entry.status);
        assertTrue(entry.phone.matches("[0-9+]+"));
        assertFalse(entry.source.isEmpty());
        assertFalse(entry.verifiedDate.isEmpty());
        assertFalse(entry.hours.isEmpty());
    }

    @Test
    public void unsafeUrlsAreRejected() {
        assertTrue(SupportDirectoryRepository.isSafeHttps("https://www.police.gov.bd/"));
        assertFalse(SupportDirectoryRepository.isSafeHttps("http://example.com"));
        assertFalse(SupportDirectoryRepository.isSafeHttps("javascript:alert(1)"));
    }
}
