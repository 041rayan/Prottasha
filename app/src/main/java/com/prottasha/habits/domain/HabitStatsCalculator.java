package com.prottasha.habits.domain;

import java.util.List;

public final class HabitStatsCalculator {

    private HabitStatsCalculator() {
    }

    public static HabitModels.HabitStats calculate(List<Boolean> completedDueDays) {
        if (completedDueDays.isEmpty()) {
            return new HabitModels.HabitStats(false, 0.0, 0);
        }
        int completed = 0;
        int streak = 0;
        for (Boolean value : completedDueDays) {
            if (Boolean.TRUE.equals(value)) {
                completed++;
            }
        }
        for (int index = completedDueDays.size() - 1;
             index >= 0 && Boolean.TRUE.equals(completedDueDays.get(index));
             index--) {
            streak++;
        }
        return new HabitModels.HabitStats(
                true, completed / (double) completedDueDays.size(), streak);
    }
}
