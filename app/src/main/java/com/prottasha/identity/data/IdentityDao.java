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

    @Insert
    long insertSuccessMemory(SuccessMemoryEntity memory);

    @Query("SELECT * FROM success_memories WHERE identityId = :identityId ORDER BY createdAt DESC")
    java.util.List<SuccessMemoryEntity> findMemoriesForIdentity(long identityId);

    @Query("SELECT * FROM success_memories WHERE identityId = :identityId ORDER BY createdAt DESC")
    LiveData<java.util.List<SuccessMemoryEntity>> observeMemoriesForIdentity(long identityId);

    @Query("SELECT * FROM success_memories ORDER BY createdAt DESC LIMIT 1")
    LiveData<SuccessMemoryEntity> observeLatestSuccessMemory();

    @Query("SELECT * FROM success_memories ORDER BY createdAt DESC LIMIT 1")
    SuccessMemoryEntity findLatestSuccessMemory();
}
