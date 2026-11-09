package com.prottasha.util;

import android.content.Context;
import android.provider.Settings;

public class GrayscaleManager {

    /**
     * Enables or disables system-wide grayscale mode (EX-02).
     * Requires WRITE_SECURE_SETTINGS permission granted via ADB:
     * adb shell pm grant com.prottasha android.permission.WRITE_SECURE_SETTINGS
     */
    public static boolean setGrayscaleEnabled(Context context, boolean enabled) {
        try {
            int daltonizerMode = enabled ? 0 : -1; // 0 = Monochromacy (Grayscale)
            Settings.Secure.putInt(
                    context.getContentResolver(),
                    "accessibility_display_daltonizer_enabled",
                    enabled ? 1 : 0
            );
            Settings.Secure.putInt(
                    context.getContentResolver(),
                    "accessibility_display_daltonizer",
                    daltonizerMode
            );
            return true;
        } catch (SecurityException e) {
            // WRITE_SECURE_SETTINGS not granted
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
