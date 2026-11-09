package com.prottasha.data.local;

import com.prottasha.data.local.entity.HabitEntity;
import com.prottasha.data.local.entity.IdentityEntity;
import com.prottasha.data.local.entity.TriggerEntity;

import org.junit.Test;
import static org.junit.Assert.*;

public class EntityLogicTest {

    @Test
    public void testIdentityEntityCreation() {
        IdentityEntity identity = new IdentityEntity(
                "I am a mindful scholar",
                "Discipline, Integrity",
                "Overcome short videos",
                null,
                1000L,
                true
        );

        assertEquals("I am a mindful scholar", identity.getIdentityStatement());
        assertTrue(identity.isActive());
        assertEquals("Discipline, Integrity", identity.getCoreValues());
    }

    @Test
    public void testHabitVoteAndStreakCalculation() {
        HabitEntity habit = new HabitEntity(
                1L,
                "Study Deeply",
                true,
                "Desk desk",
                "Learn",
                "Open book",
                "Vote +1"
        );

        assertEquals(0, habit.getVotesFor());
        assertEquals(0, habit.getCurrentStreak());
        assertEquals(0, habit.getConsecutiveMisses());

        // Simulate positive vote
        habit.setVotesFor(habit.getVotesFor() + 1);
        habit.setCurrentStreak(habit.getCurrentStreak() + 1);
        habit.setConsecutiveMisses(0);

        assertEquals(1, habit.getVotesFor());
        assertEquals(1, habit.getCurrentStreak());
        assertEquals(0, habit.getConsecutiveMisses());

        // Simulate miss
        habit.setVotesAgainst(habit.getVotesAgainst() + 1);
        habit.setConsecutiveMisses(habit.getConsecutiveMisses() + 1);

        assertEquals(1, habit.getVotesAgainst());
        assertEquals(1, habit.getConsecutiveMisses());
    }

    @Test
    public void testTriggerIfThenPlan() {
        TriggerEntity trigger = new TriggerEntity(
                "Tired in bed",
                "Distraction",
                "If tired, then place phone in living room",
                120
        );

        assertEquals(120, trigger.getUrgeSurfingSeconds());
        assertTrue(trigger.getIfThenPlan().startsWith("If tired"));
        assertEquals(0, trigger.getTimesEncountered());

        trigger.setTimesEncountered(trigger.getTimesEncountered() + 1);
        trigger.setTimesOvercome(trigger.getTimesOvercome() + 1);
        assertEquals(1, trigger.getTimesEncountered());
        assertEquals(1, trigger.getTimesOvercome());
    }
}
