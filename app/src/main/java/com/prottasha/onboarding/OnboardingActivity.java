package com.prottasha.onboarding;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
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

        int initialVersion = getIntent().getIntExtra(
                OnboardingViewModel.EXTRA_INITIAL_VERSION, -1);

        ActivityOnboardingBinding binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this, new OnboardingViewModel.Factory(
                this,
                savedInstanceState,
                application.appGraph().userSessionDatabaseProvider(),
                application.appGraph().identityRepository(),
                application.appGraph().guardRuleRepository(),
                application.appGraph().executors().disk(),
                initialVersion))
                .get(OnboardingViewModel.class);

        if (savedInstanceState == null) {
            viewModel.startStage().observe(this, stage -> {
                if (stage == OnboardingViewModel.Stage.TARGETS) {
                    NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.onboarding_nav_host);
                    if (navHost != null && navHost.getNavController().getCurrentDestination() != null
                            && navHost.getNavController().getCurrentDestination().getId() == R.id.purposeFragment) {
                        navHost.getNavController().navigate(R.id.targetSelectionFragment);
                    }
                }
            });
        }

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
