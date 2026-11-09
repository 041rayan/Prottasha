package com.prottasha.core.security;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class EncryptedPrivateFileStore {

    public enum Category {
        PROFILE,
        HABIT,
        JOURNAL,
        SUCCESS_MEMORY
    }

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int FILE_MAGIC = 0x50544631; // PTF1
    private static final int FILE_VERSION = 1;
    private static final int MAX_INPUT_BYTES = 20 * 1024 * 1024;
    private static final int MAX_OUTPUT_BYTES = 8 * 1024 * 1024;
    private static final int MAX_DIMENSION = 1600;
    private static final int MAX_DECODE_DIMENSION = 3200;
    private static final int IV_BYTES = 12;
    private static final Pattern STORAGE_ID = Pattern.compile("[0-9a-f-]{36}");

    private final Context context;
    private final KeystoreKeyManager keyManager;
    private final SecureRandom random = new SecureRandom();

    public EncryptedPrivateFileStore(Context context) {
        this.context = context.getApplicationContext();
        this.keyManager = new KeystoreKeyManager();
    }

    public StoredPrivateImage storeImage(
            UserDatabaseIdentity identity,
            Category category,
            InputStream source) throws IOException, GeneralSecurityException {
        if (identity == null || category == null || source == null) {
            throw new NullPointerException("identity, category, and source are required");
        }

        NormalizedImage normalized = normalize(source);
        byte[] secret = null;
        try {
            secret = keyManager.getOrCreateSecret(
                    identity.privateFileKeyAlias(),
                    keyEnvelopeFile(identity),
                    protectedDataExists(identity));
            String storageId = UUID.randomUUID().toString();
            writeEncrypted(encryptedFile(identity, category, storageId), normalized.bytes, secret);
            return new StoredPrivateImage(
                    storageId, normalized.width, normalized.height, normalized.bytes.length);
        } finally {
            normalized.clear();
            clear(secret);
        }
    }

    public byte[] readImage(
            UserDatabaseIdentity identity,
            Category category,
            String storageId) throws IOException, GeneralSecurityException {
        File file = encryptedFile(identity, category, storageId);
        if (!file.isFile()) {
            throw new FileNotFoundException("Encrypted private image is unavailable");
        }

        byte[] secret = null;
        try {
            secret = keyManager.getOrCreateSecret(
                    identity.privateFileKeyAlias(), keyEnvelopeFile(identity), true);
            return readEncrypted(file, secret);
        } finally {
            clear(secret);
        }
    }

    public List<String> listStorageIds(UserDatabaseIdentity identity, Category category) {
        File directory = categoryDirectory(identity, category);
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".ptimg"));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<>(files.length);
        for (File file : files) {
            ids.add(file.getName().substring(0, file.getName().length() - ".ptimg".length()));
        }
        Collections.sort(ids);
        return Collections.unmodifiableList(ids);
    }

    public void delete(UserDatabaseIdentity identity, Category category, String storageId)
            throws IOException {
        File file = encryptedFile(identity, category, storageId);
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to delete encrypted private image");
        }
    }

    public void destroy(UserDatabaseIdentity identity) throws IOException, GeneralSecurityException {
        IOException fileFailure = null;
        try {
            deleteRecursively(namespaceDirectory(identity));
        } catch (IOException error) {
            fileFailure = error;
        }
        keyManager.deleteSecret(identity.privateFileKeyAlias(), keyEnvelopeFile(identity));
        if (fileFailure != null) {
            throw fileFailure;
        }
    }

    File encryptedFile(UserDatabaseIdentity identity, Category category, String storageId) {
        if (storageId == null || !STORAGE_ID.matcher(storageId).matches()) {
            throw new IllegalArgumentException("storageId is invalid");
        }
        return new File(categoryDirectory(identity, category), storageId + ".ptimg");
    }

    File keyEnvelopeFile(UserDatabaseIdentity identity) {
        return new File(new File(context.getNoBackupFilesDir(), "key_envelopes"),
                identity.privateFileKeyAlias() + ".bin");
    }

    private NormalizedImage normalize(InputStream source) throws IOException {
        byte[] input = readBounded(source, MAX_INPUT_BYTES);
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(input, 0, input.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            Arrays.fill(input, (byte) 0);
            throw new IllegalArgumentException("Input is not a supported image");
        }

        int sampleSize = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_DECODE_DIMENSION) {
            sampleSize *= 2;
        }

        BitmapFactory.Options decode = new BitmapFactory.Options();
        decode.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeByteArray(input, 0, input.length, decode);
        Arrays.fill(input, (byte) 0);
        if (bitmap == null) {
            throw new IllegalArgumentException("Input image could not be decoded");
        }

        Bitmap outputBitmap = bitmap;
        int sourceMax = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (sourceMax > MAX_DIMENSION) {
            float scale = MAX_DIMENSION / (float) sourceMax;
            int width = Math.max(1, Math.round(bitmap.getWidth() * scale));
            int height = Math.max(1, Math.round(bitmap.getHeight() * scale));
            outputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!outputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                throw new IOException("Unable to normalize private image");
            }
            byte[] bytes = output.toByteArray();
            if (bytes.length > MAX_OUTPUT_BYTES) {
                Arrays.fill(bytes, (byte) 0);
                throw new IOException("Normalized image exceeds the private-file limit");
            }
            return new NormalizedImage(bytes, outputBitmap.getWidth(), outputBitmap.getHeight());
        } finally {
            if (outputBitmap != bitmap) {
                outputBitmap.recycle();
            }
            bitmap.recycle();
        }
    }

    private void writeEncrypted(File target, byte[] plaintext, byte[] secret)
            throws IOException, GeneralSecurityException {
        File parent = target.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("Unable to create private image directory");
        }

        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(secret, "AES"),
                new GCMParameterSpec(128, iv));
        byte[] ciphertext = cipher.doFinal(plaintext);

        File temporary = File.createTempFile("image-", ".tmp", parent);
        boolean moved = false;
        try (FileOutputStream fileOutput = new FileOutputStream(temporary);
             DataOutputStream output = new DataOutputStream(fileOutput)) {
            output.writeInt(FILE_MAGIC);
            output.writeInt(FILE_VERSION);
            output.writeInt(iv.length);
            output.write(iv);
            output.writeInt(plaintext.length);
            output.writeInt(ciphertext.length);
            output.write(ciphertext);
            output.flush();
            fileOutput.getFD().sync();
        }
        try {
            if (!temporary.renameTo(target)) {
                throw new IOException("Unable to publish encrypted private image");
            }
            moved = true;
        } finally {
            Arrays.fill(ciphertext, (byte) 0);
            if (!moved) {
                temporary.delete();
            }
        }
    }

    private byte[] readEncrypted(File file, byte[] secret)
            throws IOException, GeneralSecurityException {
        try (DataInputStream input = new DataInputStream(new FileInputStream(file))) {
            if (input.readInt() != FILE_MAGIC || input.readInt() != FILE_VERSION) {
                throw new GeneralSecurityException("Encrypted image header is invalid");
            }
            int ivLength = input.readInt();
            if (ivLength != IV_BYTES) {
                throw new GeneralSecurityException("Encrypted image IV is invalid");
            }
            byte[] iv = new byte[ivLength];
            input.readFully(iv);

            int plaintextLength = input.readInt();
            int ciphertextLength = input.readInt();
            if (plaintextLength < 1 || plaintextLength > MAX_OUTPUT_BYTES
                    || ciphertextLength < plaintextLength
                    || ciphertextLength > MAX_OUTPUT_BYTES + 32) {
                throw new GeneralSecurityException("Encrypted image lengths are invalid");
            }
            byte[] ciphertext = new byte[ciphertextLength];
            try {
                input.readFully(ciphertext);
                if (input.read() != -1) {
                    throw new GeneralSecurityException("Encrypted image has trailing data");
                }

                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(secret, "AES"),
                        new GCMParameterSpec(128, iv));
                byte[] plaintext = cipher.doFinal(ciphertext);
                if (plaintext.length != plaintextLength) {
                    Arrays.fill(plaintext, (byte) 0);
                    throw new GeneralSecurityException("Encrypted image size does not match its header");
                }
                return plaintext;
            } finally {
                Arrays.fill(ciphertext, (byte) 0);
            }
        } catch (EOFException error) {
            throw new GeneralSecurityException("Encrypted image is truncated", error);
        }
    }

    private boolean protectedDataExists(UserDatabaseIdentity identity) {
        return containsEncryptedFile(namespaceDirectory(identity));
    }

    private static boolean containsEncryptedFile(File file) {
        if (file.isFile()) {
            return file.getName().endsWith(".ptimg");
        }
        File[] children = file.listFiles();
        if (children == null) {
            return false;
        }
        for (File child : children) {
            if (containsEncryptedFile(child)) {
                return true;
            }
        }
        return false;
    }

    private File namespaceDirectory(UserDatabaseIdentity identity) {
        return new File(new File(context.getNoBackupFilesDir(), "private_images"), identity.uidHash());
    }

    private File categoryDirectory(UserDatabaseIdentity identity, Category category) {
        return new File(namespaceDirectory(identity), category.name().toLowerCase(Locale.ROOT));
    }

    private static byte[] readBounded(InputStream source, int maximum) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = source.read(buffer)) != -1) {
            total += read;
            if (total > maximum) {
                throw new IOException("Private image input exceeds the size limit");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static void deleteRecursively(File file) throws IOException {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to delete protected path");
        }
    }

    private static void clear(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    private static final class NormalizedImage {
        private final byte[] bytes;
        private final int width;
        private final int height;

        private NormalizedImage(byte[] bytes, int width, int height) {
            this.bytes = bytes;
            this.width = width;
            this.height = height;
        }

        private void clear() {
            Arrays.fill(bytes, (byte) 0);
        }
    }
}
