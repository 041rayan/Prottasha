package com.prottasha.circle.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public final class AccountabilityPartner {
    public final String name;
    public final String phoneNumber;
    public final long connectedAt;
    public final boolean isActive;

    public AccountabilityPartner(
            @NonNull String name,
            @NonNull String phoneNumber,
            long connectedAt,
            boolean isActive) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.connectedAt = connectedAt;
        this.isActive = isActive;
    }

    public static boolean isValidPhoneNumber(@Nullable String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        return trimmed.matches("^[+]?[0-9]{7,15}$");
    }

    @NonNull
    public static String generateShareNote(
            @NonNull String partnerName,
            @NonNull List<String> habitNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(partnerName.trim()).append(",\n\n");
        sb.append("Here is my Prottasha recovery commitment update.\n");
        if (habitNames.isEmpty()) {
            sb.append("I am currently setting up my recovery habits and commitments.\n");
        } else {
            sb.append("My active daily recovery habits:\n");
            for (String habit : habitNames) {
                sb.append("• ").append(habit).append("\n");
            }
        }
        sb.append("\nThank you for being my accountability partner.");
        return sb.toString();
    }
}
