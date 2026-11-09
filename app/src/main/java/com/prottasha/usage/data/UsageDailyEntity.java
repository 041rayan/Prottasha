package com.prottasha.usage.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "usage_daily", primaryKeys = {"target", "epochDay"})
public final class UsageDailyEntity {
    @NonNull public final String target;
    public final long epochDay;
    public final long totalMillis;
    @NonNull public final String quality;

    public UsageDailyEntity(@NonNull String target, long epochDay,
            long totalMillis, @NonNull String quality) {
        this.target = target;
        this.epochDay = epochDay;
        this.totalMillis = totalMillis;
        this.quality = quality;
    }
}
