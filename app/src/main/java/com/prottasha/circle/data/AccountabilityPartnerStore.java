package com.prottasha.circle.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AccountabilityPartnerStore {

    private static final String PREFS_PREFIX = "accountability_partner_";
    private static final String KEY_NAME = "partner_name";
    private static final String KEY_PHONE = "partner_phone";
    private static final String KEY_CONNECTED_AT = "connected_at";
    private static final String KEY_IS_ACTIVE = "is_active";

    private final SharedPreferences prefs;

    public AccountabilityPartnerStore(@NonNull Context context, @NonNull String uid) {
        this.prefs = context.getApplicationContext().getSharedPreferences(
                PREFS_PREFIX + uid.hashCode(), Context.MODE_PRIVATE);
    }

    public void save(@NonNull AccountabilityPartner partner) {
        prefs.edit()
                .putString(KEY_NAME, partner.name)
                .putString(KEY_PHONE, partner.phoneNumber)
                .putLong(KEY_CONNECTED_AT, partner.connectedAt)
                .putBoolean(KEY_IS_ACTIVE, partner.isActive)
                .apply();
    }

    @Nullable
    public AccountabilityPartner get() {
        if (!prefs.contains(KEY_NAME)) {
            return null;
        }
        String name = prefs.getString(KEY_NAME, "");
        String phone = prefs.getString(KEY_PHONE, "");
        long connectedAt = prefs.getLong(KEY_CONNECTED_AT, 0L);
        boolean isActive = prefs.getBoolean(KEY_IS_ACTIVE, false);
        return new AccountabilityPartner(name, phone, connectedAt, isActive);
    }

    public void revoke() {
        prefs.edit().clear().apply();
    }
}
