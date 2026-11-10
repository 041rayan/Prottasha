package com.prottasha.data.local.user;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.room.Room;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.KeystoreKeyManager;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.data.local.user.entity.LocalAccountMetadataEntity;

import net.zetetic.database.sqlcipher.SupportOpenHelperFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class UserSessionDatabaseProvider {

    private static final String SESSION_PREFERENCES = "verified_session_owner";
    private static final String VERIFIED_OWNER_HASH = "verified_owner_uid_hash";
    private static final int KEY_CHECK_MAGIC = 0x50544431; // PTD1
    private static final int KEY_CHECK_VERSION = 1;
    private static final int KEY_CHECK_BYTES = 32;
    private static final byte[] KEY_CHECK_MESSAGE =
            "prottasha-user-database-key-check-v1".getBytes(StandardCharsets.US_ASCII);

    static {
        System.loadLibrary("sqlcipher");
    }

    private final Context context;
    private final KeystoreKeyManager keyManager = new KeystoreKeyManager();

    @Nullable
    private UserDatabase activeDatabase;
    @Nullable
    private UserDatabaseIdentity activeIdentity;
    @Nullable
    private byte[] activePassphrase;

    public UserSessionDatabaseProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized UserDatabase open(SessionUser user)
            throws IOException, GeneralSecurityException {
        if (!user.isEmailVerified()) {
            throw new SecurityException("Verified Firebase account required");
        }
        UserDatabaseIdentity requested = UserDatabaseIdentity.fromUid(user.uid());
        UserDatabase database = openIdentity(requested, true);
        if (!context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(VERIFIED_OWNER_HASH, requested.uidHash())
                .commit()) {
            close();
            throw new IOException("Unable to persist verified background owner");
        }
        return database;
    }

    public synchronized UserDatabase openForVerifiedBackgroundOwner(String uidHash)
            throws IOException, GeneralSecurityException {
        UserDatabaseIdentity requested = UserDatabaseIdentity.fromUidHash(uidHash);
        String persisted = context.getSharedPreferences(
                        SESSION_PREFERENCES, Context.MODE_PRIVATE)
                .getString(VERIFIED_OWNER_HASH, null);
        if (!requested.uidHash().equals(persisted) || !databaseFile(requested).isFile()) {
            throw new SecurityException("Background database owner is not verified");
        }
        return openIdentity(requested, false);
    }

    public synchronized void clearVerifiedBackgroundOwner() {
        if (!context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .remove(VERIFIED_OWNER_HASH)
                .commit()) {
            throw new IllegalStateException("Unable to clear verified background owner");
        }
    }

    public synchronized String verifiedBackgroundOwnerHashOrNull() {
        return context.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)
                .getString(VERIFIED_OWNER_HASH, null);
    }

    private UserDatabase openIdentity(UserDatabaseIdentity requested, boolean allowCreate)
            throws IOException, GeneralSecurityException {
        if (activeDatabase != null) {
            if (activeIdentity != null && activeIdentity.uidHash().equals(requested.uidHash())) {
                return activeDatabase;
            }
            throw new IllegalStateException("Close the active user database before switching owners");
        }

        File databaseFile = databaseFile(requested);
        if (!allowCreate && !databaseFile.isFile()) {
            throw new SecurityException("Background database does not exist");
        }
        boolean databaseExisted = databaseFile.isFile();
        byte[] secret = null;
        UserDatabase database = null;
        boolean passphraseTransferred = false;
        try {
            secret = keyManager.getOrCreateSecret(
                    requested.databaseKeyAlias(),
                    keyEnvelopeFile(requested),
                    databaseFile.exists() || quarantinedDatabaseFile(requested).exists());
            if (databaseExisted) {
                verifyDatabaseKeyCheck(requested, secret);
            }
            SupportOpenHelperFactory factory = new SupportOpenHelperFactory(
                    secret, null, false);
            database = Room.databaseBuilder(context, UserDatabase.class, requested.databaseFileName())
                    .openHelperFactory(factory)
                    .addCallback(UserDatabaseMigrations.ENABLE_FOREIGN_KEYS)
                    .addMigrations(UserDatabaseMigrations.ALL)
                    .build();
            database.getOpenHelper().getWritableDatabase();
            initializeOrVerifyOwner(database, requested);
            if (!databaseExisted) {
                writeDatabaseKeyCheck(requested, secret);
            }
            activeDatabase = database;
            activeIdentity = requested;
            activePassphrase = secret;
            passphraseTransferred = true;
            return database;
        } catch (IOException | GeneralSecurityException error) {
            if (database != null) {
                database.close();
            }
            if (!databaseExisted) {
                deleteNewDatabaseArtifacts(databaseFile, error);
            }
            throw error;
        } catch (RuntimeException error) {
            if (database != null) {
                database.close();
            }
            if (!databaseExisted) {
                deleteNewDatabaseArtifacts(databaseFile, error);
            }
            throw error;
        } finally {
            if (!passphraseTransferred && secret != null) {
                Arrays.fill(secret, (byte) 0);
            }
        }
    }

    public synchronized boolean isOpen() {
        return activeDatabase != null && activeDatabase.isOpen();
    }

    public synchronized UserDatabase requireOpen() {
        if (activeDatabase == null || !activeDatabase.isOpen()) {
            throw new IllegalStateException("No user database is open");
        }
        return activeDatabase;
    }

    public synchronized void close() {
        if (activeDatabase != null) {
            activeDatabase.close();
        }
        if (activePassphrase != null) {
            Arrays.fill(activePassphrase, (byte) 0);
        }
        activeDatabase = null;
        activeIdentity = null;
        activePassphrase = null;
    }

    public synchronized void quarantine(SessionUser user) throws IOException {
        closeIfOwner(user);
        File source = databaseFile(user);
        File target = quarantinedDatabaseFile(user);
        File parent = target.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("Unable to create deletion quarantine");
        }
        if (!source.exists()) {
            return;
        }
        move(source, target);
        moveIfPresent(new File(source.getPath() + "-wal"), new File(target.getPath() + "-wal"));
        moveIfPresent(new File(source.getPath() + "-shm"), new File(target.getPath() + "-shm"));
    }

    public synchronized void restore(SessionUser user) throws IOException {
        closeIfOwner(user);
        File source = quarantinedDatabaseFile(user);
        File target = databaseFile(user);
        if (!source.exists()) {
            return;
        }
        File parent = target.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("Unable to restore user database directory");
        }
        move(source, target);
        moveIfPresent(new File(source.getPath() + "-wal"), new File(target.getPath() + "-wal"));
        moveIfPresent(new File(source.getPath() + "-shm"), new File(target.getPath() + "-shm"));
    }

    public synchronized void destroy(SessionUser user) throws IOException, GeneralSecurityException {
        closeIfOwner(user);
        destroyIdentity(UserDatabaseIdentity.fromUid(user.uid()));
    }

    public synchronized void destroyByUidHash(String uidHash)
            throws IOException, GeneralSecurityException {
        UserDatabaseIdentity identity = UserDatabaseIdentity.fromUidHash(uidHash);
        closeIfIdentity(identity);
        destroyIdentity(identity);
    }

    private void destroyIdentity(UserDatabaseIdentity identity)
            throws IOException, GeneralSecurityException {
        File databaseFile = databaseFile(identity);
        File quarantinedFile = quarantinedDatabaseFile(identity);
        deleteIfPresent(databaseFile);
        deleteIfPresent(new File(databaseFile.getPath() + "-wal"));
        deleteIfPresent(new File(databaseFile.getPath() + "-shm"));
        deleteIfPresent(quarantinedFile);
        deleteIfPresent(new File(quarantinedFile.getPath() + "-wal"));
        deleteIfPresent(new File(quarantinedFile.getPath() + "-shm"));
        deleteIfPresent(databaseKeyCheckFile(identity));
        keyManager.deleteSecret(identity.databaseKeyAlias(), keyEnvelopeFile(identity));
        String persisted = context.getSharedPreferences(
                        SESSION_PREFERENCES, Context.MODE_PRIVATE)
                .getString(VERIFIED_OWNER_HASH, null);
        if (identity.uidHash().equals(persisted)) {
            clearVerifiedBackgroundOwner();
        }
    }

    File keyEnvelopeFile(SessionUser user) {
        return keyEnvelopeFile(UserDatabaseIdentity.fromUid(user.uid()));
    }

    private File keyEnvelopeFile(UserDatabaseIdentity identity) {
        return new File(new File(context.getNoBackupFilesDir(), "key_envelopes"),
                identity.databaseKeyAlias() + ".bin");
    }

    File quarantinedDatabaseFile(SessionUser user) {
        return quarantinedDatabaseFile(UserDatabaseIdentity.fromUid(user.uid()));
    }

    private File quarantinedDatabaseFile(UserDatabaseIdentity identity) {
        return new File(new File(context.getNoBackupFilesDir(), "pending_deletion"),
                identity.databaseFileName());
    }

    private File databaseFile(SessionUser user) {
        return databaseFile(UserDatabaseIdentity.fromUid(user.uid()));
    }

    private File databaseFile(UserDatabaseIdentity identity) {
        return context.getDatabasePath(identity.databaseFileName());
    }

    private File databaseKeyCheckFile(UserDatabaseIdentity identity) {
        return new File(new File(context.getNoBackupFilesDir(), "database_key_checks"),
                identity.databaseKeyAlias() + ".bin");
    }

    private void verifyDatabaseKeyCheck(UserDatabaseIdentity identity, byte[] secret)
            throws IOException, GeneralSecurityException {
        File checkFile = databaseKeyCheckFile(identity);
        if (!checkFile.isFile()) {
            throw new KeystoreKeyManager.KeyUnavailableException(
                    "Database key check is missing for existing protected data");
        }
        byte[] stored = new byte[KEY_CHECK_BYTES];
        try (DataInputStream input = new DataInputStream(new FileInputStream(checkFile))) {
            if (input.readInt() != KEY_CHECK_MAGIC || input.readInt() != KEY_CHECK_VERSION) {
                throw new KeystoreKeyManager.KeyUnavailableException(
                        "Database key check header is invalid");
            }
            int length = input.readInt();
            if (length != KEY_CHECK_BYTES) {
                throw new KeystoreKeyManager.KeyUnavailableException(
                        "Database key check length is invalid");
            }
            input.readFully(stored);
            if (input.read() != -1) {
                throw new KeystoreKeyManager.KeyUnavailableException(
                        "Database key check has trailing data");
            }
        } catch (EOFException error) {
            throw new KeystoreKeyManager.KeyUnavailableException(
                    "Database key check is truncated", error);
        }

        byte[] expected = calculateKeyCheck(secret);
        try {
            if (!MessageDigest.isEqual(stored, expected)) {
                throw new KeystoreKeyManager.KeyUnavailableException(
                        "Database key does not match protected data");
            }
        } finally {
            Arrays.fill(stored, (byte) 0);
            Arrays.fill(expected, (byte) 0);
        }
    }

    private void writeDatabaseKeyCheck(UserDatabaseIdentity identity, byte[] secret)
            throws IOException, GeneralSecurityException {
        File target = databaseKeyCheckFile(identity);
        File parent = target.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("Unable to create database key-check directory");
        }
        byte[] check = calculateKeyCheck(secret);
        File temporary = File.createTempFile("db-check-", ".tmp", parent);
        boolean moved = false;
        try {
            try (FileOutputStream fileOutput = new FileOutputStream(temporary);
                 DataOutputStream output = new DataOutputStream(fileOutput)) {
                output.writeInt(KEY_CHECK_MAGIC);
                output.writeInt(KEY_CHECK_VERSION);
                output.writeInt(check.length);
                output.write(check);
                output.flush();
                fileOutput.getFD().sync();
            }
            if (!temporary.renameTo(target)) {
                throw new IOException("Unable to publish database key check");
            }
            moved = true;
        } finally {
            Arrays.fill(check, (byte) 0);
            if (!moved) {
                temporary.delete();
            }
        }
    }

    private static byte[] calculateKeyCheck(byte[] secret) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(KEY_CHECK_MESSAGE);
    }

    private static void initializeOrVerifyOwner(
            UserDatabase database,
            UserDatabaseIdentity identity) {
        LocalAccountMetadataEntity account = database.localAccountDao().findAccount();
        if (account == null) {
            database.localAccountDao().insert(new LocalAccountMetadataEntity(
                    1, identity.uidHash(), System.currentTimeMillis(), 0));
        } else if (!account.uidHash.equals(identity.uidHash())) {
            throw new SecurityException("Encrypted database owner does not match session");
        }
    }

    private void closeIfOwner(SessionUser user) {
        closeIfIdentity(UserDatabaseIdentity.fromUid(user.uid()));
    }

    private void closeIfIdentity(UserDatabaseIdentity requested) {
        if (activeIdentity == null) {
            return;
        }
        if (!activeIdentity.uidHash().equals(requested.uidHash())) {
            throw new IllegalStateException("A different user database is active");
        }
        close();
    }

    private static void moveIfPresent(File source, File target) throws IOException {
        if (source.exists()) {
            move(source, target);
        }
    }

    private static void move(File source, File target) throws IOException {
        if (target.exists() && !target.delete()) {
            throw new IOException("Unable to replace destination during database move");
        }
        if (!source.renameTo(target)) {
            throw new IOException("Unable to move encrypted user database");
        }
    }

    private static void deleteIfPresent(File file) throws IOException {
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to delete encrypted user database path");
        }
    }

    private static void deleteNewDatabaseArtifacts(File databaseFile, Throwable original) {
        for (File file : new File[] {
                databaseFile,
                new File(databaseFile.getPath() + "-wal"),
                new File(databaseFile.getPath() + "-shm")
        }) {
            try {
                deleteIfPresent(file);
            } catch (IOException cleanupFailure) {
                original.addSuppressed(cleanupFailure);
            }
        }
    }
}
