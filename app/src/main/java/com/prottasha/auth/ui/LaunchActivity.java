package com.prottasha.auth.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.data.AuthRepository;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.onboarding.OnboardingActivity;
import com.prottasha.onboarding.OnboardingViewModel;
import com.prottasha.ui.main.MainActivity;

@SuppressLint("CustomSplashScreen") // Uses Core SplashScreen; AGP 8.11 still flags the launcher class.
public final class LaunchActivity extends AppCompatActivity {

    private boolean routed;
    private boolean routingSecureSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        ProttashaApp application = (ProttashaApp) getApplication();
        AuthRepository authRepository = application.appGraph().authRepository();
        splashScreen.setKeepOnScreenCondition(() ->
                !routed && (routingSecureSession
                        || authRepository.currentState().status() == AuthState.Status.LOADING));
        authRepository.observeState().observe(this, this::route);
    }

    private void route(AuthState state) {
        if (routed || routingSecureSession || state.status() == AuthState.Status.LOADING) {
            return;
        }
        if ((state.status() == AuthState.Status.VERIFIED_ONLINE
                || state.status() == AuthState.Status.VERIFIED_OFFLINE)
                && state.userOrNull() != null) {
            routingSecureSession = true;
            ProttashaApp application = (ProttashaApp) getApplication();
            application.appGraph().executors().disk().execute(() -> {
                try {
                    application.appGraph().userSessionManager().switchTo(state.userOrNull());
                    int version = application.appGraph().userSessionDatabaseProvider()
                            .requireOpen().localAccountDao().requireAccount().onboardingVersion;
                    Class<?> destination = version < OnboardingViewModel.CURRENT_VERSION
                            ? OnboardingActivity.class : MainActivity.class;
                    runOnUiThread(() -> finishRoute(destination));
                } catch (Exception secureSessionFailure) {
                    runOnUiThread(() -> finishRoute(AuthActivity.class));
                }
            });
            return;
        }
        finishRoute(AuthActivity.class);
    }

    private void finishRoute(Class<?> destination) {
        if (routed) {
            return;
        }
        routed = true;
        startActivity(new Intent(this, destination));
        finish();
    }
}
