package com.prottasha.data.local.user;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class UserDatabaseMigrations {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS identities ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "statement TEXT NOT NULL, purpose TEXT NOT NULL, "
                    + "createdAt INTEGER NOT NULL, active INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS success_memories ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "identityId INTEGER NOT NULL, text TEXT NOT NULL, createdAt INTEGER NOT NULL, "
                    + "FOREIGN KEY(identityId) REFERENCES identities(id) ON UPDATE NO ACTION "
                    + "ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_success_memories_identityId "
                    + "ON success_memories(identityId)");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS habits ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, "
                    + "type TEXT NOT NULL, target INTEGER NOT NULL, scheduleFrequency TEXT NOT NULL, "
                    + "startEpochDay INTEGER NOT NULL, scheduleInterval INTEGER NOT NULL, "
                    + "scheduleDayOfMonth INTEGER NOT NULL, archived INTEGER NOT NULL, "
                    + "sortOrder INTEGER NOT NULL, createdAt INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_substeps ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, "
                    + "title TEXT NOT NULL, sortOrder INTEGER NOT NULL, "
                    + "FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_substeps_habitId ON habit_substeps(habitId)");
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_events ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, "
                    + "epochDay INTEGER NOT NULL, kind TEXT NOT NULL, amount INTEGER NOT NULL, "
                    + "nonce TEXT NOT NULL, revertedEventId INTEGER NOT NULL, occurredAt INTEGER NOT NULL, "
                    + "zoneId TEXT NOT NULL, cutoffMinutes INTEGER NOT NULL, "
                    + "FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_events_habitId ON habit_events(habitId)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_events_nonce ON habit_events(nonce)");
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_day_aggregates ("
                    + "habitId INTEGER NOT NULL, epochDay INTEGER NOT NULL, value INTEGER NOT NULL, "
                    + "completed INTEGER NOT NULL, lapsed INTEGER NOT NULL, updatedAt INTEGER NOT NULL, "
                    + "PRIMARY KEY(habitId, epochDay), "
                    + "FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_day_aggregates_habitId "
                    + "ON habit_day_aggregates(habitId)");
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_vacations ("
                    + "habitId INTEGER NOT NULL, startEpochDay INTEGER NOT NULL, endEpochDay INTEGER NOT NULL, "
                    + "PRIMARY KEY(habitId, startEpochDay), "
                    + "FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
        }
    };

    public static final Migration[] ALL = {MIGRATION_1_2, MIGRATION_2_3};
    public static final RoomDatabase.Callback ENABLE_FOREIGN_KEYS =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase database) {
                    database.setForeignKeyConstraintsEnabled(true);
                }
            };

    private UserDatabaseMigrations() {
    }
}
