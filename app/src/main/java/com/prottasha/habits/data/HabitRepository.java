package com.prottasha.habits.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitCompletionEngine;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;
import com.prottasha.habits.domain.HabitScheduleEvaluator;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public final class HabitRepository {

    private final UserSessionDatabaseProvider databaseProvider;
    private final Executor diskExecutor;
    private final MutableLiveData<List<HabitModels.HabitTodayItem>> today =
            new MutableLiveData<>(Collections.emptyList());
    private volatile long observedEpochDay;

    public HabitRepository(UserSessionDatabaseProvider databaseProvider, Executor diskExecutor) {
        this.databaseProvider = databaseProvider;
        this.diskExecutor = diskExecutor;
    }

    public LiveData<List<HabitModels.HabitTodayItem>> observeDueOn(long epochDay) {
        observedEpochDay = epochDay;
        refresh(epochDay);
        return today;
    }

    public void save(HabitModels.HabitSnapshot snapshot, ResultCallback<Long> callback) {
        diskExecutor.execute(() -> {
            try {
                HabitEntity entity = toEntity(snapshot);
                UserDatabase database = databaseProvider.requireOpen();
                long id;
                if (snapshot.id == 0L) {
                    id = database.habitDao().insert(entity);
                } else {
                    database.habitDao().update(entity);
                    id = snapshot.id;
                }
                refresh(observedEpochDay);
                callback.onResult(AppResult.success(id));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void record(HabitAction action, ResultCallback<HabitModels.CompletionResult> callback) {
        diskExecutor.execute(() -> {
            try {
                UserDatabase database = databaseProvider.requireOpen();
                final HabitModels.CompletionResult[] result = new HabitModels.CompletionResult[1];
                database.runInTransaction(() -> {
                    HabitDao dao = database.habitDao();
                    HabitEventEntity duplicate = dao.findEventByNonce(action.nonce);
                    if (duplicate != null) {
                        result[0] = fromAggregate(
                                dao.findAggregate(duplicate.habitId, duplicate.epochDay),
                                duplicate.id);
                        return;
                    }
                    HabitEntity habit = dao.find(action.habitId);
                    if (habit == null) {
                        throw new IllegalArgumentException("Habit does not exist");
                    }
                    HabitDayAggregateEntity current = dao.findAggregate(
                            action.habitId, action.epochDay);
                    HabitModels.CompletionResult projected = HabitCompletionEngine.apply(
                            HabitModels.Type.valueOf(habit.type), habit.target,
                            current == null ? 0 : current.value, action);
                    long eventId = dao.insertEvent(new HabitEventEntity(
                            0L, action.habitId, action.epochDay, action.kind.name(),
                            action.amount, action.nonce, action.revertedEventId,
                            System.currentTimeMillis(), ZoneId.systemDefault().getId(), 0));
                    dao.upsertAggregate(new HabitDayAggregateEntity(
                            action.habitId, action.epochDay, projected.value,
                            projected.completed, projected.lapsed, System.currentTimeMillis()));
                    result[0] = new HabitModels.CompletionResult(
                            projected.value, projected.completed, projected.lapsed, eventId);
                });
                refresh(observedEpochDay);
                callback.onResult(AppResult.success(result[0]));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void archive(long habitId) {
        setArchived(habitId, true);
    }

    public void restore(long habitId) {
        setArchived(habitId, false);
    }

    private void setArchived(long habitId, boolean archived) {
        diskExecutor.execute(() -> {
            databaseProvider.requireOpen().habitDao().setArchived(habitId, archived);
            refresh(observedEpochDay);
        });
    }

    public void reorder(long habitId, int sortOrder) {
        diskExecutor.execute(() -> {
            databaseProvider.requireOpen().habitDao().setSortOrder(habitId, sortOrder);
            refresh(observedEpochDay);
        });
    }

    public void undo(long habitId, long eventId,
            ResultCallback<HabitModels.CompletionResult> callback) {
        record(HabitAction.undo(UUID.randomUUID().toString(), eventId)
                .forHabit(habitId, observedEpochDay), callback);
    }

    public void delete(long habitId) {
        diskExecutor.execute(() -> {
            databaseProvider.requireOpen().habitDao().delete(habitId);
            refresh(observedEpochDay);
        });
    }

    private void refresh(long epochDay) {
        if (epochDay == 0L) {
            return;
        }
        diskExecutor.execute(() -> {
            try {
                List<HabitModels.HabitTodayItem> items = new ArrayList<>();
                HabitDao dao = databaseProvider.requireOpen().habitDao();
                for (HabitEntity entity : dao.listActive()) {
                    HabitSchedule schedule = schedule(entity);
                    if (!HabitScheduleEvaluator.isDue(schedule, epochDay, null)) {
                        continue;
                    }
                    HabitDayAggregateEntity aggregate = dao.findAggregate(entity.id, epochDay);
                    HabitModels.CompletionResult completion = fromAggregate(aggregate, 0L);
                    items.add(new HabitModels.HabitTodayItem(
                            snapshot(entity), completion.value,
                            completion.completed, completion.lapsed));
                }
                today.postValue(Collections.unmodifiableList(items));
            } catch (IllegalStateException staleSession) {
                // Sign-out or account switch closed this UID while the refresh was queued.
            }
        });
    }

    private static HabitModels.CompletionResult fromAggregate(
            HabitDayAggregateEntity aggregate, long eventId) {
        return aggregate == null
                ? new HabitModels.CompletionResult(0, false, false, eventId)
                : new HabitModels.CompletionResult(
                        aggregate.value, aggregate.completed, aggregate.lapsed, eventId);
    }

    private static HabitEntity toEntity(HabitModels.HabitSnapshot snapshot) {
        return new HabitEntity(snapshot.id, snapshot.title.trim(), snapshot.type.name(),
                snapshot.target, snapshot.schedule.frequency.name(),
                snapshot.schedule.startEpochDay, snapshot.schedule.interval,
                snapshot.schedule.dayOfMonth, false, 0, System.currentTimeMillis());
    }

    private static HabitModels.HabitSnapshot snapshot(HabitEntity entity) {
        return new HabitModels.HabitSnapshot(entity.id, entity.title,
                HabitModels.Type.valueOf(entity.type), entity.target, schedule(entity));
    }

    private static HabitSchedule schedule(HabitEntity entity) {
        HabitSchedule.Frequency frequency = HabitSchedule.Frequency.valueOf(
                entity.scheduleFrequency);
        switch (frequency) {
            case WEEKDAYS:
                return HabitSchedule.weekdays(entity.startEpochDay);
            case EVERY_N_DAYS:
                return HabitSchedule.everyNDays(entity.startEpochDay, entity.scheduleInterval);
            case EVERY_N_MONTHS:
                return HabitSchedule.everyNMonths(entity.startEpochDay,
                        entity.scheduleInterval, entity.scheduleDayOfMonth);
            case FROM_LAST_DONE:
                return HabitSchedule.fromLastDone(entity.startEpochDay, entity.scheduleInterval);
            default:
                return HabitSchedule.daily(entity.startEpochDay);
        }
    }
}
