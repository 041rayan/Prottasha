package com.prottasha.habits.domain;

public final class HabitSchedule {

    public enum Frequency {
        DAILY,
        WEEKDAYS,
        EVERY_N_DAYS,
        EVERY_N_WEEKS,
        EVERY_N_MONTHS,
        FROM_LAST_DONE,
        X_PER_WEEK,
        X_PER_MONTH
    }

    public final Frequency frequency;
    public final long startEpochDay;
    public final int interval;
    public final int dayOfMonth;

    private HabitSchedule(Frequency frequency, long startEpochDay, int interval, int dayOfMonth) {
        this.frequency = frequency;
        this.startEpochDay = startEpochDay;
        this.interval = interval;
        this.dayOfMonth = dayOfMonth;
    }

    public static HabitSchedule daily(long startEpochDay) {
        return new HabitSchedule(Frequency.DAILY, startEpochDay, 1, 0);
    }

    public static HabitSchedule weekdays(long startEpochDay) {
        return new HabitSchedule(Frequency.WEEKDAYS, startEpochDay, 1, 0);
    }

    public static HabitSchedule everyNDays(long startEpochDay, int interval) {
        return new HabitSchedule(Frequency.EVERY_N_DAYS, startEpochDay,
                requirePositive(interval), 0);
    }

    public static HabitSchedule everyNWeeks(long startEpochDay, int intervalWeeks) {
        return new HabitSchedule(Frequency.EVERY_N_WEEKS, startEpochDay,
                requirePositive(intervalWeeks), 0);
    }

    public static HabitSchedule everyNMonths(long startEpochDay, int interval, int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("dayOfMonth must be in 1..31");
        }
        return new HabitSchedule(Frequency.EVERY_N_MONTHS, startEpochDay,
                requirePositive(interval), dayOfMonth);
    }

    public static HabitSchedule fromLastDone(long startEpochDay, int interval) {
        return new HabitSchedule(Frequency.FROM_LAST_DONE, startEpochDay,
                requirePositive(interval), 0);
    }

    public static HabitSchedule xPerWeek(long startEpochDay, int targetTimesPerWeek) {
        return new HabitSchedule(Frequency.X_PER_WEEK, startEpochDay,
                requirePositive(targetTimesPerWeek), 0);
    }

    public static HabitSchedule xPerMonth(long startEpochDay, int targetTimesPerMonth) {
        return new HabitSchedule(Frequency.X_PER_MONTH, startEpochDay,
                requirePositive(targetTimesPerMonth), 0);
    }

    private static int requirePositive(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("interval must be positive");
        }
        return value;
    }
}
