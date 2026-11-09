package com.prottasha.data.local.legacy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.identity.data.IdentityEntity;

public final class IdentityLegacyMapper {
    public int importInto(Context context, UserDatabase destination) {
        if (destination.identityDao().findActive() != null) return 0;
        java.io.File source = context.getDatabasePath(AppDatabase.DATABASE_NAME);
        if (!source.isFile()) return 0;
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
                source.getPath(), null, SQLiteDatabase.OPEN_READONLY);
             Cursor cursor = database.rawQuery(
                     "SELECT identityStatement, targetOutcome, createdAt FROM identities "
                             + "WHERE identityStatement != ? ORDER BY isActive DESC, createdAt DESC LIMIT 1",
                     new String[] {"I am a focused scholar and mindful creator"})) {
            if (!cursor.moveToFirst()) return 0;
            destination.identityDao().deactivateAll();
            destination.identityDao().insert(new IdentityEntity(
                    0L, cursor.getString(0), cursor.isNull(1) ? "" : cursor.getString(1),
                    cursor.getLong(2), true));
            return 1;
        }
    }
}
