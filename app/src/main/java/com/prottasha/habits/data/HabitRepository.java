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
import com.prottasha.habits.domain.HabitStatsCalculator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public class HabitRepository {

    private final UserSessionDatabaseProvider databaseProvider;
    private final Executor diskExecutor;
    private final MutableLiveData<List<HabitModels.HabitTodayItem>> today =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<HabitModels.HabitSnapshot>> activeHabits =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<HabitModels.HabitSnapshot>> archivedHabits =
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

    public LiveData<List<HabitModels.HabitSnapshot>> observeActive() {
        refreshHabitLists();
        return activeHabits;
    }

    public LiveData<List<HabitModels.HabitSnapshot>> observeArchived() {
        refreshHabitLists();
        return archivedHabits;
    }

    public void getHabit(long habitId, ResultCallback<HabitModels.HabitSnapshot> callback) {
        diskExecutor.execute(() -> {
            try {
                HabitEntity entity = databaseProvider.requireOpen().habitDao().find(habitId);
                if (entity == null) {
                    callback.onResult(AppResult.failure(new IllegalArgumentException("Habit not found")));
                } else {
                    callback.onResult(AppResult.success(snapshot(entity)));
                }
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
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
                refreshHabitLists();
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

    public void backfill(
            long habitId,
            long epochDay,
            HabitAction.Kind kind,
            int amount,
            ResultCallback<HabitModels.CompletionResult> callback) {
        HabitAction action;
        String nonce = UUID.randomUUID().toString();
        if (kind == HabitAction.Kind.LAPSE) {
            action = HabitAction.lapse(nonce);
        } else if (kind == HabitAction.Kind.INCREMENT) {
            action = HabitAction.increment(nonce, amount);
        } else {
            action = HabitAction.complete(nonce);
        }
        record(action.forHabit(habitId, epochDay), callback);
    }

    public void archive(long habitId) {
        setArchived(habitId, true);
    }

    public void restore(long habitId) {
        setArchived(habitId, false);
    }

    private void setArchived(long habitId, boolean archived) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().setArchived(habitId, archived);
                refresh(observedEpochDay);
                refreshHabitLists();
            } catch (IllegalStateException ignored) {}
        });
    }

    public void reorder(long habitId, int sortOrder) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().setSortOrder(habitId, sortOrder);
                refresh(observedEpochDay);
                refreshHabitLists();
            } catch (IllegalStateException ignored) {}
        });
    }

    public void undo(long habitId, long eventId,
            ResultCallback<HabitModels.CompletionResult> callback) {
        record(HabitAction.undo(UUID.randomUUID().toString(), eventId)
                .forHabit(habitId, observedEpochDay), callback);
    }

    public void delete(long habitId) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().delete(habitId);
                refresh(observedEpochDay);
                refreshHabitLists();
            } catch (IllegalStateException ignored) {}
        });
    }

    public void computeStats(long habitId, ResultCallback<HabitModels.HabitStats> callback) {
        diskExecutor.execute(() -> {
            try {
                HabitDao dao = databaseProvider.requireOpen().habitDao();
                HabitEntity entity = dao.find(habitId);
                if (entity == null) {
                    callback.onResult(AppResult.failure(new IllegalArgumentException("Habit not found")));
                    return;
                }
                long todayDay = LocalDate.now().toEpochDay();
                long startDay = Math.max(entity.startEpochDay, todayDay - 365); // look back up to 1 year
                List<HabitDayAggregateEntity> aggregates = dao.listAggregatesBetween(habitId, startDay, todayDay);
                List<HabitVacationEntity> vacations = dao.listVacations(habitId);
                HabitSchedule schedule = schedule(entity);

                List<Boolean> completedDueDays = new ArrayList<>();
                Long lastDoneDay = null;

                for (long day = startDay; day <= todayDay; day++) {
                    if (HabitScheduleEvaluator.isDue(schedule, day, lastDoneDay, vacations)) {
                        boolean completed = false;
                        for (HabitDayAggregateEntity agg : aggregates) {
                            if (agg.epochDay == day && agg.completed) {
                                completed = true;
                                lastDoneDay = day;
                                break;
                            }
                        }
                        completedDueDays.add(completed);
                    }
                }
                HabitModels.HabitStats stats = HabitStatsCalculator.calculate(completedDueDays);
                callback.onResult(AppResult.success(stats));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void getSubsteps(long habitId, ResultCallback<List<HabitSubstepEntity>> callback) {
        diskExecutor.execute(() -> {
            try {
                List<HabitSubstepEntity> substeps = databaseProvider.requireOpen()
                        .habitDao().listSubsteps(habitId);
                callback.onResult(AppResult.success(substeps));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void saveSubstep(long habitId, String title, int sortOrder, ResultCallback<Long> callback) {
        diskExecutor.execute(() -> {
            try {
                long id = databaseProvider.requireOpen().habitDao()
                        .insertSubstep(new HabitSubstepEntity(0L, habitId, title.trim(), sortOrder));
                callback.onResult(AppResult.success(id));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void deleteSubstep(long substepId) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().deleteSubstep(substepId);
            } catch (Exception ignored) {}
        });
    }

    public void getVacations(long habitId, ResultCallback<List<HabitVacationEntity>> callback) {
        diskExecutor.execute(() -> {
            try {
                List<HabitVacationEntity> vacations = databaseProvider.requireOpen()
                        .habitDao().listVacations(habitId);
                callback.onResult(AppResult.success(vacations));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void addVacation(long habitId, long startEpochDay, long endEpochDay, ResultCallback<Boolean> callback) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao()
                        .insertVacation(new HabitVacationEntity(habitId, startEpochDay, endEpochDay));
                refresh(observedEpochDay);
                callback.onResult(AppResult.success(true));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void removeVacation(long habitId, long startEpochDay) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().deleteVacation(habitId, startEpochDay);
                refresh(observedEpochDay);
            } catch (Exception ignored) {}
        });
    }

    public void getAggregates(long habitId, int limit, ResultCallback<List<HabitDayAggregateEntity>> callback) {
        diskExecutor.execute(() -> {
            try {
                List<HabitDayAggregateEntity> list = databaseProvider.requireOpen()
                        .habitDao().listAggregates(habitId, limit);
                callback.onResult(AppResult.success(list));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void getNote(long habitId, long epochDay, ResultCallback<String> callback) {
        diskExecutor.execute(() -> {
            try {
                HabitDayNoteEntity note = databaseProvider.requireOpen()
                        .habitDao().findNote(habitId, epochDay);
                callback.onResult(AppResult.success(note == null ? "" : note.note));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void saveNote(long habitId, long epochDay, String text, ResultCallback<Boolean> callback) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().upsertNote(
                        new HabitDayNoteEntity(habitId, epochDay, text == null ? "" : text.trim(), System.currentTimeMillis()));
                callback.onResult(AppResult.success(true));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void getReminders(long habitId, ResultCallback<List<HabitReminderEntity>> callback) {
        diskExecutor.execute(() -> {
            try {
                List<HabitReminderEntity> reminders =
                        databaseProvider.requireOpen().habitDao().listRemindersForHabit(habitId);
                callback.onResult(AppResult.success(reminders));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void saveReminder(HabitReminderEntity reminder, ResultCallback<Long> callback) {
        diskExecutor.execute(() -> {
            try {
                long id = databaseProvider.requireOpen().habitDao().upsertReminder(reminder);
                callback.onResult(AppResult.success(id));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void deleteReminder(long reminderId, ResultCallback<Boolean> callback) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().deleteReminder(reminderId);
                callback.onResult(AppResult.success(true));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void listDayImages(long habitId, long epochDay, ResultCallback<List<HabitDayImageEntity>> callback) {
        diskExecutor.execute(() -> {
            try {
                List<HabitDayImageEntity> images =
                        databaseProvider.requireOpen().habitDao().listImagesForDay(habitId, epochDay);
                callback.onResult(AppResult.success(images));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void saveDayImage(long habitId, long epochDay, String storageId, ResultCallback<Long> callback) {
        diskExecutor.execute(() -> {
            try {
                long id = databaseProvider.requireOpen().habitDao().insertImage(
                        new HabitDayImageEntity(0L, habitId, epochDay, storageId));
                callback.onResult(AppResult.success(id));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    public void deleteDayImage(long habitId, String storageId, ResultCallback<Boolean> callback) {
        diskExecutor.execute(() -> {
            try {
                databaseProvider.requireOpen().habitDao().deleteImageByStorageId(habitId, storageId);
                callback.onResult(AppResult.success(true));
            } catch (Exception error) {
                callback.onResult(AppResult.failure(error));
            }
        });
    }

    private void refresh(long epochDay) {
        if (databaseProvider == null || epochDay == 0L) {
            return;
        }
        diskExecutor.execute(() -> {
            try {
                List<HabitModels.HabitTodayItem> items = new ArrayList<>();
                HabitDao dao = databaseProvider.requireOpen().habitDao();
                for (HabitEntity entity : dao.listActive()) {
                    HabitSchedule schedule = schedule(entity);
                    List<HabitVacationEntity> vacations = dao.listVacations(entity.id);
                    if (!HabitScheduleEvaluator.isDue(schedule, epochDay, null, vacations)) {
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

    private void refreshHabitLists() {
        if (databaseProvider == null) {
            return;
        }
        diskExecutor.execute(() -> {
            try {
                HabitDao dao = databaseProvider.requireOpen().habitDao();
                List<HabitModels.HabitSnapshot> active = new ArrayList<>();
                for (HabitEntity entity : dao.listActive()) {
                    active.add(snapshot(entity));
                }
                activeHabits.postValue(Collections.unmodifiableList(active));

                List<HabitModels.HabitSnapshot> archived = new ArrayList<>();
                for (HabitEntity entity : dao.listArchived()) {
                    archived.add(snapshot(entity));
                }
                archivedHabits.postValue(Collections.unmodifiableList(archived));
            } catch (IllegalStateException ignored) {}
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
        return new HabitEntity(
                snapshot.id,
                snapshot.title.trim(),
                snapshot.type.name(),
                snapshot.target,
                snapshot.schedule.frequency.name(),
                snapshot.schedule.startEpochDay,
                snapshot.schedule.interval,
                snapshot.schedule.dayOfMonth,
                snapshot.archived,
                snapshot.sortOrder,
                System.currentTimeMillis(),
                snapshot.cue,
                snapshot.twoMinuteAction,
                snapshot.reward,
                snapshot.ifThenPlan,
                snapshot.reboundPlan,
                snapshot.unit,
                snapshot.incrementVal,
                snapshot.maximumVal,
                snapshot.focusMinutes,
                snapshot.accountability);
    }

    public static HabitModels.HabitSnapshot snapshot(HabitEntity entity) {
        return new HabitModels.HabitSnapshot(
                entity.id,
                entity.title,
                HabitModels.Type.valueOf(entity.type),
                entity.target,
                schedule(entity),
                entity.cue,
                entity.twoMinuteAction,
                entity.reward,
                entity.ifThenPlan,
                entity.reboundPlan,
                entity.unit,
                entity.incrementVal,
                entity.maximumVal,
                entity.focusMinutes,
                entity.accountability,
                entity.archived,
                entity.sortOrder);
    }

    public static HabitSchedule schedule(HabitEntity entity) {
        HabitSchedule.Frequency frequency = HabitSchedule.Frequency.valueOf(
                entity.scheduleFrequency);
        switch (frequency) {
            case WEEKDAYS:
                return HabitSchedule.weekdays(entity.startEpochDay);
            case EVERY_N_DAYS:
                return HabitSchedule.everyNDays(entity.startEpochDay, entity.scheduleInterval);
            case EVERY_N_WEEKS:
                return HabitSchedule.everyNWeeks(entity.startEpochDay, entity.scheduleInterval);
            case EVERY_N_MONTHS:
                return HabitSchedule.everyNMonths(entity.startEpochDay,
                        entity.scheduleInterval, entity.scheduleDayOfMonth);
            case FROM_LAST_DONE:
                return HabitSchedule.fromLastDone(entity.startEpochDay, entity.scheduleInterval);
            case X_PER_WEEK:
                return HabitSchedule.xPerWeek(entity.startEpochDay, entity.scheduleInterval);
            case X_PER_MONTH:
                return HabitSchedule.xPerMonth(entity.startEpochDay, entity.scheduleInterval);
            default:
                return HabitSchedule.daily(entity.startEpochDay);
        }
    }
}
