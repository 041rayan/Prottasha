package com.prottasha.ui.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.prottasha.R;
import com.prottasha.data.firebase.FirebaseRealtimeManager;
import com.prottasha.databinding.ActivityMainBinding;
import com.prottasha.ui.crisis.CrisisHelpActivity;
import com.prottasha.ui.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseRealtimeManager realtimeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        realtimeManager = new FirebaseRealtimeManager();
        setupBottomNavigation();
        setupHeaderActions();
        observePresence();

        if (savedInstanceState == null) {
            loadFragment(new IdentityHabitsFragment());
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment;
            if (itemId == R.id.nav_identity) {
                targetFragment = new IdentityHabitsFragment();
            } else if (itemId == R.id.nav_tracker) {
                targetFragment = new TrackerFragment();
            } else if (itemId == R.id.nav_toolkit) {
                targetFragment = new MindfulToolkitFragment();
            } else if (itemId == R.id.nav_community) {
                targetFragment = new SocialJournalFragment();
            } else {
                return false;
            }
            loadFragment(targetFragment);
            return true;
        });
    }

    private void setupHeaderActions() {
        binding.btnCrisisHelp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrisisHelpActivity.class);
            startActivity(intent);
        });

        binding.btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void observePresence() {
        realtimeManager.getActiveMemberCount(count -> {
            runOnUiThread(() -> {
                if (binding != null) {
                    binding.tvLivePresence.setText("• " + count + " active");
                }
            });
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
