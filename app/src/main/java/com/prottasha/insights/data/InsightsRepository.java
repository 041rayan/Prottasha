package com.prottasha.insights.data;

import androidx.annotation.Nullable;

import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.insights.domain.InsightsCalculator;
import com.prottasha.intervention.data.InterventionEventEntity;
import com.prottasha.usage.data.UsageDailyEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InsightsRepository {
    private final UserSessionDatabaseProvider provider;

    public InsightsRepository(UserSessionDatabaseProvider provider) {
        this.provider = provider;
    }

    public InsightsCalculator.Snapshot snapshot() {
        return snapshot(InsightsCalculator.Range.LAST_30_DAYS,
                LocalDate.now().toEpochDay(), ZoneId.systemDefault());
    }

    public InsightsCalculator.Snapshot snapshot(
            InsightsCalculator.Range range,
            long currentEpochDay,
            ZoneId zoneId) {
        UserDatabase db = provider.requireOpen();

        long startEpochDay;
        if (range == InsightsCalculator.Range.TODAY) {
            startEpochDay = currentEpochDay;
        } else if (range == InsightsCalculator.Range.LAST_7_DAYS) {
            startEpochDay = currentEpochDay - 6;
        } else {
            startEpochDay = currentEpochDay - 29;
        }

        long startMillis = LocalDate.ofEpochDay(startEpochDay).atStartOfDay(zoneId).toInstant().toEpochMilli();
        long endMillis = LocalDate.ofEpochDay(currentEpochDay + 1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1;

        List<InterventionEventEntity> interventions = db.interventionDao().listBetween(startMillis, endMillis);
        List<UsageDailyEntity> usageList = db.usageDao().listRange(startEpochDay, currentEpochDay);
        List<HabitDayAggregateEntity> habitAggregates = db.habitDao().listAllAggregatesBetween(startEpochDay, currentEpochDay);

        FocusSessionEntity focusEntity = db.focusDao().find();
        List<FocusSessionEntity> focusSessions = focusEntity != null
                ? Collections.singletonList(focusEntity)
                : Collections.emptyList();

        return InsightsCalculator.calculate(
                range, currentEpochDay, zoneId,
                interventions, usageList, habitAggregates, focusSessions);
    }

    public long saveReflection(long epochDay, String fact, String interpretation, String lesson) {
        return saveReflectionWithImage(epochDay, fact, interpretation, lesson, null);
    }

    public long saveReflectionWithImage(
            long epochDay, String fact, String interpretation, String lesson, @Nullable String storageId) {
        UserDatabase db = provider.requireOpen();
        final long[] id = new long[1];
        db.runInTransaction(() -> {
            id[0] = db.insightsDao().insert(new JournalEntryEntity(
                    0L, epochDay, fact, interpretation, lesson, System.currentTimeMillis()));
            if (storageId != null && !storageId.trim().isEmpty()) {
                db.insightsDao().insertImage(new JournalImageEntity(0L, id[0], storageId.trim()));
            }
        });
        return id[0];
    }

    public void updateReflection(
            long id, long epochDay, String fact, String interpretation, String lesson, @Nullable String storageId) {
        UserDatabase db = provider.requireOpen();
        db.runInTransaction(() -> {
            db.insightsDao().update(new JournalEntryEntity(
                    id, epochDay, fact, interpretation, lesson, System.currentTimeMillis()));
            if (storageId != null) {
                db.insightsDao().deleteImagesForEntry(id);
                if (!storageId.trim().isEmpty()) {
                    db.insightsDao().insertImage(new JournalImageEntity(0L, id, storageId.trim()));
                }
            }
        });
    }

    public void deleteReflection(long id) {
        provider.requireOpen().insightsDao().deleteById(id);
    }

    public List<JournalEntryEntity> listReflections(int limit) {
        return provider.requireOpen().insightsDao().recent(limit);
    }

    public List<JournalImageEntity> getImagesForReflection(long entryId) {
        return provider.requireOpen().insightsDao().findImagesForEntry(entryId);
    }

    public long recordRehearsal(String scenario, int durationSeconds, int feltIntensity) {
        return provider.requireOpen().rehearsalDao().insert(new RehearsalSessionEntity(
                0L, scenario, durationSeconds, feltIntensity, System.currentTimeMillis()));
    }

    public List<RehearsalSessionEntity> listRehearsals() {
        return provider.requireOpen().rehearsalDao().list();
    }

    public int rehearsalCount() {
        return provider.requireOpen().rehearsalDao().count();
    }
}
