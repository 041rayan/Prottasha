package com.prottasha.ui.pause;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.data.local.entity.UrgeLogEntity;
import com.prottasha.data.repository.TriggerRepository;
import com.prottasha.databinding.ActivityPauseInterstitialBinding;
import com.prottasha.ui.settings.SettingsActivity;

public class PauseInterstitialActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_PACKAGE = "EXTRA_TARGET_PACKAGE";
    private static final String KEY_REMAINING_SECONDS = "KEY_REMAINING_SECONDS";

    private ActivityPauseInterstitialBinding binding;
    private TriggerRepository triggerRepository;
    private CountDownTimer countDownTimer;
    private ValueAnimator breathingAnimator;
    private String targetPackage = "unknown";
    private int remainingSeconds = 10;
    private int totalFrictionSeconds = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPauseInterstitialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        triggerRepository = new TriggerRepository(getApplication());

        if (getIntent() != null && getIntent().hasExtra(EXTRA_TARGET_PACKAGE)) {
            targetPackage = getIntent().getStringExtra(EXTRA_TARGET_PACKAGE);
        }

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        totalFrictionSeconds = prefs.getInt(SettingsActivity.KEY_PAUSE_DURATION_SECONDS, 10);
        remainingSeconds = totalFrictionSeconds;

        if (savedInstanceState != null) {
            remainingSeconds = savedInstanceState.getInt(KEY_REMAINING_SECONDS, totalFrictionSeconds);
        }

        setupBreathingVisualizer();
        startFrictionCountdown(remainingSeconds);

        // Explicit low-effort exit (EX-01)
        binding.btnStepBack.setOnClickListener(v -> {
            logUrgeDecision(true, "Chose ideal identity and stepped back");
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });

        binding.btnProceedWithIntention.setOnClickListener(v -> showIntentionPromptDialog());
    }

    private void setupBreathingVisualizer() {
        breathingAnimator = ValueAnimator.ofFloat(1.0f, 1.2f);
        breathingAnimator.setDuration(4000);
        breathingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breathingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            if (binding != null) {
                binding.pauseBreathingContainer.setScaleX(scale);
                binding.pauseBreathingContainer.setScaleY(scale);

                if (scale > 1.10f) {
                    binding.tvPauseBreathingText.setText("Hold & exhale slowly…");
                } else {
                    binding.tvPauseBreathingText.setText("Inhale deeply…");
                }
            }
        });
        breathingAnimator.start();
    }

    private void startFrictionCountdown(int seconds) {
        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds = (int) (millisUntilFinished / 1000);
                if (binding != null) {
                    binding.tvPauseCountdown.setText(remainingSeconds + "s");
                }
            }

            @Override
            public void onFinish() {
                remainingSeconds = 0;
                if (binding != null) {
                    binding.tvPauseCountdown.setText("0s");
                    binding.btnProceedWithIntention.setEnabled(true);
                    binding.btnProceedWithIntention.setText("Proceed with conscious reason");
                    binding.btnProceedWithIntention.setTextColor(0xFFF8FAFC);
                }
            }
        }.start();
    }

    private void showIntentionPromptDialog() {
        final EditText etReason = new EditText(this);
        etReason.setHint("Why must you use this app right now?");

        new MaterialAlertDialogBuilder(this)
                .setTitle("State Your Conscious Purpose")
                .setView(etReason)
                .setPositiveButton("Enter", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    logUrgeDecision(false, reason.isEmpty() ? "No intention entered" : reason);
                    finish(); // Drops pause overlay and lets user through
                })
                .setNegativeButton("Cancel & Step Back", (dialog, which) -> {
                    binding.btnStepBack.performClick();
                })
                .show();
    }

    private void logUrgeDecision(boolean resisted, String note) {
        UrgeLogEntity log = new UrgeLogEntity(
                System.currentTimeMillis(),
                targetPackage,
                totalFrictionSeconds,
                resisted,
                note
        );
        triggerRepository.logUrge(log);
        if (resisted) {
            Toast.makeText(this, "Victory recorded! One vote cast for your identity. (AH-01)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_REMAINING_SECONDS, remainingSeconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (breathingAnimator != null) breathingAnimator.cancel();
        binding = null;
    }
}
