package com.prottasha.habits.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public final class HabitEnginesTest {

    @Test
    public void scheduleEvaluatorHandlesDailyWeekdayAndIntervals() {
        long monday = LocalDate.of(2026, 11, 9).toEpochDay();
        assertTrue(HabitScheduleEvaluator.isDue(HabitSchedule.daily(monday), monday, null));
        assertTrue(HabitScheduleEvaluator.isDue(HabitSchedule.weekdays(monday), monday, null));
        assertFalse(HabitScheduleEvaluator.isDue(
                HabitSchedule.weekdays(monday), monday + 5, null));
        assertTrue(HabitScheduleEvaluator.isDue(
                HabitSchedule.everyNDays(monday, 3), monday + 6, null));
        assertFalse(HabitScheduleEvaluator.isDue(
                HabitSchedule.everyNDays(monday, 3), monday + 5, null));
    }

    @Test
    public void scheduleEvaluatorClampsMonthEndAndSupportsFromLastDone() {
        long january31 = LocalDate.of(2027, 1, 31).toEpochDay();
        long february28 = LocalDate.of(2027, 2, 28).toEpochDay();
        assertTrue(HabitScheduleEvaluator.isDue(
                HabitSchedule.everyNMonths(january31, 1, 31), february28, null));
        assertTrue(HabitScheduleEvaluator.isDue(
                HabitSchedule.fromLastDone(january31, 4), january31 + 8, january31 + 4));
        assertFalse(HabitScheduleEvaluator.isDue(
                HabitSchedule.fromLastDone(january31, 4), january31 + 7, january31 + 4));
    }

    @Test
    public void buildAvoidAndAmountActionsRemainSemanticallyDistinct() {
        HabitModels.CompletionResult build = HabitCompletionEngine.apply(
                HabitModels.Type.BUILD, 1, 0, HabitAction.complete("build-1"));
        HabitModels.CompletionResult lapse = HabitCompletionEngine.apply(
                HabitModels.Type.AVOID, 1, 0, HabitAction.lapse("avoid-1"));
        HabitModels.CompletionResult amount = HabitCompletionEngine.apply(
                HabitModels.Type.AMOUNT, 5, 3, HabitAction.increment("amount-1", 4));

        assertEquals(1, build.value);
        assertTrue(build.completed);
        assertTrue(lapse.lapsed);
        assertFalse(lapse.completed);
        assertEquals(5, amount.value);
        assertTrue(amount.completed);
    }

    @Test
    public void duplicateNonceAndUndoAreDeterministic() {
        HabitAction first = HabitAction.complete("same-nonce");
        HabitAction duplicate = HabitAction.complete("same-nonce");
        assertEquals(first.nonce, duplicate.nonce);
        assertEquals(HabitAction.Kind.UNDO, HabitAction.undo("undo-1", 42L).kind);
        assertEquals(42L, HabitAction.undo("undo-1", 42L).revertedEventId);
    }

    @Test
    public void noDataStatsAreNotPerfectAndCompletedDaysProduceTruthfulRate() {
        HabitModels.HabitStats empty = HabitStatsCalculator.calculate(Collections.emptyList());
        HabitModels.HabitStats data = HabitStatsCalculator.calculate(
                Arrays.asList(true, false, true, true));

        assertFalse(empty.hasData);
        assertEquals(0.0, empty.consistency, 0.0);
        assertTrue(data.hasData);
        assertEquals(0.75, data.consistency, 0.0001);
        assertEquals(2, data.currentStreak);
    }

    @Test
    public void reboundRequiresCompletionAfterLapse() {
        assertTrue(ReboundEvaluator.isRebound(100L, 101L));
        assertFalse(ReboundEvaluator.isRebound(100L, 100L));
        assertFalse(ReboundEvaluator.isRebound(100L, null));
    }
}
