package com.prottasha.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.ui.LaunchActivity;
import com.prottasha.databinding.ActivityOnboardingBinding;
import com.prottasha.ui.main.MainActivity;

public final class OnboardingActivity extends AppCompatActivity {

    private OnboardingViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProttashaApp application = (ProttashaApp) getApplication();
        try {
            application.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException noSession) {
            startActivity(new Intent(this, LaunchActivity.class));
            finish();
            return;
        }
        ActivityOnboardingBinding binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this, new OnboardingViewModel.Factory(
                this,
                savedInstanceState,
                application.appGraph().userSessionDatabaseProvider(),
                application.appGraph().identityRepository(),
                application.appGraph().executors().disk()))
                .get(OnboardingViewModel.class);
        viewModel.finished().observe(this, complete -> {
            if (Boolean.TRUE.equals(complete)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    public OnboardingViewModel onboardingViewModel() {
        return viewModel;
    }
}
