package com.prottasha.identity.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface IdentityDao {

    @Query("SELECT * FROM identities WHERE active = 1 ORDER BY createdAt DESC LIMIT 1")
    IdentityEntity findActive();

    @Query("SELECT * FROM identities WHERE active = 1 ORDER BY createdAt DESC LIMIT 1")
    LiveData<IdentityEntity> observeActive();

    @Query("UPDATE identities SET active = 0 WHERE active = 1")
    void deactivateAll();

    @Insert
    long insert(IdentityEntity identity);
}
