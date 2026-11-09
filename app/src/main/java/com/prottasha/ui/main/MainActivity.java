package com.prottasha.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.ui.LaunchActivity;
import com.prottasha.databinding.ActivityMainBinding;
import com.prottasha.ui.crisis.CrisisHelpActivity;
import com.prottasha.ui.settings.SettingsActivity;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProttashaApp application = (ProttashaApp) getApplication();
        try {
            application.appGraph().userSessionManager().requireUser();
            application.appGraph().userSessionDatabaseProvider().requireOpen();
        } catch (IllegalStateException noSession) {
            startActivity(new Intent(this, LaunchActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_nav_host);
        if (host == null) {
            throw new IllegalStateException("Main navigation host is missing");
        }
        NavController controller = host.getNavController();
        NavigationUI.setupWithNavController(binding.mainBottomNavigation, controller);
        binding.mainLifelineButton.setOnClickListener(ignored ->
                startActivity(new Intent(this, CrisisHelpActivity.class)));
        binding.mainSettingsButton.setOnClickListener(ignored ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}
