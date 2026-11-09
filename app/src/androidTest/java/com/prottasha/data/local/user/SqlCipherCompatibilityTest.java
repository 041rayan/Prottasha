package com.prottasha.data.local.user;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.database.Cursor;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.zetetic.database.sqlcipher.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public final class SqlCipherCompatibilityTest {

    @Test
    public void directSqlCipherOpenWriteReadCloseWorksOnReferenceDevice() {
        System.loadLibrary("sqlcipher");
        Context context = ApplicationProvider.getApplicationContext();
        File file = context.getDatabasePath("sqlcipher_probe_" + UUID.randomUUID() + ".db");
        byte[] password = "academic-probe-key".getBytes(StandardCharsets.UTF_8);

        try (SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(
                file, password, null, null)) {
            database.execSQL("CREATE TABLE probe (value TEXT NOT NULL)");
            database.execSQL("INSERT INTO probe(value) VALUES ('ready')");
            try (Cursor cursor = database.rawQuery("SELECT value FROM probe", null)) {
                cursor.moveToFirst();
                assertEquals("ready", cursor.getString(0));
            }
        } finally {
            java.util.Arrays.fill(password, (byte) 0);
            SQLiteDatabase.deleteDatabase(file);
        }
    }
}
