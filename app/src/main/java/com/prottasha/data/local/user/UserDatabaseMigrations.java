package com.prottasha.data.local.user;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public final class UserDatabaseMigrations {

    public static final Migration[] ALL = new Migration[0];
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
