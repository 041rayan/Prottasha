package com.prottasha.settings.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.GeneralSecurityException;

public final class AppLockPolicy {

    public static final int DEFAULT_TIMEOUT_SECONDS = 60;
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION_MILLIS = 30_000L;

    private static final String PREFS_NAME = "app_lock_prefs";
    private static final String KEY_ENABLED = "lock_enabled";
    private static final String KEY_BIOMETRIC = "biometric_enabled";
    private static final String KEY_TIMEOUT = "timeout_seconds";
    private static final String KEY_PIN_SALT = "pin_salt";
    private static final String KEY_PIN_HASH = "pin_hash";

    private boolean isLockEnabled = false;
    private boolean isBiometricEnabled = false;
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private PinCredentialStore.Verifier pinVerifier;

    private int failedAttempts = 0;
    private long lockedUntilTimestamp = 0L;

    public AppLockPolicy() {}

    public static AppLockPolicy load(@NonNull Context context, @NonNull String uidHash) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(
                PREFS_NAME + "_" + uidHash, Context.MODE_PRIVATE);
        AppLockPolicy policy = new AppLockPolicy();
        policy.isLockEnabled = prefs.getBoolean(KEY_ENABLED, false);
        policy.isBiometricEnabled = prefs.getBoolean(KEY_BIOMETRIC, false);
        policy.timeoutSeconds = prefs.getInt(KEY_TIMEOUT, DEFAULT_TIMEOUT_SECONDS);

        String saltStr = prefs.getString(KEY_PIN_SALT, null);
        String hashStr = prefs.getString(KEY_PIN_HASH, null);
        if (saltStr != null && hashStr != null) {
            try {
                byte[] salt = Base64.decode(saltStr, Base64.NO_WRAP);
                byte[] hash = Base64.decode(hashStr, Base64.NO_WRAP);
                policy.pinVerifier = new PinCredentialStore.Verifier(salt, hash);
            } catch (Exception ignored) {
            }
        }
        return policy;
    }

    public void save(@NonNull Context context, @NonNull String uidHash) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(
                PREFS_NAME + "_" + uidHash, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit()
                .putBoolean(KEY_ENABLED, isLockEnabled)
                .putBoolean(KEY_BIOMETRIC, isBiometricEnabled)
                .putInt(KEY_TIMEOUT, timeoutSeconds);

        if (pinVerifier != null) {
            editor.putString(KEY_PIN_SALT, Base64.encodeToString(pinVerifier.salt, Base64.NO_WRAP));
            editor.putString(KEY_PIN_HASH, Base64.encodeToString(pinVerifier.hash, Base64.NO_WRAP));
        } else {
            editor.remove(KEY_PIN_SALT);
            editor.remove(KEY_PIN_HASH);
        }
        editor.apply();
    }

    public boolean isLockEnabled() { return isLockEnabled; }
    public void setLockEnabled(boolean enabled) { this.isLockEnabled = enabled; }

    public boolean isBiometricEnabled() { return isBiometricEnabled; }
    public void setBiometricEnabled(boolean enabled) { this.isBiometricEnabled = enabled; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int seconds) { this.timeoutSeconds = Math.max(0, seconds); }

    public boolean hasPin() { return pinVerifier != null; }
    public PinCredentialStore.Verifier getPinVerifier() { return pinVerifier; }
    public void setPinVerifier(PinCredentialStore.Verifier verifier) { this.pinVerifier = verifier; }

    public int getFailedAttempts() { return failedAttempts; }

    public boolean isLockedOut(long now) {
        return now < lockedUntilTimestamp;
    }

    public long getRemainingLockoutSeconds(long now) {
        return Math.max(0, (lockedUntilTimestamp - now) / 1000);
    }

    public boolean verifyPin(char[] pin, long now) throws GeneralSecurityException {
        if (isLockedOut(now)) {
            return false;
        }
        if (pinVerifier == null) {
            return false;
        }
        boolean matches = PinCredentialStore.verify(pin, pinVerifier);
        if (matches) {
            failedAttempts = 0;
            lockedUntilTimestamp = 0L;
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                lockedUntilTimestamp = now + LOCKOUT_DURATION_MILLIS;
            }
            return false;
        }
    }

    public void resetLockout() {
        failedAttempts = 0;
        lockedUntilTimestamp = 0L;
    }

    public static boolean canPerformSensitiveAction(@Nullable AppLockManager lockManager, long now) {
        return lockManager == null || !lockManager.requiresUnlock(now);
    }
}
