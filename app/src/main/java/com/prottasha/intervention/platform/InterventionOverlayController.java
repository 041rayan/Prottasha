package com.prottasha.intervention.platform;

import android.accessibilityservice.AccessibilityService;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import com.prottasha.intervention.data.InterventionEventRepository;
import com.prottasha.intervention.ui.InterventionOverlayView;

public final class InterventionOverlayController {
    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final InterventionEventRepository events;
    private InterventionOverlayView current;

    public InterventionOverlayController(
            AccessibilityService service, InterventionEventRepository events) {
        this.service = service;
        this.events = events;
        windowManager = service.getSystemService(WindowManager.class);
    }

    public void show(String target, String mode) {
        if (current != null || windowManager == null) return;
        boolean block = "BLOCK".equals(mode);
        current = new InterventionOverlayView(service, target, block,
                new InterventionOverlayView.Listener() {
                    @Override public void onStepBack() {
                        events.record(target, "STEP_BACK", "");
                        remove();
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    }

                    @Override public void onContinue(String intention) {
                        events.record(target, "CONTINUE", intention);
                        remove();
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
        events.record(target, block ? "BLOCKED" : "SHOWN", "");
    }

    public void remove() {
        if (current != null && windowManager != null) {
            windowManager.removeView(current);
            current = null;
        }
    }
}
