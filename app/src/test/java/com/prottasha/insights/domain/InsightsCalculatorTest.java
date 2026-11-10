package com.prottasha.insights.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.prottasha.intervention.data.InterventionEventEntity;
import com.prottasha.usage.data.UsageDailyEntity;
import com.prottasha.usage.domain.UsageAggregator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public final class InsightsCalculatorTest {
    @Test
    public void noDataIsExplicitlyUnavailableNotPerfect() {
        InsightsCalculator.Snapshot snapshot = InsightsCalculator.calculate(Collections.emptyList());
        assertFalse(snapshot.hasData);
        assertEquals(0.0, snapshot.followThroughRate, 0.0);
        assertFalse(snapshot.adequateSample);
        assertEquals(0, snapshot.decisions);
        assertEquals(0L, snapshot.totalUsageMillis);
    }

    @Test
    public void rapidReopenDoesNotCountAsFollowThrough() {
        InsightsCalculator.Snapshot snapshot = InsightsCalculator.calculate(Arrays.asList(
                InsightsCalculator.Outcome.STEP_BACK,
                InsightsCalculator.Outcome.RAPID_REOPEN,
                InsightsCalculator.Outcome.FOLLOW_THROUGH,
                InsightsCalculator.Outcome.CONTINUE));
        assertTrue(snapshot.hasData);
        assertEquals(0.25, snapshot.followThroughRate, 0.0001);
        assertFalse(snapshot.adequateSample);
        assertEquals(1, snapshot.followThroughCount);
        assertEquals(1, snapshot.rapidReopenCount);
    }

    @Test
    public void adequateSampleRequiresAtLeastSevenDecisions() {
        InsightsCalculator.Snapshot snapshot = InsightsCalculator.calculate(Arrays.asList(
                InsightsCalculator.Outcome.FOLLOW_THROUGH,
                InsightsCalculator.Outcome.FOLLOW_THROUGH,
                InsightsCalculator.Outcome.CONTINUE,
                InsightsCalculator.Outcome.STEP_BACK,
                InsightsCalculator.Outcome.RAPID_REOPEN,
                InsightsCalculator.Outcome.FOLLOW_THROUGH,
                InsightsCalculator.Outcome.CONTINUE));
        assertTrue(snapshot.adequateSample);
        assertEquals(3.0 / 7.0, snapshot.followThroughRate, 0.0001);
        assertEquals(7, snapshot.decisions);
    }

    @Test
    public void calculateRange_windowsCorrectlyByEpochDay() {
        long todayEpoch = LocalDate.of(2026, 11, 10).toEpochDay();
        ZoneId zone = ZoneId.of("Asia/Dhaka");

        // Event today at 10 AM
        long todayTs = ZonedDateTime.of(2026, 11, 10, 10, 0, 0, 0, zone).toInstant().toEpochMilli();
        // Event 3 days ago at 10 AM
        long threeDaysAgoTs = ZonedDateTime.of(2026, 11, 7, 10, 0, 0, 0, zone).toInstant().toEpochMilli();
        // Event 15 days ago
        long fifteenDaysAgoTs = ZonedDateTime.of(2026, 10, 26, 10, 0, 0, 0, zone).toInstant().toEpochMilli();

        List<InterventionEventEntity> events = Arrays.asList(
                new InterventionEventEntity(1L, 1L, "com.target.a", "PROMPT_SHOWN", todayTs, todayTs + 5000, 5, "ACCESSIBILITY", "STEP_BACK", "", "FOLLOW_THROUGH"),
                new InterventionEventEntity(2L, 1L, "com.target.a", "PROMPT_SHOWN", threeDaysAgoTs, threeDaysAgoTs + 5000, 5, "ACCESSIBILITY", "CONTINUED", "", "RAPID_REOPEN"),
                new InterventionEventEntity(3L, 1L, "com.target.b", "PROMPT_SHOWN", fifteenDaysAgoTs, fifteenDaysAgoTs + 5000, 5, "ACCESSIBILITY", "STEP_BACK", "", "FOLLOW_THROUGH")
        );

        List<UsageDailyEntity> usage = Arrays.asList(
                new UsageDailyEntity("com.target.a", todayEpoch, 1800000L, "COMPLETE", 10, 5),
                new UsageDailyEntity("com.target.a", todayEpoch - 3, 2400000L, "COMPLETE", 15, 8),
                new UsageDailyEntity("com.target.b", todayEpoch - 15, 3600000L, "PARTIAL", 20, 10)
        );

        // TODAY range
        InsightsCalculator.Snapshot todaySnapshot = InsightsCalculator.calculate(
                InsightsCalculator.Range.TODAY, todayEpoch, zone, events, usage,
                Collections.emptyList(), Collections.emptyList());
        assertEquals(1, todaySnapshot.decisions);
        assertEquals(1, todaySnapshot.stepBackCount);
        assertEquals(1800000L, todaySnapshot.totalUsageMillis);
        assertEquals(10, todaySnapshot.totalOpens);
        assertEquals(5, todaySnapshot.totalSessions);
        assertEquals(UsageAggregator.Quality.COMPLETE, todaySnapshot.usageQuality);

        // LAST_7_DAYS range
        InsightsCalculator.Snapshot weekSnapshot = InsightsCalculator.calculate(
                InsightsCalculator.Range.LAST_7_DAYS, todayEpoch, zone, events, usage,
                Collections.emptyList(), Collections.emptyList());
        assertEquals(2, weekSnapshot.decisions);
        assertEquals(4200000L, weekSnapshot.totalUsageMillis);
        assertEquals(25, weekSnapshot.totalOpens);
        assertEquals(13, weekSnapshot.totalSessions);

        // LAST_30_DAYS range
        InsightsCalculator.Snapshot monthSnapshot = InsightsCalculator.calculate(
                InsightsCalculator.Range.LAST_30_DAYS, todayEpoch, zone, events, usage,
                Collections.emptyList(), Collections.emptyList());
        assertEquals(3, monthSnapshot.decisions);
        assertEquals(7800000L, monthSnapshot.totalUsageMillis);
        // Includes PARTIAL usage record
        assertEquals(UsageAggregator.Quality.PARTIAL, monthSnapshot.usageQuality);
    }

    @Test
    public void timeOfDayPattern_requiresAdequateSampleAndDetectsEvening() {
        long todayEpoch = LocalDate.of(2026, 11, 10).toEpochDay();
        ZoneId zone = ZoneId.of("Asia/Dhaka");

        List<InterventionEventEntity> sevenEveningEvents = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            long ts = ZonedDateTime.of(2026, 11, 10 - i, 20, 30, 0, 0, zone).toInstant().toEpochMilli();
            sevenEveningEvents.add(new InterventionEventEntity(
                    i + 1, 1L, "com.target.social", "PROMPT_SHOWN", ts, ts + 5000, 5,
                    "ACCESSIBILITY", "STEP_BACK", "", "FOLLOW_THROUGH"));
        }

        InsightsCalculator.Snapshot snapshot = InsightsCalculator.calculate(
                InsightsCalculator.Range.LAST_7_DAYS, todayEpoch, zone, sevenEveningEvents,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertTrue(snapshot.adequateSample);
        assertNotNull(snapshot.patternSummary);
        assertTrue(snapshot.patternSummary.adequateSample);
        assertEquals("com.target.social", snapshot.patternSummary.topTarget);
        assertEquals(InsightsCalculator.TimeWindow.EVENING, snapshot.patternSummary.peakWindow);
    }

    @Test
    public void timeOfDayPattern_withholdsPatternsWhenSampleUnderSeven() {
        long todayEpoch = LocalDate.of(2026, 11, 10).toEpochDay();
        ZoneId zone = ZoneId.of("Asia/Dhaka");

        List<InterventionEventEntity> threeEvents = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            long ts = ZonedDateTime.of(2026, 11, 10 - i, 20, 30, 0, 0, zone).toInstant().toEpochMilli();
            threeEvents.add(new InterventionEventEntity(
                    i + 1, 1L, "com.target.social", "PROMPT_SHOWN", ts, ts + 5000, 5,
                    "ACCESSIBILITY", "STEP_BACK", "", "FOLLOW_THROUGH"));
        }

        InsightsCalculator.Snapshot snapshot = InsightsCalculator.calculate(
                InsightsCalculator.Range.LAST_7_DAYS, todayEpoch, zone, threeEvents,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertFalse(snapshot.adequateSample);
        assertNotNull(snapshot.patternSummary);
        assertFalse(snapshot.patternSummary.adequateSample);
        assertNull(snapshot.patternSummary.peakWindow);
    }
}
