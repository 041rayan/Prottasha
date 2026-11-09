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

@SuppressLint("CustomSplashScreen") // Uses Core SplashScreen; AGP 8.11 still flags the launcher class.
public final class LaunchActivity extends AppCompatActivity {

    private boolean routed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        ProttashaApp application = (ProttashaApp) getApplication();
        AuthRepository authRepository = application.appGraph().authRepository();
        splashScreen.setKeepOnScreenCondition(() ->
                !routed && authRepository.currentState().status() == AuthState.Status.LOADING);
        authRepository.observeState().observe(this, this::route);
    }

    private void route(AuthState state) {
        if (routed || state.status() == AuthState.Status.LOADING) {
            return;
        }
        routed = true;
        // Task 4 replaces the verified-pending Auth state with encrypted-session routing.
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }
}
