package com.prottasha.guard.platform;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

public final class GrayscaleCapabilityController {
    public enum Result { ENABLED, DISABLED, PERMISSION_REQUIRED, FAILED }

    private final Context context;

    public GrayscaleCapabilityController(Context context) {
        this.context = context.getApplicationContext();
    }

    public boolean hasCapability() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public Result setEnabled(boolean enabled) {
        if (!hasCapability()) return Result.PERMISSION_REQUIRED;
        try {
            Settings.Secure.putInt(context.getContentResolver(),
                    "accessibility_display_daltonizer_enabled", enabled ? 1 : 0);
            if (enabled) {
                Settings.Secure.putInt(context.getContentResolver(),
                        "accessibility_display_daltonizer", 0);
            }
            return enabled ? Result.ENABLED : Result.DISABLED;
        } catch (SecurityException failure) {
            return Result.FAILED;
        }
    }
}
