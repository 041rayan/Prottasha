package com.prottasha.auth.data;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.UserDatabaseIdentity;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CloudUserProfile {

    public static final int SCHEMA_VERSION = 1;

    private final String displayName;
    private final String podPseudonym;
    @Nullable
    private final Timestamp createdAt;
    @Nullable
    private final Timestamp updatedAt;

    private CloudUserProfile(
            String displayName,
            String podPseudonym,
            @Nullable Timestamp createdAt,
            @Nullable Timestamp updatedAt) {
        this.displayName = requireNonBlank(displayName, "displayName");
        this.podPseudonym = requireNonBlank(podPseudonym, "podPseudonym");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CloudUserProfile bootstrap(SessionUser user) {
        String displayName = user.displayNameOrNull();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = "Member";
        }
        String suffix = UserDatabaseIdentity.fromUid(user.uid())
                .uidHash()
                .substring(0, 6)
                .toUpperCase(Locale.ROOT);
        return new CloudUserProfile(displayName, "Hope-" + suffix, null, null);
    }

    public static CloudUserProfile fromFields(Map<String, Object> fields) {
        Object version = fields.get("schemaVersion");
        if (!(version instanceof Number) || ((Number) version).intValue() != SCHEMA_VERSION) {
            throw new IllegalStateException("Unsupported cloud profile schema");
        }
        Object displayName = fields.get("displayName");
        Object pseudonym = fields.get("podPseudonym");
        Object created = fields.get("createdAt");
        Object updated = fields.get("updatedAt");
        if (!(displayName instanceof String)
                || !(pseudonym instanceof String)
                || !(created instanceof Timestamp)
                || !(updated instanceof Timestamp)) {
            throw new IllegalStateException("Cloud profile fields are invalid");
        }
        return new CloudUserProfile(
                (String) displayName,
                (String) pseudonym,
                (Timestamp) created,
                (Timestamp) updated);
    }

    public Map<String, Object> createFields(Object serverTimestamp) {
        Objects.requireNonNull(serverTimestamp, "serverTimestamp");
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("displayName", displayName);
        fields.put("podPseudonym", podPseudonym);
        fields.put("createdAt", serverTimestamp);
        fields.put("updatedAt", serverTimestamp);
        fields.put("schemaVersion", SCHEMA_VERSION);
        return fields;
    }

    public String displayName() {
        return displayName;
    }

    public String podPseudonym() {
        return podPseudonym;
    }

    @Nullable
    public Timestamp createdAtOrNull() {
        return createdAt;
    }

    @Nullable
    public Timestamp updatedAtOrNull() {
        return updatedAt;
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
