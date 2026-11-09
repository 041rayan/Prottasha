package com.prottasha.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.prottasha.ui.pause.PauseInterstitialActivity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProttashaAccessibilityService extends AccessibilityService {

    // Default distraction packages (Short video feeds, social media, casinos)
    private static final Set<String> BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "com.zhiliaoapp.musically",       // TikTok
            "com.instagram.android",          // Instagram
            "com.facebook.katana",            // Facebook
            "com.google.android.youtube",     // YouTube
            "com.twitter.android",            // X / Twitter
            "com.snapchat.android"            // Snapchat
    ));

    private long lastInterventionTime = 0;
    private static final long COOLDOWN_MS = 15000; // 15-second cooldown per app launch

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence packageNameChar = event.getPackageName();
        if (packageNameChar == null) return;
        String packageName = packageNameChar.toString();

        // Guard against recursive loop when Prottasha itself is in foreground (Review Focus 1)
        if (packageName.equals(getPackageName())) {
            return;
        }

        long now = System.currentTimeMillis();
        if (BLOCKED_PACKAGES.contains(packageName) && (now - lastInterventionTime > COOLDOWN_MS)) {
            lastInterventionTime = now;
            Intent pauseIntent = new Intent(this, PauseInterstitialActivity.class);
            pauseIntent.putExtra(PauseInterstitialActivity.EXTRA_TARGET_PACKAGE, packageName);
            pauseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(pauseIntent);
        }
    }

    @Override
    public void onInterrupt() {}
}
