package com.prottasha.insights.data;

import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.insights.domain.InsightsCalculator;
import com.prottasha.intervention.data.InterventionEventEntity;

import java.util.ArrayList;
import java.util.List;

public final class InsightsRepository {
    private final UserSessionDatabaseProvider provider;

    public InsightsRepository(UserSessionDatabaseProvider provider) {
        this.provider = provider;
    }

    public InsightsCalculator.Snapshot snapshot() {
        List<InsightsCalculator.Outcome> outcomes = new ArrayList<>();
        for (InterventionEventEntity event : provider.requireOpen().interventionDao().recent(30)) {
            try { outcomes.add(InsightsCalculator.Outcome.valueOf(event.type)); }
            catch (IllegalArgumentException ignored) { }
        }
        return InsightsCalculator.calculate(outcomes);
    }

    public long saveReflection(long epochDay, String fact, String interpretation, String lesson) {
        return provider.requireOpen().insightsDao().insert(new JournalEntryEntity(
                0L, epochDay, fact, interpretation, lesson, System.currentTimeMillis()));
    }
}
