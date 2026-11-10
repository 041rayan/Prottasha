package com.prottasha.usage.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class UsageAggregator {
    public enum Quality { COMPLETE, PARTIAL, UNAVAILABLE }

    public static final class Session {
        public final String target;
        public final long startMillis;
        public final long endMillis;

        public Session(String target, long startMillis, long endMillis) {
            this.target = target;
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }
    }

    public static final class Summary {
        public final long totalMillis;
        public final Quality quality;
        public final boolean hasMeasuredValue;

        public Summary(long totalMillis, Quality quality, boolean hasMeasuredValue) {
            this.totalMillis = totalMillis;
            this.quality = quality;
            this.hasMeasuredValue = hasMeasuredValue;
        }
    }

    private UsageAggregator() { }

    public static Summary aggregate(List<Session> sessions) {
        List<Session> valid = new ArrayList<>();
        boolean malformed = false;
        for (Session session : sessions) {
            if (session == null || session.target == null
                    || session.endMillis <= session.startMillis) {
                malformed = true;
            } else {
                valid.add(session);
            }
        }
        valid.sort(Comparator.comparingLong(value -> value.startMillis));
        long total = 0;
        long start = -1;
        long end = -1;
        for (Session session : valid) {
            if (start < 0 || session.startMillis > end) {
                if (start >= 0) total += end - start;
                start = session.startMillis;
                end = session.endMillis;
            } else {
                end = Math.max(end, session.endMillis);
            }
        }
        if (start >= 0) total += end - start;
        return new Summary(total, malformed ? Quality.PARTIAL : Quality.COMPLETE, true);
    }

    public static Summary unavailable() {
        return new Summary(0L, Quality.UNAVAILABLE, false);
    }
}
