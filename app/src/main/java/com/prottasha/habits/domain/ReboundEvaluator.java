package com.prottasha.habits.domain;

public final class ReboundEvaluator {

    private ReboundEvaluator() {
    }

    public static boolean isRebound(long lapseEpochDay, Long nextCompletionEpochDay) {
        return nextCompletionEpochDay != null && nextCompletionEpochDay > lapseEpochDay;
    }
}
