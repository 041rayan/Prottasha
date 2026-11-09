package com.prottasha.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.prottasha.data.local.entity.IdentityEntity;
import java.util.List;

@Dao
public interface IdentityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(IdentityEntity identity);

    @Update
    void update(IdentityEntity identity);

    @Query("SELECT * FROM identities WHERE isActive = 1 LIMIT 1")
    LiveData<IdentityEntity> getActiveIdentity();

    @Query("SELECT * FROM identities WHERE isActive = 1 LIMIT 1")
    IdentityEntity getActiveIdentitySync();

    @Query("SELECT * FROM identities ORDER BY createdAt DESC")
    LiveData<List<IdentityEntity>> getAllIdentities();
}
