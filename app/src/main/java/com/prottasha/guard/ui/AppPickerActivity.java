package com.prottasha.guard.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.databinding.ActivityAppPickerBinding;
import com.prottasha.guard.platform.LaunchableAppSource;

public final class AppPickerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAppPickerBinding binding = ActivityAppPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().calculation().execute(() -> {
            java.util.List<LaunchableAppSource.AppInfo> apps =
                    new LaunchableAppSource(this).listNonEssential();
            runOnUiThread(() -> {
                for (LaunchableAppSource.AppInfo item : apps) {
                    TextView row = new TextView(this);
                    row.setText(item.label);
                    row.setTextSize(16);
                    row.setPadding(24, 20, 24, 20);
                    row.setOnClickListener(ignored -> startActivity(
                            GuardRuleEditActivity.createIntent(
                                    this, item.packageName, item.label)));
                    binding.appPickerList.addView(row);
                }
            });
        });
    }
}
