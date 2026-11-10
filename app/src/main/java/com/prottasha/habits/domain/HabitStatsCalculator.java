package com.prottasha.habits.domain;

import java.util.List;

public final class HabitStatsCalculator {

    private HabitStatsCalculator() {
    }

    public static HabitModels.HabitStats calculate(List<Boolean> completedDueDays) {
        if (completedDueDays == null || completedDueDays.isEmpty()) {
            return new HabitModels.HabitStats(false, 0.0, 0, 0, 0, 0);
        }
        int completed = 0;
        int lapses = 0;
        int currentStreak = 0;
        int bestStreak = 0;
        int runStreak = 0;

        for (Boolean value : completedDueDays) {
            if (Boolean.TRUE.equals(value)) {
                completed++;
                runStreak++;
                if (runStreak > bestStreak) {
                    bestStreak = runStreak;
                }
            } else {
                lapses++;
                runStreak = 0;
            }
        }

        for (int index = completedDueDays.size() - 1;
             index >= 0 && Boolean.TRUE.equals(completedDueDays.get(index));
             index--) {
            currentStreak++;
        }

        double consistency = completed / (double) completedDueDays.size();
        return new HabitModels.HabitStats(
                true, consistency, currentStreak, bestStreak, completed, lapses);
    }
}
