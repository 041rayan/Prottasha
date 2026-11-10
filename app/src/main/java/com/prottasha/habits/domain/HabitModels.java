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
        public final String cue;
        public final String twoMinuteAction;
        public final String reward;
        public final String ifThenPlan;
        public final String reboundPlan;
        public final String unit;
        public final int incrementVal;
        public final int maximumVal;
        public final int focusMinutes;
        public final String accountability;
        public final boolean archived;
        public final int sortOrder;

        public HabitSnapshot(
                long id, String title, Type type, int target, HabitSchedule schedule) {
            this(id, title, type, target, schedule, "", "", "", "", "", "", 1, 0, 0, "", false, 0);
        }

        public HabitSnapshot(
                long id,
                String title,
                Type type,
                int target,
                HabitSchedule schedule,
                String cue,
                String twoMinuteAction,
                String reward,
                String ifThenPlan,
                String reboundPlan,
                String unit,
                int incrementVal,
                int maximumVal,
                int focusMinutes,
                String accountability,
                boolean archived,
                int sortOrder) {
            this.id = id;
            this.title = Objects.requireNonNull(title, "title");
            this.type = Objects.requireNonNull(type, "type");
            this.target = Math.max(1, target);
            this.schedule = Objects.requireNonNull(schedule, "schedule");
            this.cue = cue != null ? cue : "";
            this.twoMinuteAction = twoMinuteAction != null ? twoMinuteAction : "";
            this.reward = reward != null ? reward : "";
            this.ifThenPlan = ifThenPlan != null ? ifThenPlan : "";
            this.reboundPlan = reboundPlan != null ? reboundPlan : "";
            this.unit = unit != null ? unit : "";
            this.incrementVal = Math.max(1, incrementVal);
            this.maximumVal = Math.max(0, maximumVal);
            this.focusMinutes = Math.max(0, focusMinutes);
            this.accountability = accountability != null ? accountability : "";
            this.archived = archived;
            this.sortOrder = sortOrder;
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
        public final int bestStreak;
        public final int totalCompletions;
        public final int totalLapses;

        public HabitStats(boolean hasData, double consistency, int currentStreak) {
            this(hasData, consistency, currentStreak, currentStreak, (int) Math.round(consistency * 10), 0);
        }

        public HabitStats(
                boolean hasData,
                double consistency,
                int currentStreak,
                int bestStreak,
                int totalCompletions,
                int totalLapses) {
            this.hasData = hasData;
            this.consistency = consistency;
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
            this.totalCompletions = totalCompletions;
            this.totalLapses = totalLapses;
        }
    }

    private HabitModels() {
    }
}
