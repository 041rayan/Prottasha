package com.prottasha.settings;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.prottasha.settings.privacy.EncryptedArchiveService;
import com.prottasha.settings.security.AppLockManager;
import com.prottasha.settings.security.PinCredentialStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;

import org.junit.Test;

public final class PrivacySecurityTest {
    @Test
    public void saltedPinVerifierAcceptsOnlyCorrectPin() throws Exception {
        PinCredentialStore.Verifier verifier = PinCredentialStore.create("4829".toCharArray());
        assertTrue(PinCredentialStore.verify("4829".toCharArray(), verifier));
        assertFalse(PinCredentialStore.verify("0000".toCharArray(), verifier));
    }

    @Test
    public void privacyLockUsesTimeoutAndResetsAcrossAccountSwitch() {
        AppLockManager lock = new AppLockManager(60_000L);
        lock.onBackground(1000L);
        assertFalse(lock.requiresUnlock(30_000L));
        assertTrue(lock.requiresUnlock(61_001L));
        lock.unlock();
        assertFalse(lock.requiresUnlock(70_000L));
        lock.onAccountChanged();
        assertTrue(lock.requiresUnlock(70_000L));
    }

    @Test
    public void encryptedArchiveRoundTripsAndRejectsWrongPasswordOrTamper() throws Exception {
        byte[] payload = "{\"manifest\":1,\"tables\":[\"habits\"]}".getBytes();
        EncryptedArchiveService service = new EncryptedArchiveService(payload);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        service.exportTo(output, "Strong123".toCharArray());
        byte[] archive = output.toByteArray();

        assertFalse(new String(archive, java.nio.charset.StandardCharsets.ISO_8859_1)
                .contains("habits"));
        assertArrayEquals(payload, service.importFrom(
                new ByteArrayInputStream(archive), "Strong123".toCharArray()));
        assertThrows(GeneralSecurityException.class, () -> service.importFrom(
                new ByteArrayInputStream(archive), "Wrong123".toCharArray()));

        archive[archive.length - 1] ^= 1;
        assertThrows(GeneralSecurityException.class, () -> service.importFrom(
                new ByteArrayInputStream(archive), "Strong123".toCharArray()));
    }
}
