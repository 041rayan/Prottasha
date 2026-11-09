package com.prottasha.intervention.platform;

import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.domain.EssentialTargetPolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ServicePackageFilter {
    private volatile Map<String, String> modes = Collections.emptyMap();

    public void replace(List<GuardRuleEntity> rules, EssentialTargetPolicy essentials) {
        Map<String, String> next = new HashMap<>();
        for (GuardRuleEntity rule : rules) {
            if (rule.enabled && !essentials.isEssential(rule.target)) {
                next.put(rule.target, rule.mode);
            }
        }
        modes = Collections.unmodifiableMap(next);
    }

    public String modeFor(String packageName) {
        return modes.get(packageName);
    }
}
