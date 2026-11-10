package com.prottasha.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.lifecycle.Observer;

import com.prottasha.ProttashaApp;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.domain.EssentialTargetPolicy;
import com.prottasha.guard.domain.GuardDecision;
import com.prottasha.guard.domain.GuardDecisionEngine;
import com.prottasha.guard.domain.GuardModels;
import com.prottasha.guard.domain.GuardRule;
import com.prottasha.intervention.platform.AccessibilityServiceHealth;
import com.prottasha.intervention.platform.InterventionController;
import com.prottasha.intervention.platform.InterventionOverlayController;
import com.prottasha.intervention.platform.ServicePackageFilter;
import com.prottasha.usage.browser.ChromeDomainAdapter;
import com.prottasha.usage.domain.UsageAggregator;
import com.prottasha.usage.platform.UsageStatsDataSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProttashaAccessibilityService extends AccessibilityService {

    private final ServicePackageFilter filter = new ServicePackageFilter();
    private final ChromeDomainAdapter chromeAdapter = new ChromeDomainAdapter();
    private EssentialTargetPolicy essentialPolicy;
    private AccessibilityServiceHealth healthChecker;
    private UsageStatsDataSource usageStatsDataSource;
    private InterventionController interventionController;
    private Observer<List<GuardRuleEntity>> ruleObserver;
    private volatile long lastEventAt;
    private static volatile boolean sRunning = false;

    public static boolean isRunning() {
        return sRunning;
    }

    private static final Set<String> SUPPORTED_BROWSERS;
    static {
        Set<String> browsers = new HashSet<>();
        browsers.add("com.android.chrome");
        browsers.add("com.brave.browser");
        SUPPORTED_BROWSERS = Collections.unmodifiableSet(browsers);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        sRunning = true;
        ProttashaApp app = (ProttashaApp) getApplication();
        essentialPolicy = new EssentialTargetPolicy(getPackageName());
        healthChecker = new AccessibilityServiceHealth(this);
        usageStatsDataSource = new UsageStatsDataSource(this);
        interventionController = new InterventionController(
                new InterventionOverlayController(this, app.appGraph().interventionEvents()));

        ruleObserver = rules -> {
            app.appGraph().executors().disk().execute(() -> {
                filter.replace(rules, essentialPolicy);
            });
        };

        new Handler(Looper.getMainLooper()).post(() -> {
            app.appGraph().guardRuleRepository().observeRules().observeForever(ruleObserver);
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        lastEventAt = System.currentTimeMillis();

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        String packageName = event.getPackageName().toString();
        if (essentialPolicy != null && essentialPolicy.isEssential(packageName)) {
            return;
        }

        String target = packageName;
        if (SUPPORTED_BROWSERS.contains(packageName)) {
            AccessibilityNodeInfo source = event.getSource();
            String domain = null;
            if (source != null) {
                domain = chromeAdapter.readSelectedDomain(source);
                source.recycle();
            }
            if (domain == null) {
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if (root != null) {
                    domain = chromeAdapter.readSelectedDomain(root);
                    root.recycle();
                }
            }
            if (domain != null && filter.isMonitored(domain)) {
                target = domain;
            } else if (!filter.isMonitored(packageName)) {
                return;
            }
        } else if (!filter.isMonitored(packageName)) {
            return;
        }

        GuardRuleEntity ruleEntity = filter.ruleFor(target);
        if (ruleEntity == null || !ruleEntity.enabled) {
            return;
        }

        ProttashaApp app = (ProttashaApp) getApplication();
        int minuteOfDay = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        boolean isHealthy = healthChecker == null || healthChecker.state(lastEventAt, false) == AccessibilityServiceHealth.State.HEALTHY;
        GuardModels.GuardContext context = new GuardModels.GuardContext(minuteOfDay, isHealthy, false);

        boolean cooldownActive = app.appGraph().guardRuntime().isCoolingDown(target, System.currentTimeMillis());
        int usedMinutes = 0;
        boolean qualityAvailable = true;

        if ("BUDGET".equals(ruleEntity.mode)) {
            long dayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long now = System.currentTimeMillis();
            Set<String> queryTargets = Collections.singleton(packageName);
            UsageAggregator.Summary summary = usageStatsDataSource.query(dayStart, now, queryTargets);
            qualityAvailable = summary.quality != UsageAggregator.Quality.UNAVAILABLE;
            usedMinutes = (int) (summary.totalMillis / 60_000L);
        }

        GuardRule domainRule;
        if ("BLOCK".equals(ruleEntity.mode)) {
            domainRule = GuardRule.block(target, ruleEntity.startMinute, ruleEntity.endMinute);
        } else if ("BUDGET".equals(ruleEntity.mode)) {
            domainRule = GuardRule.budget(target, ruleEntity.budgetMinutes, ruleEntity.startMinute, ruleEntity.endMinute);
        } else {
            domainRule = GuardRule.pause(target, ruleEntity.startMinute, ruleEntity.endMinute);
        }

        GuardDecision decision = GuardDecisionEngine.decide(
                domainRule, context, new GuardModels.GuardUsage(usedMinutes, qualityAvailable, cooldownActive));

        if (decision == GuardDecision.ALLOW || decision == GuardDecision.UNAVAILABLE) {
            return;
        }

        if (decision == GuardDecision.PAUSE) {
            long cooldownMillis = ruleEntity.cooldownMinutes * 60_000L;
            app.appGraph().guardRuntime().startCooldown(target, System.currentTimeMillis() + cooldownMillis);
            if (interventionController != null) {
                interventionController.intervene(ruleEntity.id, target, "PAUSE", ruleEntity.delaySeconds, ruleEntity.replacementAction);
            }
        } else if (decision == GuardDecision.BLOCK) {
            if (interventionController != null) {
                interventionController.intervene(ruleEntity.id, target, "BLOCK", 0, ruleEntity.replacementAction);
            }
        }
    }

    public ServicePackageFilter getFilter() {
        return filter;
    }

    public long lastEventAt() {
        return lastEventAt;
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        sRunning = false;
        if (ruleObserver != null) {
            ProttashaApp app = (ProttashaApp) getApplication();
            app.appGraph().guardRuleRepository().observeRules().removeObserver(ruleObserver);
        }
        super.onDestroy();
    }
}
