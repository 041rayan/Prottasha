package com.prottasha.habits.ui;

import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.domain.HabitSchedule;
import com.prottasha.habits.domain.HabitScheduleEvaluator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class HabitConsistencyGridBuilder {

    private HabitConsistencyGridBuilder() {
    }

    public static HabitConsistencyGridState build(
            List<HabitEntity> habits,
            List<HabitDayAggregateEntity> aggregates,
            LocalDate today,
            int weeks) {
        Objects.requireNonNull(today, "today");
        if (weeks < 1) {
            throw new IllegalArgumentException("weeks must be positive");
        }

        List<HabitEntity> activeHabits = new ArrayList<>();
        Map<Long, HabitSchedule> schedules = new HashMap<>();
        for (HabitEntity habit : habits == null ? Collections.<HabitEntity>emptyList() : habits) {
            if (!habit.archived) {
                activeHabits.add(habit);
                schedules.put(habit.id, HabitRepository.schedule(habit));
            }
        }

        Map<Long, Map<Long, HabitDayAggregateEntity>> aggregateByHabitAndDay = new HashMap<>();
        for (HabitDayAggregateEntity aggregate : aggregates == null
                ? Collections.<HabitDayAggregateEntity>emptyList() : aggregates) {
            aggregateByHabitAndDay
                    .computeIfAbsent(aggregate.habitId, ignored -> new HashMap<>())
                    .put(aggregate.epochDay, aggregate);
        }

        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(weeks - 1L);
        int cellCount = weeks * 7;
        Map<Long, Long> lastDoneByHabit = lastCompletionsBefore(
                activeHabits, aggregateByHabitAndDay, start.toEpochDay());

        List<HabitConsistencyGridState.Cell> cells = new ArrayList<>(cellCount);
        int activeDays = 0;
        int currentRun = 0;
        int bestRun = 0;
        int scheduledTotal = 0;
        int completedTotal = 0;

        for (int offset = 0; offset < cellCount; offset++) {
            LocalDate date = start.plusDays(offset);
            long epochDay = date.toEpochDay();
            int scheduled = 0;
            int completed = 0;

            if (!date.isAfter(today)) {
                for (HabitEntity habit : activeHabits) {
                    Long lastDone = lastDoneByHabit.get(habit.id);
                    if (!HabitScheduleEvaluator.isDue(
                            schedules.get(habit.id), epochDay, lastDone)) {
                        continue;
                    }
                    scheduled++;
                    HabitDayAggregateEntity aggregate = aggregate(
                            aggregateByHabitAndDay, habit.id, epochDay);
                    if (isSuccessful(aggregate)) {
                        completed++;
                    }
                }

                for (HabitEntity habit : activeHabits) {
                    HabitDayAggregateEntity aggregate = aggregate(
                            aggregateByHabitAndDay, habit.id, epochDay);
                    if (isSuccessful(aggregate)) {
                        lastDoneByHabit.put(habit.id, epochDay);
                    }
                }
            }

            cells.add(new HabitConsistencyGridState.Cell(
                    date,
                    scheduled,
                    completed,
                    intensity(scheduled, completed),
                    date.equals(today)));

            if (scheduled > 0) {
                scheduledTotal += scheduled;
                completedTotal += completed;
                if (completed > 0) {
                    activeDays++;
                    currentRun++;
                    bestRun = Math.max(bestRun, currentRun);
                } else {
                    currentRun = 0;
                }
            }
        }

        boolean hasData = scheduledTotal > 0;
        int consistency = hasData
                ? (int) Math.round(completedTotal * 100.0 / scheduledTotal)
                : 0;
        return new HabitConsistencyGridState(
                cells,
                weeks,
                hasData,
                activeDays,
                currentRun,
                bestRun,
                consistency,
                summary(hasData, weeks, activeDays, currentRun, bestRun, consistency));
    }

    private static Map<Long, Long> lastCompletionsBefore(
            List<HabitEntity> habits,
            Map<Long, Map<Long, HabitDayAggregateEntity>> aggregates,
            long beforeEpochDay) {
        Map<Long, Long> result = new HashMap<>();
        for (HabitEntity habit : habits) {
            Map<Long, HabitDayAggregateEntity> byDay = aggregates.get(habit.id);
            if (byDay == null) continue;
            for (HabitDayAggregateEntity aggregate : byDay.values()) {
                if (aggregate.epochDay < beforeEpochDay
                        && isSuccessful(aggregate)
                        && aggregate.epochDay > result.getOrDefault(habit.id, Long.MIN_VALUE)) {
                    result.put(habit.id, aggregate.epochDay);
                }
            }
        }
        return result;
    }

    private static HabitDayAggregateEntity aggregate(
            Map<Long, Map<Long, HabitDayAggregateEntity>> aggregates,
            long habitId,
            long epochDay) {
        Map<Long, HabitDayAggregateEntity> byDay = aggregates.get(habitId);
        return byDay == null ? null : byDay.get(epochDay);
    }

    private static boolean isSuccessful(HabitDayAggregateEntity aggregate) {
        return aggregate != null && aggregate.completed && !aggregate.lapsed;
    }

    private static int intensity(int scheduled, int completed) {
        if (scheduled == 0) return 0;
        if (completed == 0) return 1;
        if (completed == scheduled) return 4;
        return completed * 2 <= scheduled ? 2 : 3;
    }

    private static String summary(
            boolean hasData,
            int weeks,
            int activeDays,
            int currentRun,
            int bestRun,
            int consistency) {
        if (!hasData) {
            return "No scheduled habit data in the last " + weeks + " weeks.";
        }
        return String.format(
                Locale.ROOT,
                "Habit consistency: %d active days, %d%% consistency, current run %d %s, best run %d %s.",
                activeDays,
                consistency,
                currentRun,
                currentRun == 1 ? "day" : "days",
                bestRun,
                bestRun == 1 ? "day" : "days");
    }
}
