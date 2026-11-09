package com.prottasha.insights.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.databinding.ActivityReflectionBinding;
import com.prottasha.insights.data.InsightsRepository;

import java.time.LocalDate;

public final class ReflectionActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        ActivityReflectionBinding binding = ActivityReflectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.reflectionSave.setOnClickListener(ignored -> {
            String fact = binding.reflectionFact.getText().toString().trim();
            String interpretation = binding.reflectionInterpretation.getText().toString().trim();
            String lesson = binding.reflectionLesson.getText().toString().trim();
            ProttashaApp app = (ProttashaApp) getApplication();
            app.appGraph().executors().disk().execute(() -> {
                new InsightsRepository(app.appGraph().userSessionDatabaseProvider())
                        .saveReflection(LocalDate.now().toEpochDay(), fact, interpretation, lesson);
                runOnUiThread(this::finish);
            });
        });
    }
}
