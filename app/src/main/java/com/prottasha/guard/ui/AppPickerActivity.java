package com.prottasha.guard.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityAppPickerBinding;
import com.prottasha.databinding.ItemAppPickerBinding;
import com.prottasha.guard.platform.LaunchableAppSource;
import com.prottasha.usage.browser.DomainNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AppPickerActivity extends AppCompatActivity {
    private ActivityAppPickerBinding binding;
    private List<LaunchableAppSource.AppInfo> allApps = Collections.emptyList();
    private AppPickerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppPickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new AppPickerAdapter(item -> startActivity(
                GuardRuleEditActivity.createIntent(this, item.packageName, item.label, false)));

        binding.appPickerList.setLayoutManager(new LinearLayoutManager(this));
        binding.appPickerList.setAdapter(adapter);

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
        String lowerQuery = query.trim().toLowerCase(Locale.ROOT);
        List<LaunchableAppSource.AppInfo> filtered = new ArrayList<>();
        for (LaunchableAppSource.AppInfo item : allApps) {
            if (!lowerQuery.isEmpty() && !item.label.toLowerCase(Locale.ROOT).contains(lowerQuery)
                    && !item.packageName.toLowerCase(Locale.ROOT).contains(lowerQuery)) {
                continue;
            }
            filtered.add(item);
        }
        adapter.setItems(filtered);
        binding.appPickerEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
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

    interface OnAppClickListener {
        void onAppClick(LaunchableAppSource.AppInfo item);
    }

    private static final class AppPickerAdapter extends RecyclerView.Adapter<AppPickerViewHolder> {
        private final List<LaunchableAppSource.AppInfo> items = new ArrayList<>();
        private final OnAppClickListener listener;

        AppPickerAdapter(OnAppClickListener listener) {
            this.listener = listener;
        }

        void setItems(List<LaunchableAppSource.AppInfo> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AppPickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAppPickerBinding binding = ItemAppPickerBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new AppPickerViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull AppPickerViewHolder holder, int position) {
            LaunchableAppSource.AppInfo item = items.get(position);
            holder.binding.appPickerItemLabel.setText(item.label);
            holder.binding.appPickerItemPackage.setText(item.packageName);
            holder.itemView.setOnClickListener(v -> listener.onAppClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static final class AppPickerViewHolder extends RecyclerView.ViewHolder {
        final ItemAppPickerBinding binding;

        AppPickerViewHolder(ItemAppPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
