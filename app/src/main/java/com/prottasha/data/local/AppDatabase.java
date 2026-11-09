package com.prottasha.data.local;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

import java.util.concurrent.Executors;

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

    private static final String DATABASE_NAME = "prottasha_database.db";
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
                    )
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadExecutor().execute(() -> {
                                // Seed default starter identity and habits
                                IdentityDao iDao = getInstance(context).identityDao();
                                long identityId = iDao.insert(new IdentityEntity(
                                        "I am a focused scholar and mindful creator",
                                        "Focus, Integrity, Presence, Mental Toughness",
                                        "Master attention and overcome compulsive screen loops",
                                        null,
                                        System.currentTimeMillis(),
                                        true
                                ));

                                HabitDao hDao = getInstance(context).habitDao();
                                hDao.insert(new HabitEntity(
                                        identityId,
                                        "Deep Study Session",
                                        true,
                                        "Sitting at desk with notebook open",
                                        "Desire for mastery and quiet focus",
                                        "Put phone in another room, read 2 minutes",
                                        "Record 1 identity vote and feel satisfied"
                                ));
                                hDao.insert(new HabitEntity(
                                        identityId,
                                        "Compulsive Short Video Scrolling",
                                        false,
                                        "Feeling stuck or emotionally fatigued",
                                        "Craving quick distraction and numbing",
                                        "Trigger 10s pause screen; 3 deep breaths",
                                        "Avoid shame; rebound immediately"
                                ));

                                TriggerDao tDao = getInstance(context).triggerDao();
                                tDao.insert(new TriggerEntity(
                                        "Late evening alone in bedroom with smartphone",
                                        "Relief from daily mental exhaustion",
                                        "When I enter bed, I will place phone across the room and read a book",
                                        120
                                ));
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
