package com.prottasha.data.local.legacy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prottasha.data.local.AppDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class LegacySnapshotReader {

    private static final String[] TABLES = {
            "identities",
            "habits",
            "triggers",
            "urge_logs",
            "journal_entries",
            "assessment_results"
    };

    private final File sourceFile;

    public LegacySnapshotReader(Context context) {
        sourceFile = context.getApplicationContext().getDatabasePath(AppDatabase.DATABASE_NAME);
    }

    public LegacySnapshot inspect() throws IOException {
        if (!sourceFile.isFile()) {
            return LegacySnapshot.absent();
        }

        String sourceHash = sha256(sourceFile);
        long sourceBytes = sourceFile.length();
        Map<String, Integer> counts = new LinkedHashMap<>();
        Set<String> sampleFlags = new LinkedHashSet<>();
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
                sourceFile.getPath(), null,
                SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS)) {
            for (String table : TABLES) {
                counts.put(table, tableExists(database, table) ? count(database, table) : 0);
            }
            addFlagIfPresent(
                    database,
                    "identities",
                    "identityStatement",
                    "I am a focused scholar and mindful creator",
                    LegacySnapshot.PROTOTYPE_IDENTITY,
                    sampleFlags);
            addFlagIfPresent(
                    database,
                    "habits",
                    "title",
                    "Deep Study Session",
                    LegacySnapshot.PROTOTYPE_BUILD_HABIT,
                    sampleFlags);
            addFlagIfPresent(
                    database,
                    "habits",
                    "title",
                    "Compulsive Short Video Scrolling",
                    LegacySnapshot.PROTOTYPE_AVOID_HABIT,
                    sampleFlags);
            addFlagIfPresent(
                    database,
                    "triggers",
                    "situationCue",
                    "Late evening alone in bedroom with smartphone",
                    LegacySnapshot.PROTOTYPE_TRIGGER,
                    sampleFlags);
        }
        return new LegacySnapshot(true, sourceHash, sourceBytes, counts, sampleFlags);
    }

    private static boolean tableExists(SQLiteDatabase database, String table) {
        try (Cursor cursor = database.rawQuery(
                "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ? LIMIT 1",
                new String[] {table})) {
            return cursor.moveToFirst();
        }
    }

    private static int count(SQLiteDatabase database, String table) {
        try (Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM `" + table + "`", null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    private static void addFlagIfPresent(
            SQLiteDatabase database,
            String table,
            String column,
            String value,
            String flag,
            Set<String> flags) {
        if (!tableExists(database, table)) {
            return;
        }
        try (Cursor cursor = database.rawQuery(
                "SELECT 1 FROM `" + table + "` WHERE `" + column + "` = ? LIMIT 1",
                new String[] {value})) {
            if (cursor.moveToFirst()) {
                flags.add(flag);
            }
        }
    }

    private static String sha256(File file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
        byte[] buffer = new byte[8192];
        try (FileInputStream input = new FileInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        StringBuilder hex = new StringBuilder(64);
        for (byte value : digest.digest()) {
            int unsigned = value & 0xff;
            hex.append(Character.forDigit(unsigned >>> 4, 16));
            hex.append(Character.forDigit(unsigned & 0x0f, 16));
        }
        return hex.toString();
    }
}
