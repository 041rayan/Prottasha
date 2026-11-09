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

@Database(
        entities = {
                LocalAccountMetadataEntity.class,
                ImportJournalEntity.class,
                IdentityEntity.class,
                SuccessMemoryEntity.class
        },
        version = 2,
        exportSchema = true)
public abstract class UserDatabase extends RoomDatabase {
    public abstract LocalAccountDao localAccountDao();
    public abstract ImportJournalDao importJournalDao();
    public abstract IdentityDao identityDao();
}
