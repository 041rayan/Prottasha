package com.prottasha.insights.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public final class InsightsCalculatorTest {
    @Test
    public void noDataIsExplicitlyUnavailableNotPerfect() {
        InsightsCalculator.Snapshot snapshot = InsightsCalculator.calculate(Collections.emptyList());
        assertFalse(snapshot.hasData);
        assertEquals(0.0, snapshot.followThroughRate, 0.0);
        assertFalse(snapshot.adequateSample);
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
    }
}
