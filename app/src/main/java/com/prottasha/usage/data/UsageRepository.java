package com.prottasha.usage.data;

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
        disk.execute(() -> provider.requireOpen().usageDao().upsert(new UsageDailyEntity(
                target, epochDay, summary.totalMillis, summary.quality.name())));
    }

    public List<UsageDailyEntity> listDay(long epochDay) {
        return provider.requireOpen().usageDao().listDay(epochDay);
    }
}
