package com.prottasha.core.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class KeystoreKeyManager {

    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int ENVELOPE_MAGIC = 0x50544B31; // PTK1
    private static final int ENVELOPE_VERSION = 1;
    private static final int SECRET_BYTES = 32;
    private static final int MAX_IV_BYTES = 32;
    private static final int MAX_CIPHERTEXT_BYTES = 128;

    private final SecureRandom random = new SecureRandom();

    public synchronized byte[] getOrCreateSecret(
            String alias,
            File envelopeFile,
            boolean protectedDataExists) throws IOException, GeneralSecurityException {
        if (envelopeFile.exists()) {
            return decryptEnvelope(alias, envelopeFile);
        }
        if (protectedDataExists) {
            throw new KeyUnavailableException("Key envelope is missing for existing protected data");
        }

        SecretKey wrappingKey = getOrCreateWrappingKey(alias);
        byte[] secret = new byte[SECRET_BYTES];
        random.nextBytes(secret);
        try {
            writeEnvelope(envelopeFile, wrappingKey, secret);
            return secret.clone();
        } finally {
            java.util.Arrays.fill(secret, (byte) 0);
        }
    }

    public synchronized void deleteSecret(String alias, File envelopeFile)
            throws IOException, GeneralSecurityException {
        KeyStore keyStore = loadKeyStore();
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias);
        }
        if (envelopeFile.exists() && !envelopeFile.delete()) {
            throw new IOException("Unable to delete key envelope");
        }
    }

    private byte[] decryptEnvelope(String alias, File envelopeFile)
            throws IOException, GeneralSecurityException {
        KeyStore keyStore = loadKeyStore();
        SecretKey key = (SecretKey) keyStore.getKey(alias, null);
        if (key == null) {
            throw new KeyUnavailableException("Android Keystore key is unavailable");
        }

        try (DataInputStream input = new DataInputStream(new FileInputStream(envelopeFile))) {
            if (input.readInt() != ENVELOPE_MAGIC || input.readInt() != ENVELOPE_VERSION) {
                throw new KeyUnavailableException("Key envelope header is invalid");
            }
            int ivLength = input.readInt();
            if (ivLength < 12 || ivLength > MAX_IV_BYTES) {
                throw new KeyUnavailableException("Key envelope IV is invalid");
            }
            byte[] iv = new byte[ivLength];
            input.readFully(iv);

            int ciphertextLength = input.readInt();
            if (ciphertextLength < SECRET_BYTES || ciphertextLength > MAX_CIPHERTEXT_BYTES) {
                throw new KeyUnavailableException("Key envelope payload is invalid");
            }
            byte[] ciphertext = new byte[ciphertextLength];
            input.readFully(ciphertext);
            if (input.read() != -1) {
                throw new KeyUnavailableException("Key envelope has trailing data");
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] secret = cipher.doFinal(ciphertext);
            if (secret.length != SECRET_BYTES) {
                java.util.Arrays.fill(secret, (byte) 0);
                throw new KeyUnavailableException("Unwrapped key has an invalid size");
            }
            return secret;
        } catch (EOFException | GeneralSecurityException error) {
            if (error instanceof KeyUnavailableException) {
                throw error;
            }
            throw new KeyUnavailableException("Unable to unwrap protected key", error);
        }
    }

    private void writeEnvelope(File envelopeFile, SecretKey key, byte[] secret)
            throws IOException, GeneralSecurityException {
        File parent = envelopeFile.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("Unable to create key envelope directory");
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] ciphertext = cipher.doFinal(secret);
        byte[] iv = cipher.getIV();

        File temporary = File.createTempFile("key-", ".tmp", parent);
        boolean moved = false;
        try (FileOutputStream fileOutput = new FileOutputStream(temporary);
             DataOutputStream output = new DataOutputStream(fileOutput)) {
            output.writeInt(ENVELOPE_MAGIC);
            output.writeInt(ENVELOPE_VERSION);
            output.writeInt(iv.length);
            output.write(iv);
            output.writeInt(ciphertext.length);
            output.write(ciphertext);
            output.flush();
            fileOutput.getFD().sync();
        }
        try {
            if (!temporary.renameTo(envelopeFile)) {
                throw new IOException("Unable to publish key envelope");
            }
            moved = true;
        } finally {
            if (!moved) {
                temporary.delete();
            }
        }
    }

    private SecretKey getOrCreateWrappingKey(String alias) throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore();
        SecretKey existing = (SecretKey) keyStore.getKey(alias, null);
        if (existing != null) {
            return existing;
        }

        KeyGenerator generator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
        generator.init(new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    private static KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
        keyStore.load(null);
        return keyStore;
    }

    public static final class KeyUnavailableException extends GeneralSecurityException {
        public KeyUnavailableException(String message) {
            super(message);
        }

        public KeyUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
