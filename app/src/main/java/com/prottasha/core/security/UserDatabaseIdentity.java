package com.prottasha.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class UserDatabaseIdentity {

    private final String uidHash;

    private UserDatabaseIdentity(String uidHash) {
        this.uidHash = uidHash;
    }

    public static UserDatabaseIdentity fromUid(String uid) {
        if (uid == null || uid.trim().isEmpty()) {
            throw new IllegalArgumentException("uid must not be blank");
        }
        return new UserDatabaseIdentity(sha256(uid));
    }

    public String uidHash() {
        return uidHash;
    }

    public String databaseFileName() {
        return "prottasha_" + uidHash + ".db";
    }

    public String databaseKeyAlias() {
        return "prottasha_db_key_" + uidHash;
    }

    public String privateFileKeyAlias() {
        return "prottasha_file_key_" + uidHash;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                int unsigned = item & 0xff;
                hex.append(Character.forDigit(unsigned >>> 4, 16));
                hex.append(Character.forDigit(unsigned & 0x0f, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
