package com.prottasha.usage.platform;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.prottasha.usage.domain.UsageAggregator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UsageStatsDataSource {
    public static final class TargetMetrics {
        public final long totalMillis;
        public final int opens;
        public final int sessions;
        public final UsageAggregator.Quality quality;

        public TargetMetrics(long totalMillis, int opens, int sessions, UsageAggregator.Quality quality) {
            this.totalMillis = totalMillis;
            this.opens = opens;
            this.sessions = sessions;
            this.quality = quality;
        }
    }

    private final UsageStatsManager manager;

    public UsageStatsDataSource(Context context) {
        manager = context.getSystemService(UsageStatsManager.class);
    }

    public UsageAggregator.Summary query(long startMillis, long endMillis, Set<String> targets) {
        if (manager == null) return UsageAggregator.unavailable();
        UsageEvents events = manager.queryEvents(startMillis, endMillis);
        if (events == null) return UsageAggregator.unavailable();
        Map<String, Long> starts = new HashMap<>();
        List<UsageAggregator.Session> sessions = new ArrayList<>();
        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            String packageName = event.getPackageName();
            if (!targets.contains(packageName)) continue;
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                starts.put(packageName, event.getTimeStamp());
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                Long start = starts.remove(packageName);
                if (start != null) {
                    sessions.add(new UsageAggregator.Session(
                            packageName, start, event.getTimeStamp()));
                }
            }
        }
        return UsageAggregator.aggregate(sessions);
    }

    public Map<String, TargetMetrics> queryPerTarget(long startMillis, long endMillis, Set<String> targets) {
        if (manager == null || targets == null || targets.isEmpty()) return Collections.emptyMap();
        UsageEvents events = manager.queryEvents(startMillis, endMillis);
        if (events == null) return Collections.emptyMap();

        Map<String, Long> starts = new HashMap<>();
        Map<String, List<UsageAggregator.Session>> sessionsByTarget = new HashMap<>();
        Map<String, Integer> opensByTarget = new HashMap<>();
        Map<String, Integer> sessionsCountByTarget = new HashMap<>();

        for (String target : targets) {
            sessionsByTarget.put(target, new ArrayList<>());
            opensByTarget.put(target, 0);
            sessionsCountByTarget.put(target, 0);
        }

        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            String packageName = event.getPackageName();
            if (!targets.contains(packageName)) continue;

            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                starts.put(packageName, event.getTimeStamp());
                opensByTarget.put(packageName, opensByTarget.getOrDefault(packageName, 0) + 1);
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                Long start = starts.remove(packageName);
                if (start != null) {
                    List<UsageAggregator.Session> list = sessionsByTarget.get(packageName);
                    if (list != null) {
                        list.add(new UsageAggregator.Session(packageName, start, event.getTimeStamp()));
                    }
                    sessionsCountByTarget.put(packageName,
                            sessionsCountByTarget.getOrDefault(packageName, 0) + 1);
                }
            }
        }

        Map<String, TargetMetrics> result = new HashMap<>();
        for (String target : targets) {
            List<UsageAggregator.Session> list = sessionsByTarget.get(target);
            UsageAggregator.Summary summary = (list != null && !list.isEmpty())
                    ? UsageAggregator.aggregate(list)
                    : new UsageAggregator.Summary(0L, UsageAggregator.Quality.COMPLETE, true);
            result.put(target, new TargetMetrics(
                    summary.totalMillis,
                    opensByTarget.getOrDefault(target, 0),
                    sessionsCountByTarget.getOrDefault(target, 0),
                    summary.quality));
        }
        return result;
    }
}
