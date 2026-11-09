package com.prottasha.data.local.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.ui.AuthActivity;
import com.prottasha.core.security.KeystoreKeyManager;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.data.local.user.entity.ImportJournalEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public final class EncryptedUserDatabaseTest {

    private ActivityScenario<AuthActivity> foregroundHost;

    @Before
    public void keepTargetProcessForegroundOnHios() {
        foregroundHost = ActivityScenario.launch(AuthActivity.class);
    }

    @After
    public void closeForegroundHost() {
        if (foregroundHost != null) {
            foregroundHost.close();
        }
    }

    @Test
    public void opensWritesClosesAndReopensEncryptedRows() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("alice");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            UserDatabase database = provider.open(user);
            assertEquals(UserDatabaseIdentity.fromUid(user.uid()).uidHash(),
                    database.localAccountDao().requireAccount().uidHash);
            try (Cursor foreignKeys = database.query("PRAGMA foreign_keys", null)) {
                assertTrue(foreignKeys.moveToFirst());
                assertEquals(1, foreignKeys.getInt(0));
            }
            database.importJournalDao().upsert(journal("source-a", "INSPECTED"));
            provider.close();

            File databaseFile = context.getDatabasePath(
                    UserDatabaseIdentity.fromUid(user.uid()).databaseFileName());
            assertTrue(databaseFile.isFile());
            assertFalse(readPrefix(databaseFile, 16).startsWith("SQLite format 3"));

            UserDatabase reopened = provider.open(user);
            assertEquals("INSPECTED",
                    reopened.importJournalDao().find("source-a").state);
        } finally {
            provider.close();
            provider.destroy(user);
        }
    }

    @Test
    public void usersHaveDifferentDatabasesAndCannotBeOpenTogether() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser alice = uniqueUser("alice");
        SessionUser bob = uniqueUser("bob");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            provider.open(alice).importJournalDao().upsert(journal("alice-only", "COPIED"));
            assertThrows(IllegalStateException.class, () -> provider.open(bob));
            provider.close();

            assertNull(provider.open(bob).importJournalDao().find("alice-only"));
            provider.close();

            assertNotNull(provider.open(alice).importJournalDao().find("alice-only"));
        } finally {
            provider.close();
            provider.destroy(alice);
            provider.destroy(bob);
        }
    }

    @Test
    public void closeInvalidatesProviderAndMissingEnvelopeFailsClosed() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("key-loss");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            provider.open(user).importJournalDao().upsert(journal("keep", "VERIFIED"));
            provider.close();
            assertThrows(IllegalStateException.class, provider::requireOpen);

            File envelope = provider.keyEnvelopeFile(user);
            assertTrue(envelope.delete());

            assertThrows(KeystoreKeyManager.KeyUnavailableException.class,
                    () -> provider.open(user));
            assertTrue(context.getDatabasePath(
                    UserDatabaseIdentity.fromUid(user.uid()).databaseFileName()).exists());
        } finally {
            provider.close();
            provider.destroy(user);
        }
    }

    @Test
    public void corruptEnvelopeFailsClosedWithoutReplacingDatabase() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("corrupt-key");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            provider.open(user).importJournalDao().upsert(journal("keep", "VERIFIED"));
            provider.close();
            File databaseFile = context.getDatabasePath(
                    UserDatabaseIdentity.fromUid(user.uid()).databaseFileName());
            long databaseLength = databaseFile.length();

            File envelope = provider.keyEnvelopeFile(user);
            try (RandomAccessFile randomAccess = new RandomAccessFile(envelope, "rw")) {
                long last = randomAccess.length() - 1;
                randomAccess.seek(last);
                int value = randomAccess.read();
                randomAccess.seek(last);
                randomAccess.write(value ^ 0x01);
            }

            assertThrows(KeystoreKeyManager.KeyUnavailableException.class,
                    () -> provider.open(user));
            assertTrue(databaseFile.exists());
            assertEquals(databaseLength, databaseFile.length());
        } finally {
            provider.close();
            provider.destroy(user);
        }
    }

    @Test
    public void structurallyValidEnvelopeWithWrongDatabaseKeyFailsBeforeNativeOpen()
            throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("wrong-key");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            provider.open(user).importJournalDao().upsert(journal("keep", "VERIFIED"));
            provider.close();

            UserDatabaseIdentity identity = UserDatabaseIdentity.fromUid(user.uid());
            File databaseFile = context.getDatabasePath(identity.databaseFileName());
            long databaseLength = databaseFile.length();
            File replacementEnvelope = new File(
                    provider.keyEnvelopeFile(user).getParentFile(),
                    "wrong-" + UUID.randomUUID() + ".bin");
            byte[] wrongKey = new KeystoreKeyManager().getOrCreateSecret(
                    identity.databaseKeyAlias(), replacementEnvelope, false);
            Arrays.fill(wrongKey, (byte) 0);
            Files.move(replacementEnvelope.toPath(), provider.keyEnvelopeFile(user).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            assertThrows(KeystoreKeyManager.KeyUnavailableException.class,
                    () -> provider.open(user));
            assertTrue(databaseFile.exists());
            assertEquals(databaseLength, databaseFile.length());
        } finally {
            provider.close();
            provider.destroy(user);
        }
    }

    @Test
    public void backgroundOpenRequiresThePersistedVerifiedOwner() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("background");
        SessionUser other = uniqueUser("other");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            String userHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();
            String otherHash = UserDatabaseIdentity.fromUid(other.uid()).uidHash();
            provider.open(user).importJournalDao().upsert(journal("background", "READY"));
            provider.close();

            assertThrows(SecurityException.class,
                    () -> provider.openForVerifiedBackgroundOwner(otherHash));
            assertEquals("READY", provider.openForVerifiedBackgroundOwner(userHash)
                    .importJournalDao().find("background").state);
            provider.close();

            provider.clearVerifiedBackgroundOwner();
            assertThrows(SecurityException.class,
                    () -> provider.openForVerifiedBackgroundOwner(userHash));
        } finally {
            provider.close();
            provider.destroy(user);
            provider.destroy(other);
        }
    }

    @Test
    public void quarantineAndRestorePreserveRows() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("quarantine");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);

        try {
            provider.open(user).importJournalDao().upsert(journal("restore-me", "COPYING"));
            provider.close();

            provider.quarantine(user);
            assertFalse(context.getDatabasePath(
                    UserDatabaseIdentity.fromUid(user.uid()).databaseFileName()).exists());
            assertTrue(provider.quarantinedDatabaseFile(user).exists());

            provider.restore(user);
            assertEquals("COPYING",
                    provider.open(user).importJournalDao().find("restore-me").state);
        } finally {
            provider.close();
            provider.destroy(user);
        }
    }

    @Test
    public void postAuthCleanupDestroysQuarantineByOpaqueHash() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        SessionUser user = uniqueUser("post-auth-cleanup");
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);
        String uidHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();

        provider.open(user).importJournalDao().upsert(journal("delete", "READY"));
        provider.close();
        provider.quarantine(user);

        provider.destroyByUidHash(uidHash);

        assertFalse(provider.quarantinedDatabaseFile(user).exists());
        assertFalse(provider.keyEnvelopeFile(user).exists());
        assertThrows(SecurityException.class,
                () -> provider.openForVerifiedBackgroundOwner(uidHash));
    }

    private static ImportJournalEntity journal(String sourceHash, String state) {
        return new ImportJournalEntity(
                sourceHash, state, "{}", "{}", System.currentTimeMillis());
    }

    private static SessionUser uniqueUser(String prefix) {
        String token = UUID.randomUUID().toString();
        return new SessionUser(prefix + "-" + token, prefix, prefix + "@example.test", true);
    }

    private static String readPrefix(File file, int maximum) throws Exception {
        byte[] bytes = new byte[maximum];
        int read;
        try (FileInputStream input = new FileInputStream(file)) {
            read = input.read(bytes);
        }
        return new String(bytes, 0, Math.max(0, read), StandardCharsets.US_ASCII);
    }
}
