package com.prottasha.insights.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.databinding.ActivityRehearsalBinding;

public final class RehearsalActivity extends AppCompatActivity {
    private ActivityRehearsalBinding binding;
    private long startedAt;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityRehearsalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startedAt = System.currentTimeMillis();

        binding.rehearsalCompleteButton.setOnClickListener(ignored -> {
            String scenario = binding.rehearsalScenario.getText() != null
                    ? binding.rehearsalScenario.getText().toString().trim() : "Mindful Course-Correction";
            int intensity = Math.round(binding.rehearsalIntensitySlider.getValue());
            int durationSeconds = (int) Math.max(1, (System.currentTimeMillis() - startedAt) / 1000L);

            ProttashaApp app = (ProttashaApp) getApplication();
            app.appGraph().executors().disk().execute(() -> {
                app.appGraph().insightsRepository().recordRehearsal(scenario, durationSeconds, intensity);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Rehearsal completed", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }
}
