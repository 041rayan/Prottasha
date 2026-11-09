package com.prottasha.core.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class UserDatabaseIdentityTest {

    @Test
    public void sameUidProducesStableOpaqueNames() {
        UserDatabaseIdentity first = UserDatabaseIdentity.fromUid("firebase-uid-alice");
        UserDatabaseIdentity second = UserDatabaseIdentity.fromUid("firebase-uid-alice");

        assertEquals(first.uidHash(), second.uidHash());
        assertEquals(first.databaseFileName(), second.databaseFileName());
        assertEquals(first.databaseKeyAlias(), second.databaseKeyAlias());
        assertEquals(first.privateFileKeyAlias(), second.privateFileKeyAlias());
        assertTrue(first.uidHash().matches("[0-9a-f]{64}"));
        assertTrue(first.databaseFileName().matches("prottasha_[0-9a-f]{64}\\.db"));
    }

    @Test
    public void differentUidsProduceDifferentNamespaces() {
        UserDatabaseIdentity alice = UserDatabaseIdentity.fromUid("firebase-uid-alice");
        UserDatabaseIdentity bob = UserDatabaseIdentity.fromUid("firebase-uid-bob");

        assertNotEquals(alice.uidHash(), bob.uidHash());
        assertNotEquals(alice.databaseFileName(), bob.databaseFileName());
        assertNotEquals(alice.databaseKeyAlias(), bob.databaseKeyAlias());
        assertNotEquals(alice.privateFileKeyAlias(), bob.privateFileKeyAlias());
    }

    @Test
    public void namesContainNeitherUidNorEmail() {
        String rawUid = "rayan@example.com-firebase-uid";
        UserDatabaseIdentity identity = UserDatabaseIdentity.fromUid(rawUid);

        String allNames = identity.uidHash()
                + identity.databaseFileName()
                + identity.databaseKeyAlias()
                + identity.privateFileKeyAlias();
        assertFalse(allNames.contains(rawUid));
        assertFalse(allNames.contains("rayan"));
        assertFalse(allNames.contains("example.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBlankUid() {
        UserDatabaseIdentity.fromUid("  ");
    }
}
