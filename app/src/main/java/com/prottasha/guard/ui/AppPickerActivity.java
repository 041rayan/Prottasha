package com.prottasha.guard.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityAppPickerBinding;
import com.prottasha.guard.platform.LaunchableAppSource;
import com.prottasha.usage.browser.DomainNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AppPickerActivity extends AppCompatActivity {
    private ActivityAppPickerBinding binding;
    private List<LaunchableAppSource.AppInfo> allApps = Collections.emptyList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.appPickerAddWebsiteButton.setOnClickListener(v -> showAddWebsiteDialog());

        binding.appPickerSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().calculation().execute(() -> {
            allApps = new LaunchableAppSource(this).listNonEssential();
            runOnUiThread(() -> filterApps(""));
        });
    }

    private void filterApps(String query) {
        binding.appPickerList.removeAllViews();
        String lowerQuery = query.trim().toLowerCase(Locale.ROOT);
        for (LaunchableAppSource.AppInfo item : allApps) {
            if (!lowerQuery.isEmpty() && !item.label.toLowerCase(Locale.ROOT).contains(lowerQuery)
                    && !item.packageName.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                continue;
            }
            TextView row = new TextView(this);
            row.setText(item.label);
            row.setTextSize(16);
            row.setTextColor(getResources().getColor(R.color.color_text_primary, null));
            row.setPadding(24, 24, 24, 24);
            row.setOnClickListener(ignored -> startActivity(
                    GuardRuleEditActivity.createIntent(
                            this, item.packageName, item.label, false)));
            binding.appPickerList.addView(row);
        }
    }

    private void showAddWebsiteDialog() {
        final EditText input = new EditText(this);
        input.setHint(R.string.guard_add_website_hint);
        input.setSingleLine(true);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle(R.string.guard_add_website)
                .setView(input)
                .setPositiveButton(R.string.guard_add, (dialog, which) -> {
                    String raw = input.getText() != null ? input.getText().toString().trim() : "";
                    String normalized = DomainNormalizer.normalize(raw);
                    if (normalized == null || normalized.isEmpty()) {
                        input.setError(getString(R.string.guard_add_website_invalid));
                        return;
                    }
                    startActivity(GuardRuleEditActivity.createIntent(
                            this, normalized, normalized, true));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
