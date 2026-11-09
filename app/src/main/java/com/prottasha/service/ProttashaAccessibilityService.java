package com.prottasha.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.prottasha.ProttashaApp;
import com.prottasha.guard.domain.EssentialTargetPolicy;
import com.prottasha.intervention.platform.ServicePackageFilter;
import com.prottasha.ui.pause.PauseInterstitialActivity;

public final class ProttashaAccessibilityService extends AccessibilityService {
    private static final long COOLDOWN_MS = 15_000L;
    private final ServicePackageFilter filter = new ServicePackageFilter();
    private long lastInterventionAt;
    private volatile long lastEventAt;

    @Override
    protected void onServiceConnected() {
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().disk().execute(() -> {
            try {
                filter.replace(app.appGraph().userSessionDatabaseProvider().requireOpen()
                                .guardRuleDao().list(),
                        new EssentialTargetPolicy(getPackageName()));
            } catch (IllegalStateException noSession) {
                filter.replace(java.util.Collections.emptyList(),
                        new EssentialTargetPolicy(getPackageName()));
            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getPackageName() == null) return;
        lastEventAt = System.currentTimeMillis();
        String packageName = event.getPackageName().toString();
        String mode = filter.modeFor(packageName);
        if (mode == null || System.currentTimeMillis() - lastInterventionAt < COOLDOWN_MS) return;
        lastInterventionAt = System.currentTimeMillis();
        Intent intent = new Intent(this, PauseInterstitialActivity.class)
                .putExtra(PauseInterstitialActivity.EXTRA_TARGET_PACKAGE, packageName)
                .putExtra("guard_mode", mode)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public long lastEventAt() { return lastEventAt; }
    @Override public void onInterrupt() { }
}
