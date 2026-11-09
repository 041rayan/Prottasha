package com.prottasha.data.local.legacy;

import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.entity.ImportJournalEntity;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public final class LegacyImportCoordinator {

    interface ImportJournalStore {
        ImportJournalEntity find(String sourceHash);
        void upsert(ImportJournalEntity journal);
        void delete(String sourceHash);
    }

    private final LegacySnapshotReader reader;

    public LegacyImportCoordinator(LegacySnapshotReader reader) {
        this.reader = reader;
    }

    public LegacySnapshot inspect() throws IOException {
        return reader.inspect();
    }

    public LegacyImportReport importInto(UserDatabase database, LegacySnapshot snapshot) {
        return importInto(store(database), snapshot);
    }

    LegacyImportReport importInto(ImportJournalStore store, LegacySnapshot snapshot) {
        if (!snapshot.sourcePresent()) {
            return new LegacyImportReport(LegacyImportReport.Status.NO_SOURCE, "");
        }
        ImportJournalEntity existing = store.find(snapshot.sourceHash());
        if (existing != null) {
            return new LegacyImportReport(
                    "VERIFIED".equals(existing.state)
                            ? LegacyImportReport.Status.ALREADY_VERIFIED
                            : LegacyImportReport.Status.ALREADY_STAGED,
                    snapshot);
        }

        try {
            store.upsert(new ImportJournalEntity(
                    snapshot.sourceHash(),
                    "STAGED",
                    countsJson(snapshot.rowCounts()),
                    "{}",
                    System.currentTimeMillis()));
        } catch (RuntimeException error) {
            try {
                store.delete(snapshot.sourceHash());
            } catch (RuntimeException rollbackFailure) {
                error.addSuppressed(rollbackFailure);
            }
            throw error;
        }
        return new LegacyImportReport(LegacyImportReport.Status.STAGED, snapshot);
    }

    public LegacyImportReport verify(UserDatabase database, LegacySnapshot expected)
            throws IOException {
        LegacySnapshot current = reader.inspect();
        if (!current.sourcePresent()
                || !current.sourceHash().equals(expected.sourceHash())
                || !current.rowCounts().equals(expected.rowCounts())) {
            throw new IllegalStateException("Legacy source changed during import staging");
        }
        ImportJournalEntity journal = database.importJournalDao().find(expected.sourceHash());
        if (journal == null) {
            throw new IllegalStateException("Legacy import journal is missing");
        }
        if ("VERIFIED".equals(journal.state)) {
            return new LegacyImportReport(
                    LegacyImportReport.Status.ALREADY_VERIFIED, expected);
        }
        if (!countsJson(expected.rowCounts()).equals(journal.expectedCountsJson)) {
            throw new IllegalStateException("Legacy import counts do not match the journal");
        }
        database.importJournalDao().upsert(new ImportJournalEntity(
                expected.sourceHash(),
                "VERIFIED",
                journal.expectedCountsJson,
                journal.copiedCountsJson,
                System.currentTimeMillis()));
        return new LegacyImportReport(LegacyImportReport.Status.VERIFIED, expected);
    }

    public LegacyImportReport rollbackIncomplete(UserDatabase database, String sourceHash) {
        ImportJournalEntity journal = database.importJournalDao().find(sourceHash);
        if (journal == null || "VERIFIED".equals(journal.state)) {
            return new LegacyImportReport(
                    LegacyImportReport.Status.NOTHING_TO_ROLL_BACK, sourceHash);
        }
        database.importJournalDao().delete(sourceHash);
        return new LegacyImportReport(LegacyImportReport.Status.ROLLED_BACK, sourceHash);
    }

    private static ImportJournalStore store(UserDatabase database) {
        return new ImportJournalStore() {
            @Override
            public ImportJournalEntity find(String sourceHash) {
                return database.importJournalDao().find(sourceHash);
            }

            @Override
            public void upsert(ImportJournalEntity journal) {
                database.importJournalDao().upsert(journal);
            }

            @Override
            public void delete(String sourceHash) {
                database.importJournalDao().delete(sourceHash);
            }
        };
    }

    private static String countsJson(Map<String, Integer> counts) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : new TreeMap<>(counts).entrySet()) {
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
        }
        return json.append('}').toString();
    }
}
