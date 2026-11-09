package com.prottasha.insights.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "reflection_images",
        foreignKeys = @ForeignKey(entity = JournalEntryEntity.class, parentColumns = "id",
                childColumns = "entryId", onDelete = ForeignKey.CASCADE),
        indices = @Index("entryId"))
public final class JournalImageEntity {
    @PrimaryKey(autoGenerate = true) public long id;
    public final long entryId;
    @NonNull public final String storageId;

    public JournalImageEntity(long id, long entryId, @NonNull String storageId) {
        this.id = id;
        this.entryId = entryId;
        this.storageId = storageId;
    }
}
