package com.prottasha.data.local.legacy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.habits.data.HabitEntity;

import java.time.LocalDate;

public final class HabitLegacyMapper {
    public int importInto(Context context, UserDatabase destination) {
        java.io.File source = context.getDatabasePath(AppDatabase.DATABASE_NAME);
        if (!source.isFile()) return 0;
        long day = LocalDate.now().toEpochDay();
        int copied = 0;
        try (SQLiteDatabase database = SQLiteDatabase.openDatabase(
                source.getPath(), null, SQLiteDatabase.OPEN_READONLY);
             Cursor cursor = database.rawQuery(
                     "SELECT title, isGoodHabit, lastLoggedTimestamp FROM habits "
                             + "WHERE title NOT IN (?, ?) ORDER BY id",
                     new String[] {"Deep Study Session", "Compulsive Short Video Scrolling"})) {
            while (cursor.moveToNext()) {
                destination.habitDao().insert(new HabitEntity(
                        0L, cursor.getString(0), cursor.getInt(1) == 1 ? "BUILD" : "AVOID",
                        1, "DAILY", day, 1, 0, false, copied,
                        cursor.isNull(2) ? System.currentTimeMillis() : cursor.getLong(2)));
                copied++;
            }
        }
        return copied;
    }
}
