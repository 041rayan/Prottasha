package com.prottasha.circle.ui;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class DeterministicAvatar {

    private static final int[] PALETTE = new int[] {
            Color.parseColor("#10B981"), // Emerald
            Color.parseColor("#0284C7"), // Ocean Blue
            Color.parseColor("#D97706"), // Amber Dark
            Color.parseColor("#7C3AED"), // Violet
            Color.parseColor("#DB2777"), // Pink
            Color.parseColor("#0D9488"), // Teal
            Color.parseColor("#E11D48"), // Rose
            Color.parseColor("#4F46E5")  // Indigo
    };

    private DeterministicAvatar() {}

    @ColorInt
    public static int getColor(@Nullable String seed) {
        if (seed == null || seed.trim().isEmpty()) {
            return PALETTE[0];
        }
        int hash = Math.abs(seed.hashCode());
        return PALETTE[hash % PALETTE.length];
    }

    @NonNull
    public static String getInitials(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String trimmed = name.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 2) {
            String first = parts[0].substring(0, 1);
            String second = parts[1].substring(0, 1);
            return (first + second).toUpperCase();
        }
        if (trimmed.length() >= 2) {
            return trimmed.substring(0, 2).toUpperCase();
        }
        return trimmed.substring(0, 1).toUpperCase();
    }
}
