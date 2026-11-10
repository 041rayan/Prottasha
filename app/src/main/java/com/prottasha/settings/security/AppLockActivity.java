package com.prottasha.settings.security;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.databinding.ActivityAppLockBinding;

import java.util.concurrent.Executor;

public final class AppLockActivity extends AppCompatActivity {

    private ActivityAppLockBinding binding;
    private AppLockPolicy policy;
    private AppLockManager lockManager;
    private SessionUser user;
    private String uidHash;

    private final StringBuilder enteredPin = new StringBuilder();
    private CountDownTimer lockoutTimer;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityAppLockBinding.inflate(getLayoutInflater());
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

        if (!policy.isLockEnabled() || !policy.hasPin()) {
            setResult(RESULT_OK);
            finish();
            return;
        }

        lockManager = new AppLockManager(policy.getTimeoutSeconds() * 1000L);

        setupKeypad();
        setupForgotPin();
        checkLockout();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                moveTaskToBack(true);
            }
        });

        if (policy.isBiometricEnabled() && !policy.isLockedOut(System.currentTimeMillis())) {
            showBiometricPrompt();
        }
    }

    private void setupKeypad() {
        View.OnClickListener numListener = v -> {
            if (policy.isLockedOut(System.currentTimeMillis())) return;
            if (v instanceof com.google.android.material.button.MaterialButton) {
                String text = ((com.google.android.material.button.MaterialButton) v).getText().toString();
                if (enteredPin.length() < 6) {
                    enteredPin.append(text);
                    updatePinDisplay();
                    if (enteredPin.length() >= 4) {
                        new Handler(Looper.getMainLooper()).postDelayed(this::verifyEnteredPin, 150);
                    }
                }
            }
        };

        binding.btnNum0.setOnClickListener(numListener);
        binding.btnNum1.setOnClickListener(numListener);
        binding.btnNum2.setOnClickListener(numListener);
        binding.btnNum3.setOnClickListener(numListener);
        binding.btnNum4.setOnClickListener(numListener);
        binding.btnNum5.setOnClickListener(numListener);
        binding.btnNum6.setOnClickListener(numListener);
        binding.btnNum7.setOnClickListener(numListener);
        binding.btnNum8.setOnClickListener(numListener);
        binding.btnNum9.setOnClickListener(numListener);

        binding.btnBackspace.setOnClickListener(v -> {
            if (enteredPin.length() > 0) {
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updatePinDisplay();
            }
        });

        binding.btnBiometric.setOnClickListener(v -> {
            if (policy.isBiometricEnabled() && !policy.isLockedOut(System.currentTimeMillis())) {
                showBiometricPrompt();
            } else {
                Toast.makeText(this, "Biometrics not configured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePinDisplay() {
        if (enteredPin.length() == 0) {
            binding.appLockPinDisplay.setText("");
            return;
        }
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < enteredPin.length(); i++) {
            dots.append("• ");
        }
        binding.appLockPinDisplay.setText(dots.toString().trim());
    }

    private void verifyEnteredPin() {
        if (enteredPin.length() == 0) return;
        long now = System.currentTimeMillis();
        try {
            char[] pinChars = enteredPin.toString().toCharArray();
            boolean success = policy.verifyPin(pinChars, now);
            policy.save(this, uidHash);

            if (success) {
                lockManager.unlock();
                setResult(RESULT_OK);
                finish();
            } else {
                enteredPin.setLength(0);
                updatePinDisplay();

                if (policy.isLockedOut(now)) {
                    checkLockout();
                } else {
                    int remaining = AppLockPolicy.MAX_FAILED_ATTEMPTS - policy.getFailedAttempts();
                    binding.appLockErrorText.setText("Incorrect PIN. " + remaining + " attempts remaining.");
                    binding.appLockErrorText.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Verification error", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLockout() {
        long now = System.currentTimeMillis();
        if (policy.isLockedOut(now)) {
            long remainingSec = policy.getRemainingLockoutSeconds(now);
            binding.appLockErrorText.setVisibility(View.VISIBLE);
            binding.appLockErrorText.setText("Too many attempts. Locked for " + remainingSec + "s");
            setKeypadEnabled(false);

            if (lockoutTimer != null) lockoutTimer.cancel();
            lockoutTimer = new CountDownTimer(remainingSec * 1000L, 1000L) {
                @Override
                public void onTick(long millisUntilFinished) {
                    binding.appLockErrorText.setText("Too many attempts. Locked for "
                            + (millisUntilFinished / 1000) + "s");
                }

                @Override
                public void onFinish() {
                    binding.appLockErrorText.setVisibility(View.INVISIBLE);
                    setKeypadEnabled(true);
                }
            }.start();
        } else {
            setKeypadEnabled(true);
        }
    }

    private void setKeypadEnabled(boolean enabled) {
        binding.appLockKeypad.setEnabled(enabled);
        binding.btnNum0.setEnabled(enabled);
        binding.btnNum1.setEnabled(enabled);
        binding.btnNum2.setEnabled(enabled);
        binding.btnNum3.setEnabled(enabled);
        binding.btnNum4.setEnabled(enabled);
        binding.btnNum5.setEnabled(enabled);
        binding.btnNum6.setEnabled(enabled);
        binding.btnNum7.setEnabled(enabled);
        binding.btnNum8.setEnabled(enabled);
        binding.btnNum9.setEnabled(enabled);
        binding.btnBackspace.setEnabled(enabled);
        binding.btnBiometric.setEnabled(enabled);
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        policy.resetLockout();
                        policy.save(AppLockActivity.this, uidHash);
                        lockManager.unlock();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // User cancelled or biometric error; stay on PIN screen
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Prottasha")
                .setSubtitle("Confirm fingerprint or face to resume")
                .setNegativeButtonText("Use PIN")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupForgotPin() {
        binding.appLockForgotPin.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("Firebase account password");
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
            input.setPadding(padding, padding, padding, padding);

            new AlertDialog.Builder(this)
                    .setTitle("Reset App Lock")
                    .setMessage("Enter your account password to verify your identity and reset your PIN.")
                    .setView(input)
                    .setPositiveButton("Reset PIN", (dialog, which) -> {
                        String password = input.getText().toString().trim();
                        if (password.isEmpty()) return;
                        reauthAndResetPin(password);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void reauthAndResetPin(String password) {
        if (user.email() == null || user.email().isEmpty()) {
            Toast.makeText(this, "Email is unavailable for re-authentication", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(user.email(), password)
                .addOnSuccessListener(result -> {
                    policy.setLockEnabled(false);
                    policy.setPinVerifier(null);
                    policy.resetLockout();
                    policy.save(this, uidHash);
                    lockManager.unlock();
                    Toast.makeText(this, "App Lock has been reset", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Invalid password. Re-authentication failed.", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        if (lockoutTimer != null) {
            lockoutTimer.cancel();
            lockoutTimer = null;
        }
        super.onDestroy();
    }
}
