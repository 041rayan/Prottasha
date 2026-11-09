package com.prottasha.focus.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.databinding.ActivityFocusBinding;
import com.prottasha.service.ProttashaFocusService;

public final class FocusActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFocusBinding binding = ActivityFocusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FocusViewModel viewModel = new ViewModelProvider(this).get(FocusViewModel.class);
        binding.focusStart.setOnClickListener(ignored -> {
            int minutes;
            try { minutes = Integer.parseInt(binding.focusMinutes.getText().toString()); }
            catch (NumberFormatException invalid) { minutes = 25; }
            viewModel.start(this, minutes);
        });
        binding.focusPause.setOnClickListener(ignored ->
                viewModel.command(this, ProttashaFocusService.ACTION_PAUSE));
        binding.focusResume.setOnClickListener(ignored ->
                viewModel.command(this, ProttashaFocusService.ACTION_RESUME));
        binding.focusEnd.setOnClickListener(ignored ->
                viewModel.command(this, ProttashaFocusService.ACTION_END));
    }
}
