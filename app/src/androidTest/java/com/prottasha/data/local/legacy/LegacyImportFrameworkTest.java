package com.prottasha.data.local.legacy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.ui.AuthActivity;
import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.data.local.user.entity.ImportJournalEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public final class LegacyImportFrameworkTest {

    private Context context;
    private File sourceFile;
    private ActivityScenario<AuthActivity> foregroundHost;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        sourceFile = context.getDatabasePath(AppDatabase.DATABASE_NAME);
        AppDatabase.closeInstance();
        SQLiteDatabase.deleteDatabase(sourceFile);
        foregroundHost = ActivityScenario.launch(AuthActivity.class);
    }

    @After
    public void tearDown() {
        AppDatabase.closeInstance();
        SQLiteDatabase.deleteDatabase(sourceFile);
        if (foregroundHost != null) {
            foregroundHost.close();
        }
    }

    @Test
    public void inspectStageVerifyAndRetryPreserveSourceBytes() throws Exception {
        createLegacySource();
        byte[] before = Files.readAllBytes(sourceFile.toPath());
        LegacySnapshotReader reader = new LegacySnapshotReader(context);

        LegacySnapshot snapshot = reader.inspect();

        assertTrue(snapshot.sourcePresent());
        assertEquals(64, snapshot.sourceHash().length());
        assertEquals(2, count(snapshot.rowCounts(), "identities"));
        assertEquals(2, count(snapshot.rowCounts(), "habits"));
        assertEquals(1, count(snapshot.rowCounts(), "triggers"));
        assertTrue(snapshot.sampleFlags().contains(LegacySnapshot.PROTOTYPE_IDENTITY));
        assertTrue(snapshot.sampleFlags().contains(LegacySnapshot.PROTOTYPE_BUILD_HABIT));
        assertTrue(snapshot.sampleFlags().contains(LegacySnapshot.PROTOTYPE_AVOID_HABIT));
        assertTrue(snapshot.sampleFlags().contains(LegacySnapshot.PROTOTYPE_TRIGGER));

        SessionUser user = uniqueUser();
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);
        try {
            UserDatabase destination = provider.open(user);
            LegacyImportCoordinator coordinator = new LegacyImportCoordinator(reader);

            LegacyImportReport staged = coordinator.importInto(destination, snapshot);
            assertEquals(LegacyImportReport.Status.STAGED, staged.status());
            assertEquals(snapshot.rowCounts(), staged.rowCounts());
            assertEquals(snapshot.sampleFlags(), staged.sampleFlags());
            assertEquals(LegacyImportReport.Status.VERIFIED,
                    coordinator.verify(destination, snapshot).status());
            assertEquals(LegacyImportReport.Status.ALREADY_VERIFIED,
                    coordinator.importInto(destination, snapshot).status());
            assertEquals(1, destination.importJournalDao().list().size());

            destination.importJournalDao().upsert(new ImportJournalEntity(
                    "incomplete-source", "STAGED", "{}", "{}", 1L));
            assertEquals(LegacyImportReport.Status.ROLLED_BACK,
                    coordinator.rollbackIncomplete(destination, "incomplete-source").status());
            assertNull(destination.importJournalDao().find("incomplete-source"));
        } finally {
            provider.close();
            provider.destroy(user);
        }

        assertArrayEquals(before, Files.readAllBytes(sourceFile.toPath()));
    }

    @Test
    public void failedStagingRollsBackItsJournal() throws Exception {
        createLegacySource();
        LegacySnapshot snapshot = new LegacySnapshotReader(context).inspect();
        FailingJournalStore store = new FailingJournalStore();
        LegacyImportCoordinator coordinator = new LegacyImportCoordinator(
                new LegacySnapshotReader(context));

        assertThrows(IllegalStateException.class,
                () -> coordinator.importInto(store, snapshot));

        assertNull(store.stored);
        assertTrue(store.rollbackCalled);
    }

    @Test
    public void inspectingAbsentSourceDoesNotCreateIt() throws Exception {
        assertFalse(sourceFile.exists());

        LegacySnapshot snapshot = new LegacySnapshotReader(context).inspect();

        assertFalse(snapshot.sourcePresent());
        assertFalse(sourceFile.exists());
        assertTrue(snapshot.rowCounts().isEmpty());
    }

    @Test
    public void newlyOpenedLegacyDatabaseDoesNotSeedPrototypeRows() throws Exception {
        AppDatabase database = AppDatabase.getInstance(context);
        database.getOpenHelper().getWritableDatabase();
        Thread.sleep(1000L);

        try (Cursor cursor = database.query("SELECT COUNT(*) FROM identities", null)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(0, cursor.getInt(0));
        }
    }

    private void createLegacySource() {
        File parent = sourceFile.getParentFile();
        assertTrue(parent != null && (parent.exists() || parent.mkdirs()));
        try (SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(sourceFile, null)) {
            database.execSQL("CREATE TABLE identities (id INTEGER PRIMARY KEY, identityStatement TEXT)");
            database.execSQL("CREATE TABLE habits (id INTEGER PRIMARY KEY, title TEXT)");
            database.execSQL("CREATE TABLE triggers (id INTEGER PRIMARY KEY, situationCue TEXT)");
            database.execSQL("CREATE TABLE urge_logs (id INTEGER PRIMARY KEY)");
            database.execSQL("CREATE TABLE journal_entries (id INTEGER PRIMARY KEY)");
            database.execSQL("CREATE TABLE assessment_results (id INTEGER PRIMARY KEY)");
            database.execSQL("INSERT INTO identities VALUES (1, ?)", new Object[] {
                    "I am a focused scholar and mindful creator"
            });
            database.execSQL("INSERT INTO identities VALUES (2, 'My real identity')");
            database.execSQL("INSERT INTO habits VALUES (1, 'Deep Study Session')");
            database.execSQL(
                    "INSERT INTO habits VALUES (2, 'Compulsive Short Video Scrolling')");
            database.execSQL("INSERT INTO triggers VALUES (1, ?)", new Object[] {
                    "Late evening alone in bedroom with smartphone"
            });
        }
    }

    private static int count(Map<String, Integer> counts, String table) {
        Integer count = counts.get(table);
        return count == null ? -1 : count;
    }

    private static SessionUser uniqueUser() {
        String value = UUID.randomUUID().toString();
        return new SessionUser("legacy-" + value, "Legacy", "legacy@example.test", true);
    }

    private static final class FailingJournalStore
            implements LegacyImportCoordinator.ImportJournalStore {
        private ImportJournalEntity stored;
        private boolean rollbackCalled;

        @Override
        public ImportJournalEntity find(String sourceHash) {
            return stored != null && stored.sourceHash.equals(sourceHash) ? stored : null;
        }

        @Override
        public void upsert(ImportJournalEntity journal) {
            stored = journal;
            throw new IllegalStateException("planned staging failure");
        }

        @Override
        public void delete(String sourceHash) {
            rollbackCalled = true;
            stored = null;
        }
    }
}
