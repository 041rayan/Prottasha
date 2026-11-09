package com.prottasha.data.local.user.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.prottasha.data.local.user.entity.LocalAccountMetadataEntity;

@Dao
public interface LocalAccountDao {

    @Nullable
    @Query("SELECT * FROM local_account_metadata WHERE id = 1")
    LocalAccountMetadataEntity findAccount();

    @Query("SELECT * FROM local_account_metadata WHERE id = 1")
    LocalAccountMetadataEntity requireAccount();

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(LocalAccountMetadataEntity account);

    @Query("UPDATE local_account_metadata SET onboardingVersion = :version WHERE id = 1")
    void updateOnboardingVersion(int version);
}
