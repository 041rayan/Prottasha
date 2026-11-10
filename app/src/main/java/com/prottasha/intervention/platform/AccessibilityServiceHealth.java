package com.prottasha.intervention.platform;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;

import com.prottasha.service.ProttashaAccessibilityService;

public class AccessibilityServiceHealth {
    public enum State { HEALTHY, STALE, REVOKED, UNSUPPORTED, BATTERY_RISK }

    private final Context context;

    public AccessibilityServiceHealth(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
    }

    public State state(long lastEventAt, boolean batteryRisk) {
        if (!isEnabled()) return State.REVOKED;
        if (batteryRisk) return State.BATTERY_RISK;
        if (lastEventAt > 0 && System.currentTimeMillis() - lastEventAt > 10 * 60_000L) {
            return State.STALE;
        }
        return State.HEALTHY;
    }

    private boolean isEnabled() {
        String enabled = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String component = new ComponentName(context, ProttashaAccessibilityService.class)
                .flattenToString();
        return enabled != null && enabled.contains(component);
    }
}
