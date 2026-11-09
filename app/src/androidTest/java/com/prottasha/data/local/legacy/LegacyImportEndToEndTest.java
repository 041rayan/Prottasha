package com.prottasha.data.local.legacy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.data.local.AppDatabase;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public final class LegacyImportEndToEndTest {

    @Test
    public void importsOnlyRealIdentityAndHabitWithoutChangingSource() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File source = context.getDatabasePath(AppDatabase.DATABASE_NAME);
        SQLiteDatabase.deleteDatabase(source);
        File parent = source.getParentFile();
        if (parent != null) parent.mkdirs();
        try (SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(source, null)) {
            db.execSQL("CREATE TABLE identities (id INTEGER PRIMARY KEY, identityStatement TEXT, "
                    + "targetOutcome TEXT, createdAt INTEGER, isActive INTEGER)");
            db.execSQL("CREATE TABLE habits (id INTEGER PRIMARY KEY, title TEXT, "
                    + "isGoodHabit INTEGER, lastLoggedTimestamp INTEGER)");
            db.execSQL("INSERT INTO identities VALUES (1, ?, '', 1, 0)", new Object[] {
                    "I am a focused scholar and mindful creator"});
            db.execSQL("INSERT INTO identities VALUES (2, 'I choose deliberate work', "
                    + "'Build a meaningful life', 2, 1)");
            db.execSQL("INSERT INTO habits VALUES (1, 'Deep Study Session', 1, 1)");
            db.execSQL("INSERT INTO habits VALUES (2, 'Morning walk', 1, 2)");
        }
        byte[] before = Files.readAllBytes(source.toPath());
        SessionUser user = new SessionUser(
                "legacy-map-" + UUID.randomUUID(), "Legacy", "legacy-map@example.test", true);
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);
        try {
            UserDatabase destination = provider.open(user);
            assertEquals(1, new IdentityLegacyMapper().importInto(context, destination));
            assertEquals(1, new HabitLegacyMapper().importInto(context, destination));
            assertEquals("I choose deliberate work", destination.identityDao().findActive().statement);
            assertEquals("Morning walk", destination.habitDao().listActive().get(0).title);
            assertArrayEquals(before, Files.readAllBytes(source.toPath()));
        } finally {
            provider.close();
            provider.destroy(user);
            SQLiteDatabase.deleteDatabase(source);
        }
    }
}
