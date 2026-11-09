package com.prottasha.guard.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GuardRuntimeRepository {
    private final Map<String, Long> cooldownUntil = new ConcurrentHashMap<>();

    public boolean isCoolingDown(String target, long now) {
        return cooldownUntil.getOrDefault(target, 0L) > now;
    }

    public void startCooldown(String target, long until) {
        cooldownUntil.put(target, until);
    }
}
