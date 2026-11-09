package com.prottasha.habits.domain;

import java.util.Objects;

public final class HabitModels {

    public enum Type { BUILD, AVOID, AMOUNT }

    public static final class HabitSnapshot {
        public final long id;
        public final String title;
        public final Type type;
        public final int target;
        public final HabitSchedule schedule;

        public HabitSnapshot(
                long id, String title, Type type, int target, HabitSchedule schedule) {
            this.id = id;
            this.title = Objects.requireNonNull(title, "title");
            this.type = Objects.requireNonNull(type, "type");
            this.target = Math.max(1, target);
            this.schedule = Objects.requireNonNull(schedule, "schedule");
        }
    }

    public static final class HabitTodayItem {
        public final HabitSnapshot habit;
        public final int value;
        public final boolean completed;
        public final boolean lapsed;

        public HabitTodayItem(
                HabitSnapshot habit, int value, boolean completed, boolean lapsed) {
            this.habit = habit;
            this.value = value;
            this.completed = completed;
            this.lapsed = lapsed;
        }
    }

    public static final class CompletionResult {
        public final int value;
        public final boolean completed;
        public final boolean lapsed;
        public final long eventId;

        public CompletionResult(int value, boolean completed, boolean lapsed) {
            this(value, completed, lapsed, 0L);
        }

        public CompletionResult(int value, boolean completed, boolean lapsed, long eventId) {
            this.value = value;
            this.completed = completed;
            this.lapsed = lapsed;
            this.eventId = eventId;
        }
    }

    public static final class HabitStats {
        public final boolean hasData;
        public final double consistency;
        public final int currentStreak;

        public HabitStats(boolean hasData, double consistency, int currentStreak) {
            this.hasData = hasData;
            this.consistency = consistency;
            this.currentStreak = currentStreak;
        }
    }

    private HabitModels() {
    }
}
