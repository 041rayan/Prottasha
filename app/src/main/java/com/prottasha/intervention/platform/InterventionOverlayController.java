package com.prottasha.intervention.platform;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.prottasha.focus.ui.FocusActivity;
import com.prottasha.intervention.data.InterventionEventRepository;
import com.prottasha.intervention.ui.InterventionOverlayView;

public final class InterventionOverlayController {
    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final InterventionEventRepository events;
    @Nullable
    private InterventionOverlayView current;
    private long currentEventId = 0L;

    public InterventionOverlayController(
            AccessibilityService service, InterventionEventRepository events) {
        this.service = service;
        this.events = events;
        windowManager = service.getSystemService(WindowManager.class);
    }

    public void show(String target, String mode) {
        show(0L, target, mode, 0, "");
    }

    public void show(long ruleId, String target, String mode, int delaySeconds, String replacementAction) {
        if (current != null || windowManager == null) return;
        boolean block = "BLOCK".equals(mode);

        events.recordPromptShown(ruleId, target, mode, delaySeconds, "ACCESSIBILITY", result -> {
            if (result.isSuccess() && result.valueOrNull() != null) {
                currentEventId = result.valueOrNull();
            }
        });

        current = new InterventionOverlayView(
                service,
                target,
                block,
                delaySeconds,
                replacementAction,
                new InterventionOverlayView.Listener() {
                    @Override
                    public void onStepBack() {
                        events.recordResolution(currentEventId, "STEP_BACK", "", null);
                        remove();
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    }

                    @Override
                    public void onContinue(String intention) {
                        events.recordResolution(currentEventId, "CONTINUED", intention, null);
                        remove();
                    }

                    @Override
                    public void onReplacement(String replacement) {
                        events.recordResolution(currentEventId, "REPLACEMENT_CHOSEN", replacement, null);
                        remove();
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    }

                    @Override
                    public void onStartFocus() {
                        events.recordResolution(currentEventId, "FOCUS_STARTED", "Focus Session", null);
                        remove();
                        Intent focusIntent = new Intent(service, FocusActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        service.startActivity(focusIntent);
                    }
                });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        windowManager.addView(current, params);
    }

    public void remove() {
        if (current != null && windowManager != null) {
            windowManager.removeView(current);
            current = null;
            currentEventId = 0L;
        }
    }
}
