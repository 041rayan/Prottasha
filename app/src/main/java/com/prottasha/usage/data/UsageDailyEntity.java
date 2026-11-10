package com.prottasha.usage.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "usage_daily", primaryKeys = {"target", "epochDay"})
public final class UsageDailyEntity {
    @NonNull public final String target;
    public final long epochDay;
    public final long totalMillis;
    @NonNull public final String quality;
    public final int opens;
    public final int sessions;

    public UsageDailyEntity(@NonNull String target, long epochDay,
            long totalMillis, @NonNull String quality, int opens, int sessions) {
        this.target = target;
        this.epochDay = epochDay;
        this.totalMillis = totalMillis;
        this.quality = quality;
        this.opens = opens;
        this.sessions = sessions;
    }

    @Ignore
    public UsageDailyEntity(@NonNull String target, long epochDay,
            long totalMillis, @NonNull String quality) {
        this(target, epochDay, totalMillis, quality, 0, 0);
    }
}
