package com.prottasha.identity.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "identities")
public final class IdentityEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    @NonNull
    public final String statement;
    @NonNull
    public final String purpose;
    public final long createdAt;
    public final boolean active;

    public IdentityEntity(
            long id,
            @NonNull String statement,
            @NonNull String purpose,
            long createdAt,
            boolean active) {
        this.id = id;
        this.statement = statement;
        this.purpose = purpose;
        this.createdAt = createdAt;
        this.active = active;
    }
}
