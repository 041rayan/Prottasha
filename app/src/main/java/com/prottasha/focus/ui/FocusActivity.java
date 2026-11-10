package com.prottasha.focus.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityFocusBinding;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.service.ProttashaFocusService;

import java.util.Locale;

public final class FocusActivity extends AppCompatActivity {
    private ActivityFocusBinding binding;
    private FocusViewModel viewModel;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable ticker;
    private FocusSessionEntity activeSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFocusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(FocusViewModel.class);

        binding.focusStart.setOnClickListener(ignored -> {
            int minutes;
            try {
                minutes = Integer.parseInt(binding.focusMinutes.getText().toString().trim());
                if (minutes <= 0) minutes = 25;
            } catch (NumberFormatException invalid) {
                minutes = 25;
            }
            viewModel.start(this, minutes);
            // Refresh state after starting
            handler.postDelayed(this::refreshSession, 200L);
        });

        binding.focusPause.setOnClickListener(ignored -> {
            viewModel.command(this, ProttashaFocusService.ACTION_PAUSE);
            handler.postDelayed(this::refreshSession, 200L);
        });

        binding.focusResume.setOnClickListener(ignored -> {
            viewModel.command(this, ProttashaFocusService.ACTION_RESUME);
            handler.postDelayed(this::refreshSession, 200L);
        });

        binding.focusEnd.setOnClickListener(ignored -> {
            viewModel.command(this, ProttashaFocusService.ACTION_END);
            stopTicker();
            activeSession = null;
            renderIdleState();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSession();
    }

    @Override
    protected void onPause() {
        stopTicker();
        super.onPause();
    }

    private void refreshSession() {
        ProttashaApp app = (ProttashaApp) getApplication();
        viewModel.loadSession(app, result -> runOnUiThread(() -> {
            if (result.isSuccess() && result.valueOrNull() != null) {
                activeSession = result.valueOrNull();
                renderSession(activeSession);
            } else {
                activeSession = null;
                renderIdleState();
            }
        }));
    }

    private void renderIdleState() {
        stopTicker();
        binding.focusSummaryCard.setVisibility(View.GONE);
        binding.focusInputContainer.setVisibility(View.VISIBLE);
        binding.focusStart.setVisibility(View.VISIBLE);
        binding.focusActiveControls.setVisibility(View.GONE);
        binding.focusStatusLabel.setText(R.string.focus_status_ready);

        int minutes = 25;
        try {
            minutes = Integer.parseInt(binding.focusMinutes.getText().toString().trim());
        } catch (Exception ignored) {}
        binding.focusCountdown.setText(String.format(Locale.ROOT, "%02d:00", minutes));
    }

    private void renderSession(FocusSessionEntity session) {
        binding.focusInputContainer.setVisibility(View.GONE);
        binding.focusStart.setVisibility(View.GONE);
        binding.focusActiveControls.setVisibility(View.VISIBLE);
        binding.focusSummaryCard.setVisibility(View.GONE);

        long now = System.currentTimeMillis();
        long elapsed = session.accumulatedMillis + ("PAUSED".equals(session.state) ? 0 : Math.max(0, now - session.startedAt));
        long remainingMillis = Math.max(0, session.durationMillis - elapsed);

        if (remainingMillis == 0) {
            stopTicker();
            binding.focusCountdown.setText("00:00");
            binding.focusStatusLabel.setText(R.string.focus_status_completed);
            binding.focusActiveControls.setVisibility(View.GONE);
            binding.focusSummaryCard.setVisibility(View.VISIBLE);
            binding.focusStart.setVisibility(View.VISIBLE);
            return;
        }

        int totalSeconds = (int) (remainingMillis / 1000L);
        binding.focusCountdown.setText(String.format(Locale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60));

        if ("PAUSED".equals(session.state)) {
            stopTicker();
            binding.focusStatusLabel.setText(R.string.focus_status_paused);
            binding.focusPause.setVisibility(View.GONE);
            binding.focusResume.setVisibility(View.VISIBLE);
        } else {
            binding.focusStatusLabel.setText(R.string.focus_status_running);
            binding.focusPause.setVisibility(View.VISIBLE);
            binding.focusResume.setVisibility(View.GONE);
            startTicker();
        }
    }

    private void startTicker() {
        stopTicker();
        ticker = new Runnable() {
            @Override
            public void run() {
                if (activeSession == null || !"RUNNING".equals(activeSession.state)) return;
                long now = System.currentTimeMillis();
                long elapsed = activeSession.accumulatedMillis + Math.max(0, now - activeSession.startedAt);
                long remainingMillis = Math.max(0, activeSession.durationMillis - elapsed);

                if (remainingMillis == 0) {
                    renderSession(activeSession);
                    return;
                }

                int totalSeconds = (int) (remainingMillis / 1000L);
                binding.focusCountdown.setText(
                        String.format(Locale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60));
                handler.postDelayed(this, 1000L);
            }
        };
        handler.postDelayed(ticker, 1000L);
    }

    private void stopTicker() {
        if (ticker != null) {
            handler.removeCallbacks(ticker);
            ticker = null;
        }
    }
}
