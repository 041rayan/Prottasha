package com.prottasha.usage.data;

import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.usage.domain.UsageAggregator;

import java.util.List;
import java.util.concurrent.Executor;

public final class UsageRepository {
    private final UserSessionDatabaseProvider provider;
    private final Executor disk;

    public UsageRepository(UserSessionDatabaseProvider provider, Executor disk) {
        this.provider = provider;
        this.disk = disk;
    }

    public void store(String target, long epochDay, UsageAggregator.Summary summary) {
        store(target, epochDay, summary.totalMillis, summary.quality.name(), 0, 0);
    }

    public void store(String target, long epochDay, long totalMillis, String quality, int opens, int sessions) {
        disk.execute(() -> {
            UserDatabase db = provider.requireOpen();
            db.usageDao().upsert(new UsageDailyEntity(
                    target, epochDay, totalMillis, quality, opens, sessions));
        });
    }

    public void recordDomainSession(String domain, long epochDay, long durationMillis) {
        disk.execute(() -> {
            UserDatabase db = provider.requireOpen();
            UsageDailyEntity existing = db.usageDao().find(domain, epochDay);
            if (existing != null) {
                db.usageDao().upsert(new UsageDailyEntity(
                        domain, epochDay, existing.totalMillis + durationMillis,
                        existing.quality, existing.opens + 1, existing.sessions + 1));
            } else {
                db.usageDao().upsert(new UsageDailyEntity(
                        domain, epochDay, durationMillis, UsageAggregator.Quality.COMPLETE.name(), 1, 1));
            }
        });
    }

    public List<UsageDailyEntity> listDay(long epochDay) {
        return provider.requireOpen().usageDao().listDay(epochDay);
    }

    public List<UsageDailyEntity> listRange(long startEpochDay, long endEpochDay) {
        return provider.requireOpen().usageDao().listRange(startEpochDay, endEpochDay);
    }
}
