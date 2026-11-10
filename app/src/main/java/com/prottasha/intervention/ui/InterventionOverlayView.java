package com.prottasha.intervention.ui;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prottasha.R;
import com.prottasha.databinding.ViewInterventionOverlayBinding;

import java.util.Locale;

public final class InterventionOverlayView extends FrameLayout {

    public interface Listener {
        void onStepBack();
        void onContinue(String intention);
        default void onReplacement(String replacement) {}
        default void onStartFocus() {}
    }

    private final ViewInterventionOverlayBinding binding;
    @Nullable
    private CountDownTimer countDownTimer;

    public InterventionOverlayView(
            @NonNull Context context,
            @NonNull String target,
            boolean block,
            @NonNull Listener listener) {
        this(context, target, block, 0, "", listener);
    }

    public InterventionOverlayView(
            @NonNull Context context,
            @NonNull String target,
            boolean block,
            int delaySeconds,
            @Nullable String replacementAction,
            @NonNull Listener listener) {
        super(context);
        binding = ViewInterventionOverlayBinding.inflate(LayoutInflater.from(context), this, true);
        binding.interventionTarget.setText(target);

        if (block) {
            binding.interventionTitle.setText("Access Blocked");
            binding.interventionContinue.setVisibility(GONE);
            binding.interventionIntentionLayout.setVisibility(GONE);
            binding.interventionCountdownText.setVisibility(GONE);
        } else {
            binding.interventionTitle.setText(R.string.intervention_title);
            binding.interventionContinue.setVisibility(VISIBLE);
            binding.interventionIntentionLayout.setVisibility(VISIBLE);

            if (delaySeconds > 0) {
                binding.interventionContinue.setEnabled(false);
                binding.interventionCountdownText.setVisibility(VISIBLE);
                startDelayCountdown(delaySeconds);
            } else {
                binding.interventionContinue.setEnabled(true);
                binding.interventionCountdownText.setVisibility(GONE);
            }
        }

        if (replacementAction != null && !replacementAction.trim().isEmpty()) {
            binding.interventionReplacementButton.setVisibility(VISIBLE);
            binding.interventionReplacementButton.setText("Try: " + replacementAction.trim());
            binding.interventionReplacementButton.setOnClickListener(v -> {
                cancelTimer();
                listener.onReplacement(replacementAction.trim());
            });
        } else {
            binding.interventionReplacementButton.setVisibility(GONE);
        }

        binding.interventionStepBack.setOnClickListener(ignored -> {
            cancelTimer();
            listener.onStepBack();
        });

        binding.interventionFocusButton.setOnClickListener(ignored -> {
            cancelTimer();
            listener.onStartFocus();
        });

        binding.interventionContinue.setOnClickListener(ignored -> {
            String intention = binding.interventionIntention.getText() == null
                    ? "" : binding.interventionIntention.getText().toString();
            if (intention.trim().isEmpty()) {
                binding.interventionIntentionLayout.setError(
                        getContext().getString(R.string.intervention_intention_required));
            } else {
                cancelTimer();
                listener.onContinue(intention.trim());
            }
        });
    }

    private void startDelayCountdown(int totalSeconds) {
        countDownTimer = new CountDownTimer(totalSeconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                binding.interventionCountdownText.setText(
                        String.format(Locale.ROOT, "Take a breath. Continue unlocks in %ds", secondsLeft));
            }

            @Override
            public void onFinish() {
                binding.interventionCountdownText.setText("Breathing complete.");
                binding.interventionContinue.setEnabled(true);
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelTimer();
        super.onDetachedFromWindow();
    }
}
