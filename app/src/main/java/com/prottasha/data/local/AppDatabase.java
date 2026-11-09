package com.prottasha.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.prottasha.data.local.dao.AssessmentDao;
import com.prottasha.data.local.dao.HabitDao;
import com.prottasha.data.local.dao.IdentityDao;
import com.prottasha.data.local.dao.JournalDao;
import com.prottasha.data.local.dao.TriggerDao;
import com.prottasha.data.local.entity.AssessmentResultEntity;
import com.prottasha.data.local.entity.HabitEntity;
import com.prottasha.data.local.entity.IdentityEntity;
import com.prottasha.data.local.entity.JournalEntryEntity;
import com.prottasha.data.local.entity.TriggerEntity;
import com.prottasha.data.local.entity.UrgeLogEntity;

@Database(
    entities = {
        IdentityEntity.class,
        HabitEntity.class,
        TriggerEntity.class,
        UrgeLogEntity.class,
        JournalEntryEntity.class,
        AssessmentResultEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "prottasha_database.db";
    private static volatile AppDatabase INSTANCE;

    public abstract IdentityDao identityDao();
    public abstract HabitDao habitDao();
    public abstract TriggerDao triggerDao();
    public abstract JournalDao journalDao();
    public abstract AssessmentDao assessmentDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }

    public static void closeInstance() {
        synchronized (AppDatabase.class) {
            if (INSTANCE != null) {
                INSTANCE.close();
                INSTANCE = null;
            }
        }
    }
}
