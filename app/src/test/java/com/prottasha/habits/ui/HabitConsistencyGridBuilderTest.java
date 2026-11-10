package com.prottasha.habits.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

public final class HabitConsistencyGridBuilderTest {

    @Test
    public void emptyGridIsTwelveMondayAlignedWeeksWithOneCurrentDay() {
        LocalDate today = LocalDate.of(2026, 11, 10);

        HabitConsistencyGridState state = HabitConsistencyGridBuilder.build(
                Collections.emptyList(), Collections.emptyList(), today, 12);

        assertEquals(84, state.cells.size());
        assertEquals(LocalDate.of(2026, 8, 24), state.cells.get(0).date);
        assertEquals(LocalDate.of(2026, 11, 15), state.cells.get(83).date);
        assertEquals(1, state.cells.stream().filter(cell -> cell.currentDay).count());
        assertTrue(cell(state, today).currentDay);
        assertFalse(state.hasData);
        assertEquals(0, state.consistencyPercent);
        assertTrue(state.accessibilitySummary.contains("No scheduled habit data"));
    }

    @Test
    public void scheduleDistinguishesDueDaysFromUnscheduledDays() {
        LocalDate monday = LocalDate.of(2026, 11, 9);
        LocalDate sunday = monday.plusDays(6);
        HabitEntity weekdayHabit = habit(
                1L, HabitModels.Type.BUILD, 1, HabitSchedule.Frequency.WEEKDAYS, monday);

        HabitConsistencyGridState state = HabitConsistencyGridBuilder.build(
                Collections.singletonList(weekdayHabit), Collections.emptyList(), sunday, 1);

        for (int day = 0; day < 5; day++) {
            assertEquals(1, cell(state, monday.plusDays(day)).scheduledCount);
        }
        assertEquals(0, cell(state, monday.plusDays(5)).scheduledCount);
        assertEquals(0, cell(state, sunday).scheduledCount);
        assertTrue(state.hasData);
        assertEquals(0, state.consistencyPercent);
    }

    @Test
    public void persistedBuildAmountAndAvoidResultsProduceTruthfulIntensity() {
        LocalDate monday = LocalDate.of(2026, 11, 9);
        LocalDate tuesday = monday.plusDays(1);
        HabitEntity build = habit(
                1L, HabitModels.Type.BUILD, 1, HabitSchedule.Frequency.DAILY, monday);
        HabitEntity amount = habit(
                2L, HabitModels.Type.AMOUNT, 3, HabitSchedule.Frequency.DAILY, monday);
        HabitEntity avoid = habit(
                3L, HabitModels.Type.AVOID, 1, HabitSchedule.Frequency.DAILY, monday);

        HabitConsistencyGridState state = HabitConsistencyGridBuilder.build(
                Arrays.asList(build, amount, avoid),
                Arrays.asList(
                        aggregate(1L, monday, 1, true, false),
                        aggregate(2L, monday, 2, false, false),
                        aggregate(3L, monday, 0, false, true),
                        aggregate(1L, tuesday, 1, true, false),
                        aggregate(2L, tuesday, 3, true, false),
                        aggregate(3L, tuesday, 1, true, false)),
                tuesday,
                1);

        HabitConsistencyGridState.Cell mondayCell = cell(state, monday);
        assertEquals(3, mondayCell.scheduledCount);
        assertEquals(1, mondayCell.completedCount);
        assertEquals(2, mondayCell.intensity);

        HabitConsistencyGridState.Cell tuesdayCell = cell(state, tuesday);
        assertEquals(3, tuesdayCell.scheduledCount);
        assertEquals(3, tuesdayCell.completedCount);
        assertEquals(4, tuesdayCell.intensity);

        assertEquals(2, state.activeDays);
        assertEquals(67, state.consistencyPercent);
    }

    @Test
    public void avoidHabitRequiresARecordedCleanCheckIn() {
        LocalDate monday = LocalDate.of(2026, 11, 9);
        LocalDate tuesday = monday.plusDays(1);
        HabitEntity avoid = habit(
                7L, HabitModels.Type.AVOID, 1, HabitSchedule.Frequency.DAILY, monday);

        HabitConsistencyGridState state = HabitConsistencyGridBuilder.build(
                Collections.singletonList(avoid),
                Collections.singletonList(aggregate(7L, tuesday, 1, true, false)),
                tuesday,
                1);

        assertEquals(0, cell(state, monday).completedCount);
        assertEquals(1, cell(state, monday).intensity);
        assertEquals(1, cell(state, tuesday).completedCount);
        assertEquals(4, cell(state, tuesday).intensity);
        assertEquals(50, state.consistencyPercent);
    }

    @Test
    public void summariesTrackActiveDaysCurrentRunAndBestRun() {
        LocalDate monday = LocalDate.of(2026, 11, 9);
        LocalDate saturday = monday.plusDays(5);
        HabitEntity daily = habit(
                9L, HabitModels.Type.BUILD, 1, HabitSchedule.Frequency.DAILY, monday);

        HabitConsistencyGridState state = HabitConsistencyGridBuilder.build(
                Collections.singletonList(daily),
                Arrays.asList(
                        aggregate(9L, monday, 1, true, false),
                        aggregate(9L, monday.plusDays(1), 1, true, false),
                        aggregate(9L, monday.plusDays(2), 1, true, false),
                        aggregate(9L, monday.plusDays(4), 1, true, false),
                        aggregate(9L, saturday, 1, true, false)),
                saturday,
                1);

        assertEquals(5, state.activeDays);
        assertEquals(2, state.currentRun);
        assertEquals(3, state.bestRun);
        assertEquals(83, state.consistencyPercent);
        assertTrue(state.accessibilitySummary.contains("5 active days"));
        assertTrue(state.accessibilitySummary.contains("current run 2 days"));
        assertTrue(state.accessibilitySummary.contains("best run 3 days"));
    }

    private static HabitEntity habit(
            long id,
            HabitModels.Type type,
            int target,
            HabitSchedule.Frequency frequency,
            LocalDate start) {
        return new HabitEntity(
                id,
                type.name() + " habit",
                type.name(),
                target,
                frequency.name(),
                start.toEpochDay(),
                1,
                0,
                false,
                0,
                0L);
    }

    private static HabitDayAggregateEntity aggregate(
            long habitId,
            LocalDate date,
            int value,
            boolean completed,
            boolean lapsed) {
        return new HabitDayAggregateEntity(
                habitId, date.toEpochDay(), value, completed, lapsed, 0L);
    }

    private static HabitConsistencyGridState.Cell cell(
            HabitConsistencyGridState state,
            LocalDate date) {
        return state.cells.stream()
                .filter(cell -> cell.date.equals(date))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing cell for " + date));
    }
}
