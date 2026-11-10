package com.prottasha.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.databinding.ActivitySettingsSecureBinding;
import com.prottasha.focus.ui.FocusActivity;
import com.prottasha.intervention.ui.AccessibilityDisclosureActivity;
import com.prottasha.methodology.MethodologyActivity;
import com.prottasha.settings.account.AccountActivity;
import com.prottasha.settings.privacy.PrivacyActivity;

public final class SettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        ActivitySettingsSecureBinding binding = ActivitySettingsSecureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.settingsAccount.setOnClickListener(v -> open(AccountActivity.class));
        binding.settingsPrivacy.setOnClickListener(v -> open(PrivacyActivity.class));
        binding.settingsGuardPermission.setOnClickListener(v -> open(AccessibilityDisclosureActivity.class));
        binding.settingsFocus.setOnClickListener(v -> open(FocusActivity.class));
        binding.settingsMethodology.setOnClickListener(v -> open(MethodologyActivity.class));
    }

    private void open(Class<?> destination) { startActivity(new Intent(this, destination)); }
}
