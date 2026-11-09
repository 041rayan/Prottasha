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

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_reminders ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, "
                    + "hour INTEGER NOT NULL, minute INTEGER NOT NULL, dayMask INTEGER NOT NULL, "
                    + "enabled INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habits(id) "
                    + "ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_reminders_habitId ON habit_reminders(habitId)");
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_day_notes ("
                    + "habitId INTEGER NOT NULL, epochDay INTEGER NOT NULL, note TEXT NOT NULL, "
                    + "updatedAt INTEGER NOT NULL, PRIMARY KEY(habitId, epochDay), "
                    + "FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE TABLE IF NOT EXISTS habit_day_images ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, "
                    + "epochDay INTEGER NOT NULL, storageId TEXT NOT NULL, "
                    + "FOREIGN KEY(habitId) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_day_images_habitId ON habit_day_images(habitId)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_day_images_storageId ON habit_day_images(storageId)");
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS guard_rules ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, target TEXT NOT NULL, "
                    + "label TEXT NOT NULL, mode TEXT NOT NULL, budgetMinutes INTEGER NOT NULL, "
                    + "startMinute INTEGER NOT NULL, endMinute INTEGER NOT NULL, enabled INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_guard_rules_target ON guard_rules(target)");
            database.execSQL("CREATE TABLE IF NOT EXISTS guard_schedules ("
                    + "ruleId INTEGER NOT NULL, dayOfWeek INTEGER NOT NULL, enabled INTEGER NOT NULL, "
                    + "PRIMARY KEY(ruleId, dayOfWeek), FOREIGN KEY(ruleId) REFERENCES guard_rules(id) "
                    + "ON UPDATE NO ACTION ON DELETE CASCADE)");
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS usage_daily ("
                    + "target TEXT NOT NULL, epochDay INTEGER NOT NULL, totalMillis INTEGER NOT NULL, "
                    + "quality TEXT NOT NULL, PRIMARY KEY(target, epochDay))");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS intervention_events ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, target TEXT NOT NULL, "
                    + "type TEXT NOT NULL, timestamp INTEGER NOT NULL, intention TEXT NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS focus_session ("
                    + "id INTEGER NOT NULL, durationMillis INTEGER NOT NULL, startedAt INTEGER NOT NULL, "
                    + "accumulatedMillis INTEGER NOT NULL, state TEXT NOT NULL, PRIMARY KEY(id))");
        }
    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS reflections ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, epochDay INTEGER NOT NULL, "
                    + "fact TEXT NOT NULL, interpretation TEXT NOT NULL, lesson TEXT NOT NULL, "
                    + "createdAt INTEGER NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS reflection_images ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, entryId INTEGER NOT NULL, "
                    + "storageId TEXT NOT NULL, FOREIGN KEY(entryId) REFERENCES reflections(id) "
                    + "ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_reflection_images_entryId "
                    + "ON reflection_images(entryId)");
            database.execSQL("CREATE TABLE IF NOT EXISTS trigger_plans ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, cue TEXT NOT NULL, "
                    + "plan TEXT NOT NULL, createdAt INTEGER NOT NULL)");
        }
    };

    public static final Migration[] ALL = {
            MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
            MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8
    };
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
