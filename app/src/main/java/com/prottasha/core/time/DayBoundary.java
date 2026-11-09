package com.prottasha.core.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Objects;

public final class DayBoundary {

    private DayBoundary() {
    }

    public static long toEpochDay(Instant instant, ZoneId zone, int cutoffMinutes) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        if (cutoffMinutes < 0 || cutoffMinutes > 1439) {
            throw new IllegalArgumentException("cutoffMinutes must be in 0..1439");
        }

        ZonedDateTime local = instant.atZone(zone);
        LocalDate date = local.toLocalDate();
        int minuteOfDay = local.getHour() * 60 + local.getMinute();
        return (minuteOfDay < cutoffMinutes ? date.minusDays(1) : date).toEpochDay();
    }
}
