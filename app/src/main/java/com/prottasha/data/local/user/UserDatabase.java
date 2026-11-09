package com.prottasha.data.local.user;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.prottasha.data.local.user.dao.ImportJournalDao;
import com.prottasha.data.local.user.dao.LocalAccountDao;
import com.prottasha.data.local.user.entity.ImportJournalEntity;
import com.prottasha.data.local.user.entity.LocalAccountMetadataEntity;
import com.prottasha.identity.data.IdentityDao;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.habits.data.HabitDao;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitEventEntity;
import com.prottasha.habits.data.HabitSubstepEntity;
import com.prottasha.habits.data.HabitVacationEntity;

@Database(
        entities = {
                LocalAccountMetadataEntity.class,
                ImportJournalEntity.class,
                IdentityEntity.class,
                SuccessMemoryEntity.class,
                HabitEntity.class,
                HabitSubstepEntity.class,
                HabitEventEntity.class,
                HabitDayAggregateEntity.class,
                HabitVacationEntity.class
        },
        version = 3,
        exportSchema = true)
public abstract class UserDatabase extends RoomDatabase {
    public abstract LocalAccountDao localAccountDao();
    public abstract ImportJournalDao importJournalDao();
    public abstract IdentityDao identityDao();
    public abstract HabitDao habitDao();
}
