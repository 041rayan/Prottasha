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
}
