package com.prottasha.auth.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.prottasha.auth.domain.SessionUser;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public final class CloudUserProfileTest {

    @Test
    public void bootstrapProfileMatchesDeployedMinimalSchemaWithoutPrivateIdentity() {
        SessionUser user = new SessionUser(
                "firebase-uid-alice", "Alice Example", "alice@example.test", false);
        Object serverTimestamp = new Object();

        CloudUserProfile profile = CloudUserProfile.bootstrap(user);
        Map<String, Object> fields = profile.createFields(serverTimestamp);

        assertEquals(new HashSet<>(Arrays.asList(
                        "displayName", "podPseudonym", "createdAt", "updatedAt", "schemaVersion")),
                fields.keySet());
        assertEquals("Alice Example", fields.get("displayName"));
        assertEquals(1, fields.get("schemaVersion"));
        assertSame(serverTimestamp, fields.get("createdAt"));
        assertSame(serverTimestamp, fields.get("updatedAt"));
        assertTrue(profile.podPseudonym().matches("Hope-[0-9A-F]{6}"));

        String serialized = fields.values().toString();
        assertFalse(serialized.contains(user.uid()));
        assertFalse(serialized.contains(user.email()));
    }

    @Test
    public void generatedPseudonymIsStableAndDifferentAcrossUsers() {
        CloudUserProfile first = CloudUserProfile.bootstrap(new SessionUser(
                "uid-a", "Same Name", "a@example.test", false));
        CloudUserProfile again = CloudUserProfile.bootstrap(new SessionUser(
                "uid-a", "Same Name", "a@example.test", true));
        CloudUserProfile other = CloudUserProfile.bootstrap(new SessionUser(
                "uid-b", "Same Name", "b@example.test", false));

        assertEquals(first.podPseudonym(), again.podPseudonym());
        assertFalse(first.podPseudonym().equals(other.podPseudonym()));
    }

    @Test
    public void profileWithAvatarFieldsIncludesAvatarInSerializedFields() {
        SessionUser user = new SessionUser("uid-c", "Charlie", "c@example.test", true);
        CloudUserProfile profile = CloudUserProfile.bootstrap(user)
                .withAvatar("seed-12345", "PALETTE_EMERALD", "STYLE_GEOMETRIC");

        assertEquals("seed-12345", profile.avatarSeedOrNull());
        assertEquals("PALETTE_EMERALD", profile.avatarPaletteOrNull());
        assertEquals("STYLE_GEOMETRIC", profile.avatarStyleOrNull());

        Object timestamp = new Object();
        Map<String, Object> fields = profile.createFields(timestamp);
        assertEquals("seed-12345", fields.get("avatarSeed"));
        assertEquals("PALETTE_EMERALD", fields.get("avatarPalette"));
        assertEquals("STYLE_GEOMETRIC", fields.get("avatarStyle"));
    }

    @Test
    public void fromFieldsParsesAvatarFieldsWhenPresent() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("displayName", "Dana");
        map.put("podPseudonym", "Hope-123456");
        map.put("schemaVersion", 1);
        map.put("createdAt", new com.google.firebase.Timestamp(100, 0));
        map.put("updatedAt", new com.google.firebase.Timestamp(200, 0));
        map.put("avatarSeed", "seed-xyz");
        map.put("avatarPalette", "OCEAN");
        map.put("avatarStyle", "INITIALS");

        CloudUserProfile parsed = CloudUserProfile.fromFields(map);
        assertEquals("Dana", parsed.displayName());
        assertEquals("Hope-123456", parsed.podPseudonym());
        assertEquals("seed-xyz", parsed.avatarSeedOrNull());
        assertEquals("OCEAN", parsed.avatarPaletteOrNull());
        assertEquals("INITIALS", parsed.avatarStyleOrNull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidAvatarLengthThrowsIllegalArgumentException() {
        SessionUser user = new SessionUser("uid-d", "David", "d@example.test", true);
        StringBuilder longSeed = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            longSeed.append("x");
        }
        CloudUserProfile.bootstrap(user).withAvatar(longSeed.toString(), "PALETTE", "STYLE");
    }
}
