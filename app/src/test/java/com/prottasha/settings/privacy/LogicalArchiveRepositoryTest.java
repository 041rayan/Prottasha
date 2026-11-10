package com.prottasha.settings.privacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;

public final class LogicalArchiveRepositoryTest {

    @Test
    public void validArchiveJsonPassesValidation() throws Exception {
        JSONObject archive = new JSONObject();
        archive.put("format", LogicalArchiveRepository.ARCHIVE_FORMAT);
        archive.put("schemaVersion", 12);
        archive.put("exportedAt", System.currentTimeMillis());
        archive.put("uidHash", "hash123");

        JSONObject tables = new JSONObject();
        tables.put("habits", new JSONArray());
        tables.put("identities", new JSONArray());
        archive.put("tables", tables);
        archive.put("files", new JSONArray());

        LogicalArchiveRepository.validateArchiveJson(archive);
    }

    @Test
    public void invalidFormatOrMissingTablesThrowsException() throws Exception {
        JSONObject archive = new JSONObject();
        archive.put("format", "WRONG_FORMAT");
        archive.put("schemaVersion", 12);

        try {
            LogicalArchiveRepository.validateArchiveJson(archive);
            fail("Expected IllegalArgumentException for wrong format");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test
    public void unsupportedFutureSchemaVersionThrowsException() throws Exception {
        JSONObject archive = new JSONObject();
        archive.put("format", LogicalArchiveRepository.ARCHIVE_FORMAT);
        archive.put("schemaVersion", 999); // Future unsupported version
        archive.put("tables", new JSONObject());

        try {
            LogicalArchiveRepository.validateArchiveJson(archive);
            fail("Expected IllegalArgumentException for unsupported future schema version");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("schemaVersion"));
        }
    }

    @Test
    public void encryptedStreamRoundTrip() throws Exception {
        JSONObject archive = new JSONObject();
        archive.put("format", LogicalArchiveRepository.ARCHIVE_FORMAT);
        archive.put("schemaVersion", 12);
        archive.put("exportedAt", 1000L);
        archive.put("tables", new JSONObject());
        archive.put("files", new JSONArray());

        byte[] payload = archive.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EncryptedArchiveService archiveService = new EncryptedArchiveService(payload);
        archiveService.exportTo(out, "SafePassword123".toCharArray());

        byte[] cipherBytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(cipherBytes);
        byte[] decrypted = archiveService.importFrom(in, "SafePassword123".toCharArray());

        JSONObject restored = new JSONObject(new String(decrypted, java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(LogicalArchiveRepository.ARCHIVE_FORMAT, restored.getString("format"));
        assertEquals(12, restored.getInt("schemaVersion"));
    }
}
