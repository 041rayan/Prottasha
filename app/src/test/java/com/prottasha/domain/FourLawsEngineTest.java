package com.prottasha.domain;

import com.prottasha.data.local.entity.HabitEntity;

import org.junit.Test;
import static org.junit.Assert.*;

public class FourLawsEngineTest {

    @Test
    public void testTwoMinuteScaling() {
        String study = FourLawsEngine.scaleToTwoMinuteVersion("Deep study computer architecture for 3 hours");
        assertTrue(study.contains("read just one page"));

        String workout = FourLawsEngine.scaleToTwoMinuteVersion("Heavy gym workout");
        assertTrue(workout.contains("running shoes"));

        String fallback = FourLawsEngine.scaleToTwoMinuteVersion("Clean the entire garage");
        assertTrue(fallback.contains("Start the first step"));
    }

    @Test
    public void testAlignmentCalculation() {
        // 8 votes for, 2 against = 80%
        int score = FourLawsEngine.calculateAlignmentScore(8, 2);
        assertEquals(80, score);
        assertTrue(FourLawsEngine.isIdentityMajority(8, 2));

        // 0 votes total = 100% baseline
        assertEquals(100, FourLawsEngine.calculateAlignmentScore(0, 0));

        // 2 votes for, 8 against = 20%
        assertEquals(20, FourLawsEngine.calculateAlignmentScore(2, 8));
        assertFalse(FourLawsEngine.isIdentityMajority(2, 8));
    }

    @Test
    public void testNeverMissTwiceDetector() {
        HabitEntity habit = new HabitEntity(1L, "Meditation", true, "Morning", "Calm", "Sit 2m", "Vote +1");

        // 0 misses -> ON_TRACK
        habit.setConsecutiveMisses(0);
        assertEquals(NeverMissTwiceDetector.ReboundStatus.ON_TRACK, NeverMissTwiceDetector.evaluate(habit));

        // 1 miss -> REBOUND_NEEDED (AH-18)
        habit.setConsecutiveMisses(1);
        assertEquals(NeverMissTwiceDetector.ReboundStatus.REBOUND_NEEDED, NeverMissTwiceDetector.evaluate(habit));
        String reboundMsg = NeverMissTwiceDetector.getReboundInterventionMessage(habit);
        assertTrue(reboundMsg.contains("rebound"));

        // 2 misses -> CHAIN_REVIEW_TRIGGERED (EX-10)
        habit.setConsecutiveMisses(2);
        assertEquals(NeverMissTwiceDetector.ReboundStatus.CHAIN_REVIEW_TRIGGERED, NeverMissTwiceDetector.evaluate(habit));
        String reviewMsg = NeverMissTwiceDetector.getReboundInterventionMessage(habit);
        assertTrue(reviewMsg.contains("No shame, just data"));
    }
}
