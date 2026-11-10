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
    public static final int MAX_DISPLAY_NAME_LENGTH = 80;
    public static final int MAX_PSEUDONYM_LENGTH = 40;
    public static final int MAX_AVATAR_SEED_LENGTH = 64;
    public static final int MAX_AVATAR_PALETTE_LENGTH = 24;
    public static final int MAX_AVATAR_STYLE_LENGTH = 24;

    private final String displayName;
    private final String podPseudonym;
    @Nullable
    private final String avatarSeed;
    @Nullable
    private final String avatarPalette;
    @Nullable
    private final String avatarStyle;
    @Nullable
    private final Timestamp createdAt;
    @Nullable
    private final Timestamp updatedAt;

    public CloudUserProfile(
            String displayName,
            String podPseudonym,
            @Nullable String avatarSeed,
            @Nullable String avatarPalette,
            @Nullable String avatarStyle,
            @Nullable Timestamp createdAt,
            @Nullable Timestamp updatedAt) {
        this.displayName = validateLength(requireNonBlank(displayName, "displayName"),
                "displayName", MAX_DISPLAY_NAME_LENGTH);
        this.podPseudonym = validateLength(requireNonBlank(podPseudonym, "podPseudonym"),
                "podPseudonym", MAX_PSEUDONYM_LENGTH);
        if (avatarSeed != null || avatarPalette != null || avatarStyle != null) {
            this.avatarSeed = validateLength(requireNonBlank(avatarSeed, "avatarSeed"),
                    "avatarSeed", MAX_AVATAR_SEED_LENGTH);
            this.avatarPalette = validateLength(requireNonBlank(avatarPalette, "avatarPalette"),
                    "avatarPalette", MAX_AVATAR_PALETTE_LENGTH);
            this.avatarStyle = validateLength(requireNonBlank(avatarStyle, "avatarStyle"),
                    "avatarStyle", MAX_AVATAR_STYLE_LENGTH);
        } else {
            this.avatarSeed = null;
            this.avatarPalette = null;
            this.avatarStyle = null;
        }
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
        return new CloudUserProfile(
                displayName,
                "Hope-" + suffix,
                null,
                null,
                null,
                null,
                null);
    }

    public CloudUserProfile withAvatar(String seed, String palette, String style) {
        return new CloudUserProfile(
                displayName,
                podPseudonym,
                seed,
                palette,
                style,
                createdAt,
                updatedAt);
    }

    public CloudUserProfile withDisplayName(String newDisplayName) {
        return new CloudUserProfile(
                newDisplayName,
                podPseudonym,
                avatarSeed,
                avatarPalette,
                avatarStyle,
                createdAt,
                updatedAt);
    }

    public CloudUserProfile withPodPseudonym(String newPodPseudonym) {
        return new CloudUserProfile(
                displayName,
                newPodPseudonym,
                avatarSeed,
                avatarPalette,
                avatarStyle,
                createdAt,
                updatedAt);
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

        Object seed = fields.get("avatarSeed");
        Object palette = fields.get("avatarPalette");
        Object style = fields.get("avatarStyle");

        String avatarSeed = (seed instanceof String) ? (String) seed : null;
        String avatarPalette = (palette instanceof String) ? (String) palette : null;
        String avatarStyle = (style instanceof String) ? (String) style : null;

        return new CloudUserProfile(
                (String) displayName,
                (String) pseudonym,
                avatarSeed,
                avatarPalette,
                avatarStyle,
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
        if (avatarSeed != null && avatarPalette != null && avatarStyle != null) {
            fields.put("avatarSeed", avatarSeed);
            fields.put("avatarPalette", avatarPalette);
            fields.put("avatarStyle", avatarStyle);
        }
        return fields;
    }

    public Map<String, Object> updateFields(Object serverTimestamp) {
        Objects.requireNonNull(serverTimestamp, "serverTimestamp");
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("displayName", displayName);
        fields.put("podPseudonym", podPseudonym);
        fields.put("updatedAt", serverTimestamp);
        if (avatarSeed != null && avatarPalette != null && avatarStyle != null) {
            fields.put("avatarSeed", avatarSeed);
            fields.put("avatarPalette", avatarPalette);
            fields.put("avatarStyle", avatarStyle);
        }
        return fields;
    }

    public String displayName() {
        return displayName;
    }

    public String podPseudonym() {
        return podPseudonym;
    }

    @Nullable
    public String avatarSeedOrNull() {
        return avatarSeed;
    }

    @Nullable
    public String avatarPaletteOrNull() {
        return avatarPalette;
    }

    @Nullable
    public String avatarStyleOrNull() {
        return avatarStyle;
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

    private static String validateLength(String value, String name, int maxLength) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(name + " length exceeds maximum of " + maxLength);
        }
        return value;
    }
}
