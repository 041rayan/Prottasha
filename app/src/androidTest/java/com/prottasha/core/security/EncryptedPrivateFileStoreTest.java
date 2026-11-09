package com.prottasha.core.security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public final class EncryptedPrivateFileStoreTest {

    @Test
    public void storesNormalizedMetadataFreeCiphertextAndReopensIt() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UserDatabaseIdentity identity = uniqueIdentity("alice");
        EncryptedPrivateFileStore store = new EncryptedPrivateFileStore(context);

        try {
            byte[] source = jpegWithTrailingLocationMetadata(2000, 1000);
            StoredPrivateImage saved = store.storeImage(
                    identity,
                    EncryptedPrivateFileStore.Category.HABIT,
                    new ByteArrayInputStream(source));

            assertEquals(1600, saved.width());
            assertEquals(800, saved.height());
            assertEquals("image/jpeg", saved.mimeType());

            File ciphertextFile = store.encryptedFile(
                    identity, EncryptedPrivateFileStore.Category.HABIT, saved.storageId());
            byte[] ciphertext = Files.readAllBytes(ciphertextFile.toPath());
            assertFalse(startsWithJpegMagic(ciphertext));
            assertFalse(new String(ciphertext, StandardCharsets.ISO_8859_1)
                    .contains("GPSLatitude"));

            byte[] normalized = store.readImage(
                    identity, EncryptedPrivateFileStore.Category.HABIT, saved.storageId());
            assertTrue(startsWithJpegMagic(normalized));
            assertFalse(new String(normalized, StandardCharsets.ISO_8859_1)
                    .contains("GPSLatitude"));

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(normalized, 0, normalized.length, bounds);
            assertEquals(1600, bounds.outWidth);
            assertEquals(800, bounds.outHeight);

            List<String> ids = store.listStorageIds(
                    identity, EncryptedPrivateFileStore.Category.HABIT);
            assertEquals(1, ids.size());
            assertEquals(saved.storageId(), ids.get(0));

            EncryptedPrivateFileStore reconstructed = new EncryptedPrivateFileStore(context);
            assertArrayEquals(normalized, reconstructed.readImage(
                    identity, EncryptedPrivateFileStore.Category.HABIT, saved.storageId()));

            reconstructed.delete(
                    identity, EncryptedPrivateFileStore.Category.HABIT, saved.storageId());
            assertTrue(reconstructed.listStorageIds(
                    identity, EncryptedPrivateFileStore.Category.HABIT).isEmpty());
        } finally {
            store.destroy(identity);
        }
    }

    @Test
    public void rejectsOtherUidAndTamperedCiphertext() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UserDatabaseIdentity alice = uniqueIdentity("alice");
        UserDatabaseIdentity bob = uniqueIdentity("bob");
        EncryptedPrivateFileStore store = new EncryptedPrivateFileStore(context);

        try {
            StoredPrivateImage saved = store.storeImage(
                    alice,
                    EncryptedPrivateFileStore.Category.JOURNAL,
                    new ByteArrayInputStream(jpegWithTrailingLocationMetadata(40, 20)));

            assertThrows(FileNotFoundException.class, () -> store.readImage(
                    bob, EncryptedPrivateFileStore.Category.JOURNAL, saved.storageId()));

            File ciphertextFile = store.encryptedFile(
                    alice, EncryptedPrivateFileStore.Category.JOURNAL, saved.storageId());
            try (RandomAccessFile file = new RandomAccessFile(ciphertextFile, "rw")) {
                long last = file.length() - 1;
                file.seek(last);
                int value = file.read();
                file.seek(last);
                file.write(value ^ 0x01);
            }

            assertThrows(GeneralSecurityException.class, () -> store.readImage(
                    alice, EncryptedPrivateFileStore.Category.JOURNAL, saved.storageId()));
        } finally {
            store.destroy(alice);
            store.destroy(bob);
        }
    }

    @Test
    public void missingKeyEnvelopeFailsClosedWithoutReplacingData() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UserDatabaseIdentity identity = uniqueIdentity("key-loss");
        EncryptedPrivateFileStore store = new EncryptedPrivateFileStore(context);

        try {
            StoredPrivateImage saved = store.storeImage(
                    identity,
                    EncryptedPrivateFileStore.Category.PROFILE,
                    new ByteArrayInputStream(jpegWithTrailingLocationMetadata(40, 20)));
            File ciphertextFile = store.encryptedFile(
                    identity, EncryptedPrivateFileStore.Category.PROFILE, saved.storageId());
            long originalLength = ciphertextFile.length();

            assertTrue(store.keyEnvelopeFile(identity).delete());
            EncryptedPrivateFileStore reconstructed = new EncryptedPrivateFileStore(context);

            assertThrows(KeystoreKeyManager.KeyUnavailableException.class,
                    () -> reconstructed.readImage(
                            identity, EncryptedPrivateFileStore.Category.PROFILE,
                            saved.storageId()));
            assertThrows(KeystoreKeyManager.KeyUnavailableException.class,
                    () -> reconstructed.storeImage(
                            identity,
                            EncryptedPrivateFileStore.Category.PROFILE,
                            new ByteArrayInputStream(jpegWithTrailingLocationMetadata(10, 10))));
            assertTrue(ciphertextFile.exists());
            assertEquals(originalLength, ciphertextFile.length());
        } finally {
            store.destroy(identity);
        }
    }

    @Test
    public void rejectsNonImageInput() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UserDatabaseIdentity identity = uniqueIdentity("invalid");
        EncryptedPrivateFileStore store = new EncryptedPrivateFileStore(context);

        try {
            assertThrows(IllegalArgumentException.class, () -> store.storeImage(
                    identity,
                    EncryptedPrivateFileStore.Category.SUCCESS_MEMORY,
                    new ByteArrayInputStream("not an image".getBytes(StandardCharsets.UTF_8))));
        } finally {
            store.destroy(identity);
        }
    }

    private static UserDatabaseIdentity uniqueIdentity(String prefix) {
        return UserDatabaseIdentity.fromUid(prefix + "-" + UUID.randomUUID());
    }

    private static byte[] jpegWithTrailingLocationMetadata(int width, int height) throws Exception {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFF34D399);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output));
        bitmap.recycle();
        output.write("GPSLatitude=23.8103;GPSLongitude=90.4125".getBytes(StandardCharsets.US_ASCII));
        return output.toByteArray();
    }

    private static boolean startsWithJpegMagic(byte[] bytes) {
        return bytes.length >= 2 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8;
    }
}
