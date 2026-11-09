package com.prottasha.habits.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public final class HabitScheduleEvaluator {

    private HabitScheduleEvaluator() {
    }

    public static boolean isDue(HabitSchedule schedule, long epochDay, Long lastDoneEpochDay) {
        if (epochDay < schedule.startEpochDay) {
            return false;
        }
        switch (schedule.frequency) {
            case DAILY:
                return true;
            case WEEKDAYS:
                DayOfWeek day = LocalDate.ofEpochDay(epochDay).getDayOfWeek();
                return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            case EVERY_N_DAYS:
                return (epochDay - schedule.startEpochDay) % schedule.interval == 0;
            case FROM_LAST_DONE:
                long anchor = lastDoneEpochDay == null
                        ? schedule.startEpochDay : lastDoneEpochDay;
                return epochDay >= anchor + schedule.interval;
            case EVERY_N_MONTHS:
                LocalDate start = LocalDate.ofEpochDay(schedule.startEpochDay);
                LocalDate current = LocalDate.ofEpochDay(epochDay);
                long months = ChronoUnit.MONTHS.between(
                        YearMonth.from(start), YearMonth.from(current));
                int targetDay = Math.min(schedule.dayOfMonth,
                        YearMonth.from(current).lengthOfMonth());
                return months >= 0
                        && months % schedule.interval == 0
                        && current.getDayOfMonth() == targetDay;
            default:
                return false;
        }
    }
}
