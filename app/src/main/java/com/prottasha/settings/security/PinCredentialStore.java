package com.prottasha.settings.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PinCredentialStore {
    private static final int ITERATIONS = 50_000;

    public static final class Verifier {
        public final byte[] salt;
        public final byte[] hash;
        Verifier(byte[] salt, byte[] hash) { this.salt = salt; this.hash = hash; }
    }

    private PinCredentialStore() { }

    public static Verifier create(char[] pin) throws GeneralSecurityException {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return new Verifier(salt, derive(pin, salt));
    }

    public static boolean verify(char[] pin, Verifier verifier) throws GeneralSecurityException {
        byte[] candidate = derive(pin, verifier.salt);
        try { return MessageDigest.isEqual(candidate, verifier.hash); }
        finally { Arrays.fill(candidate, (byte) 0); }
    }

    private static byte[] derive(char[] pin, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(pin, salt, ITERATIONS, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); Arrays.fill(pin, '\0'); }
    }
}
