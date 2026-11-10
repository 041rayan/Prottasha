package com.prottasha.habits.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HabitConsistencyGridState {

    public final List<Cell> cells;
    public final int weeks;
    public final boolean hasData;
    public final int activeDays;
    public final int currentRun;
    public final int bestRun;
    public final int consistencyPercent;
    public final String accessibilitySummary;

    HabitConsistencyGridState(
            List<Cell> cells,
            int weeks,
            boolean hasData,
            int activeDays,
            int currentRun,
            int bestRun,
            int consistencyPercent,
            String accessibilitySummary) {
        this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
        this.weeks = weeks;
        this.hasData = hasData;
        this.activeDays = activeDays;
        this.currentRun = currentRun;
        this.bestRun = bestRun;
        this.consistencyPercent = consistencyPercent;
        this.accessibilitySummary = accessibilitySummary;
    }

    public static final class Cell {
        public final LocalDate date;
        public final int scheduledCount;
        public final int completedCount;
        public final int intensity;
        public final boolean currentDay;

        Cell(
                LocalDate date,
                int scheduledCount,
                int completedCount,
                int intensity,
                boolean currentDay) {
            this.date = date;
            this.scheduledCount = scheduledCount;
            this.completedCount = completedCount;
            this.intensity = intensity;
            this.currentDay = currentDay;
        }
    }
}
