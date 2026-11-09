package com.prottasha.data.local.user;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.prottasha.data.local.user.dao.ImportJournalDao;
import com.prottasha.data.local.user.dao.LocalAccountDao;
import com.prottasha.data.local.user.entity.ImportJournalEntity;
import com.prottasha.data.local.user.entity.LocalAccountMetadataEntity;

@Database(
        entities = {
                LocalAccountMetadataEntity.class,
                ImportJournalEntity.class
        },
        version = 1,
        exportSchema = true)
public abstract class UserDatabase extends RoomDatabase {
    public abstract LocalAccountDao localAccountDao();
    public abstract ImportJournalDao importJournalDao();
}
