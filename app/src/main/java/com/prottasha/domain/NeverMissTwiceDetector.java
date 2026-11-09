package com.prottasha.domain;

import com.prottasha.data.local.entity.HabitEntity;

public class NeverMissTwiceDetector {

    public enum ReboundStatus {
        ON_TRACK,
        REBOUND_NEEDED, // 1 miss: AH-18 Never Miss Twice
        CHAIN_REVIEW_TRIGGERED // 2+ misses: EX-10 Marlatt chain review
    }

    public static ReboundStatus evaluate(HabitEntity habit) {
        if (habit == null || habit.getConsecutiveMisses() == 0) {
            return ReboundStatus.ON_TRACK;
        } else if (habit.getConsecutiveMisses() == 1) {
            return ReboundStatus.REBOUND_NEEDED;
        } else {
            return ReboundStatus.CHAIN_REVIEW_TRIGGERED;
        }
    }

    public static String getReboundInterventionMessage(HabitEntity habit) {
        ReboundStatus status = evaluate(habit);
        switch (status) {
            case REBOUND_NEEDED:
                return "Yesterday was just one vote. Today is the rebound: perform a 2-minute micro version of '" 
                        + habit.getTitle() + "' to lock in your identity!";
            case CHAIN_REVIEW_TRIGGERED:
                return "No shame, just data. Let's inspect what cue triggered this loop and adjust your environment.";
            case ON_TRACK:
            default:
                return "Streak intact: " + (habit != null ? habit.getCurrentStreak() : 0) + " small wins!";
        }
    }
}
