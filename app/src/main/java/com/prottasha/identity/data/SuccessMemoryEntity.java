package com.prottasha.identity.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "success_memories",
        foreignKeys = @ForeignKey(
                entity = IdentityEntity.class,
                parentColumns = "id",
                childColumns = "identityId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("identityId"))
public final class SuccessMemoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public final long identityId;
    @NonNull
    public final String text;
    public final long createdAt;

    public SuccessMemoryEntity(long id, long identityId, @NonNull String text, long createdAt) {
        this.id = id;
        this.identityId = identityId;
        this.text = text;
        this.createdAt = createdAt;
    }
}
