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

    public static final Migration[] ALL = {MIGRATION_1_2};
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
