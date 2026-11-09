package com.prottasha.usage.platform;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.prottasha.usage.domain.UsageAggregator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UsageStatsDataSource {
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
}
