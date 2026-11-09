package com.prottasha.intervention.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.databinding.ActivityAccessibilityDisclosureBinding;

public final class AccessibilityDisclosureActivity extends AppCompatActivity {
    public static final int CONSENT_VERSION = 1;
    private static final String PREFS = "accessibility_consent";
    private static final String VERSION = "version";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAccessibilityDisclosureBinding binding =
                ActivityAccessibilityDisclosureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.accessibilityContinue.setEnabled(false);
        binding.accessibilityConsent.setOnCheckedChangeListener((button, checked) ->
                binding.accessibilityContinue.setEnabled(checked));
        binding.accessibilityContinue.setOnClickListener(ignored -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putInt(VERSION, CONSENT_VERSION).apply();
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
    }

    public static boolean hasCurrentConsent(android.content.Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE)
                .getInt(VERSION, 0) == CONSENT_VERSION;
    }
}
