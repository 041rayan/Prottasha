package com.prottasha.intervention.platform;

import androidx.annotation.Nullable;

import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.domain.EssentialTargetPolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ServicePackageFilter {

    private volatile Map<String, GuardRuleEntity> rulesMap = Collections.emptyMap();

    public void replace(List<GuardRuleEntity> rules, EssentialTargetPolicy essentials) {
        Map<String, GuardRuleEntity> next = new HashMap<>();
        if (rules != null) {
            for (GuardRuleEntity rule : rules) {
                if (rule != null && rule.enabled && (essentials == null || !essentials.isEssential(rule.target))) {
                    next.put(rule.target, rule);
                }
            }
        }
        rulesMap = Collections.unmodifiableMap(next);
    }

    @Nullable
    public GuardRuleEntity ruleFor(String target) {
        if (target == null) return null;
        return rulesMap.get(target);
    }

    @Nullable
    public String modeFor(String target) {
        GuardRuleEntity rule = ruleFor(target);
        return rule != null ? rule.mode : null;
    }

    public boolean isMonitored(String target) {
        return ruleFor(target) != null;
    }

    public Set<String> monitoredTargets() {
        return rulesMap.keySet();
    }
}
