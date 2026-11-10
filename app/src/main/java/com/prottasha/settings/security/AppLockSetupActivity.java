package com.prottasha.settings.security;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.databinding.ActivityAppLockSetupBinding;

public final class AppLockSetupActivity extends AppCompatActivity {

    private ActivityAppLockSetupBinding binding;
    private AppLockPolicy policy;
    private SessionUser user;
    private String uidHash;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityAppLockSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProttashaApp app = (ProttashaApp) getApplication();
        try {
            user = app.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException noUser) {
            finish();
            return;
        }

        uidHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();
        policy = AppLockPolicy.load(this, uidHash);

        setupViews();
    }

    private void setupViews() {
        // Master enable switch
        binding.appLockEnableSwitch.setChecked(policy.isLockEnabled());
        binding.appLockEnableSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked && !policy.hasPin()) {
                Toast.makeText(this, "Please set a security PIN first", Toast.LENGTH_SHORT).show();
                binding.appLockEnableSwitch.setChecked(false);
                return;
            }
            policy.setLockEnabled(isChecked);
            policy.save(this, uidHash);
            updateVisibility();
        });

        // Biometrics check
        BiometricManager bm = BiometricManager.from(this);
        int canAuth = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        boolean bioSupported = (canAuth == BiometricManager.BIOMETRIC_SUCCESS);

        if (!bioSupported) {
            binding.appLockBiometricSwitch.setEnabled(false);
            binding.appLockBiometricSwitch.setChecked(false);
        } else {
            binding.appLockBiometricSwitch.setChecked(policy.isBiometricEnabled());
            binding.appLockBiometricSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                policy.setBiometricEnabled(isChecked);
                policy.save(this, uidHash);
            });
        }

        // Timeout Radios
        int timeout = policy.getTimeoutSeconds();
        if (timeout == 0) binding.radioTimeoutImmediate.setChecked(true);
        else if (timeout == 30) binding.radioTimeout30s.setChecked(true);
        else if (timeout == 300) binding.radioTimeout5m.setChecked(true);
        else binding.radioTimeout1m.setChecked(true);

        binding.appLockTimeoutGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_timeout_immediate) policy.setTimeoutSeconds(0);
            else if (checkedId == R.id.radio_timeout_30s) policy.setTimeoutSeconds(30);
            else if (checkedId == R.id.radio_timeout_5m) policy.setTimeoutSeconds(300);
            else policy.setTimeoutSeconds(60);
            policy.save(this, uidHash);
        });

        // Save PIN button
        binding.appLockSavePinButton.setOnClickListener(v -> saveNewPin());

        updateVisibility();
    }

    private void updateVisibility() {
        boolean hasPin = policy.hasPin();
        binding.appLockPinStatus.setText(hasPin ? "PIN is active" : "No PIN configured");
        binding.appLockPinTitle.setText(hasPin ? "Change Security PIN" : "Set Security PIN");
        binding.appLockSavePinButton.setText(hasPin ? "Update PIN" : "Save and Enable PIN");

        binding.appLockBiometricCard.setVisibility(policy.isLockEnabled() ? View.VISIBLE : View.GONE);
        binding.appLockTimeoutCard.setVisibility(policy.isLockEnabled() ? View.VISIBLE : View.GONE);
    }

    private void saveNewPin() {
        String newPin = binding.appLockNewPinInput.getText() != null
                ? binding.appLockNewPinInput.getText().toString().trim()
                : "";
        String confirmPin = binding.appLockConfirmPinInput.getText() != null
                ? binding.appLockConfirmPinInput.getText().toString().trim()
                : "";

        if (newPin.length() < 4 || newPin.length() > 6) {
            binding.appLockNewPinLayout.setError("PIN must be 4 to 6 digits");
            return;
        }
        binding.appLockNewPinLayout.setError(null);

        if (!newPin.equals(confirmPin)) {
            binding.appLockConfirmPinLayout.setError("PINs do not match");
            return;
        }
        binding.appLockConfirmPinLayout.setError(null);

        try {
            PinCredentialStore.Verifier verifier = PinCredentialStore.create(newPin.toCharArray());
            policy.setPinVerifier(verifier);
            policy.setLockEnabled(true);
            policy.save(this, uidHash);

            binding.appLockNewPinInput.setText("");
            binding.appLockConfirmPinInput.setText("");
            binding.appLockEnableSwitch.setChecked(true);
            updateVisibility();

            Toast.makeText(this, "PIN saved. App Lock is enabled.", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Failed to create security credential", Toast.LENGTH_SHORT).show();
        }
    }
}
