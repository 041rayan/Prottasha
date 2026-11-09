package com.prottasha.data.local.user.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "local_account_metadata")
public final class LocalAccountMetadataEntity {

    @PrimaryKey
    public final int id;
    @NonNull
    public final String uidHash;
    public final long createdAt;
    public final int onboardingVersion;

    public LocalAccountMetadataEntity(
            int id,
            @NonNull String uidHash,
            long createdAt,
            int onboardingVersion) {
        this.id = id;
        this.uidHash = uidHash;
        this.createdAt = createdAt;
        this.onboardingVersion = onboardingVersion;
    }
}
