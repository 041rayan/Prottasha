package com.prottasha.support.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.List;

public final class SupportDirectoryRepositoryTest {
    @Test
    public void bundledDirectoryShowsSourceDateHoursAndDialableNumber() {
        List<DirectoryEntryEntity> entries = new SupportDirectoryRepository().entries();
        assertTrue(entries.size() >= 3);
        DirectoryEntryEntity entry = entries.get(0);
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
        assertFalse(SupportDirectoryRepository.isSafeHttps(""));
        assertFalse(SupportDirectoryRepository.isSafeHttps(null));
    }

    @Test
    public void parsesValidDirectoryJson() throws Exception {
        String validJson = "{\n"
                + "  \"version\": 1,\n"
                + "  \"updatedAt\": \"2026-11-10\",\n"
                + "  \"entries\": [\n"
                + "    {\n"
                + "      \"name\": \"Test Crisis Line\",\n"
                + "      \"phone\": \"01700000000\",\n"
                + "      \"hours\": \"24 hours\",\n"
                + "      \"source\": \"Test Ministry\",\n"
                + "      \"verifiedDate\": \"2026-11-10\",\n"
                + "      \"status\": \"VERIFIED\",\n"
                + "      \"website\": \"https://test.gov.bd/\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        List<DirectoryEntryEntity> result = SupportDirectoryRepository.parseDirectoryJson(validJson);
        assertEquals(1, result.size());
        assertEquals("Test Crisis Line", result.get(0).name);
        assertEquals("https://test.gov.bd/", result.get(0).website);
    }

    @Test
    public void rejectsDirectoryWithInvalidFieldsOrUnsafeUrls() {
        String invalidJson = "{\n"
                + "  \"version\": 1,\n"
                + "  \"entries\": [\n"
                + "    {\n"
                + "      \"name\": \"Invalid Line\",\n"
                + "      \"phone\": \"abc\",\n" // invalid phone
                + "      \"hours\": \"24 hours\",\n"
                + "      \"source\": \"Test\",\n"
                + "      \"verifiedDate\": \"2026-11-10\",\n"
                + "      \"status\": \"VERIFIED\",\n"
                + "      \"website\": \"http://insecure.test\"\n" // insecure http
                + "    }\n"
                + "  ]\n"
                + "}";
        try {
            SupportDirectoryRepository.parseDirectoryJson(invalidJson);
            fail("Expected IllegalArgumentException for insecure url or invalid phone");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
