package com.prottasha.intervention.platform;

public final class InterventionController {
    private final InterventionOverlayController overlay;

    public InterventionController(InterventionOverlayController overlay) {
        this.overlay = overlay;
    }

    public void intervene(String target, String mode) {
        overlay.show(target, mode);
    }
}
