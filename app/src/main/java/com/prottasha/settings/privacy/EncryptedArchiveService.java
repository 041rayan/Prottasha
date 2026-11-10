package com.prottasha.settings.privacy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class EncryptedArchiveService {
    private static final int MAGIC = 0x50544131; // PTA1
    private static final int MAX_BYTES = 32 * 1024 * 1024;
    private final byte[] payload;

    public EncryptedArchiveService(byte[] payload) {
        this.payload = payload.clone();
    }

    public void exportTo(OutputStream destination, char[] password)
            throws IOException, GeneralSecurityException {
        byte[] salt = new byte[16];
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(salt);
        new SecureRandom().nextBytes(iv);
        byte[] key = derive(password, salt);
        byte[] encrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv));
            encrypted = cipher.doFinal(payload);
        } finally {
            Arrays.fill(key, (byte) 0);
        }
        DataOutputStream output = new DataOutputStream(destination);
        output.writeInt(MAGIC);
        output.writeInt(salt.length); output.write(salt);
        output.writeInt(iv.length); output.write(iv);
        output.writeInt(encrypted.length); output.write(encrypted);
        output.flush();
        Arrays.fill(encrypted, (byte) 0);
    }

    public byte[] importFrom(InputStream source, char[] password)
            throws IOException, GeneralSecurityException {
        DataInputStream input = new DataInputStream(source);
        if (input.readInt() != MAGIC) throw new GeneralSecurityException("Invalid archive");
        byte[] salt = read(input, 16, 32);
        byte[] iv = read(input, 12, 32);
        byte[] encrypted = read(input, 16, MAX_BYTES);
        byte[] key = derive(password, salt);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv));
            return cipher.doFinal(encrypted);
        } finally {
            Arrays.fill(key, (byte) 0);
            Arrays.fill(encrypted, (byte) 0);
        }
    }

    private static byte[] read(DataInputStream input, int minimum, int maximum)
            throws IOException, GeneralSecurityException {
        int length = input.readInt();
        if (length < minimum || length > maximum) throw new GeneralSecurityException("Invalid archive length");
        byte[] value = new byte[length];
        input.readFully(value);
        return value;
    }

    private static byte[] derive(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 100_000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); Arrays.fill(password, '\0'); }
    }
}
