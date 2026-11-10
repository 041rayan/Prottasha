package com.prottasha.guard.ui;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentGuardBinding;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.platform.GrayscaleCapabilityController;
import com.prottasha.intervention.platform.AccessibilityServiceHealth;
import com.prottasha.intervention.ui.AccessibilityDisclosureActivity;
import com.prottasha.service.ProttashaAccessibilityService;
import com.prottasha.usage.browser.DomainNormalizer;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class GuardFragment extends Fragment {
    private FragmentGuardBinding binding;
    private GuardViewModel viewModel;
    private GuardRulesAdapter adapter;
    private List<GuardRuleEntity> cachedRules = Collections.emptyList();
    private String currentQuery = "";
    private int currentFilterId = R.id.guard_chip_all;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
        viewModel = new ViewModelProvider(this, new GuardViewModel.Factory(
                app.appGraph().guardRuleRepository())).get(GuardViewModel.class);

        binding.guardAddButton.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), AppPickerActivity.class)));

        binding.guardAddWebsiteButton.setOnClickListener(ignored -> showAddWebsiteDialog());

        binding.guardEnableService.setOnClickListener(ignored -> startActivity(
                new Intent(requireContext(), AccessibilityDisclosureActivity.class)));

        binding.guardHealthFixUsage.setOnClickListener(ignored ->
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));

        binding.guardHealthFixBattery.setOnClickListener(ignored -> {
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
            } catch (Exception fallback) {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            }
        });

        GrayscaleCapabilityController grayscale = new GrayscaleCapabilityController(requireContext());
        binding.guardGrayscaleStatus.setText(grayscale.hasCapability()
                ? R.string.guard_grayscale_available : R.string.guard_grayscale_guidance);

        binding.guardSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s != null ? s.toString() : "";
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.guardFilterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentFilterId = checkedIds.isEmpty() ? R.id.guard_chip_all : checkedIds.get(0);
            applyFilters();
        });

        adapter = new GuardRulesAdapter(new GuardRulesAdapter.Listener() {
            @Override
            public void onToggle(long ruleId, boolean enabled) {
                viewModel.setEnabled(ruleId, enabled);
            }

            @Override
            public void onClick(GuardRuleEntity rule) {
                startActivity(GuardRuleEditActivity.editIntent(requireContext(), rule));
            }

            @Override
            public void onLongClick(GuardRuleEntity rule) {
                confirmDelete(rule);
            }
        });
        binding.guardRules.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.guardRules.setAdapter(adapter);

        viewModel.rules().observe(getViewLifecycleOwner(), rules -> {
            cachedRules = rules != null ? rules : Collections.emptyList();
            applyFilters();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHealthStatus();
    }

    private void updateHealthStatus() {
        if (binding == null) return;
        Context context = getContext();
        if (context == null) return;

        AccessibilityServiceHealth healthChecker = new AccessibilityServiceHealth(context);
        boolean accessibilityEnabled = healthChecker.isEnabled();
        boolean accessibilityRunning = ProttashaAccessibilityService.isRunning();

        if (!accessibilityEnabled) {
            binding.guardHealthAccessibilityAlert.setVisibility(View.VISIBLE);
            binding.guardHealthAccessibilityText.setText(R.string.guard_health_accessibility_missing);
            binding.guardEnableService.setText(R.string.guard_enable_service);
        } else if (!accessibilityRunning) {
            binding.guardHealthAccessibilityAlert.setVisibility(View.VISIBLE);
            binding.guardHealthAccessibilityText.setText("Accessibility service stopped. Tap to reconnect.");
            binding.guardEnableService.setText("Reconnect Service");
        } else {
            binding.guardHealthAccessibilityAlert.setVisibility(View.GONE);
        }

        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        boolean usageGranted = false;
        if (appOps != null) {
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName());
            usageGranted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        binding.guardHealthUsageAlert.setVisibility(usageGranted ? View.GONE : View.VISIBLE);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean ignoringBattery = powerManager != null && powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        binding.guardHealthBatteryAlert.setVisibility(ignoringBattery ? View.GONE : View.VISIBLE);

        boolean allHealthy = accessibilityEnabled && accessibilityRunning && usageGranted && ignoringBattery;
        binding.guardHealthStatusAllGood.setVisibility(allHealthy ? View.VISIBLE : View.GONE);
    }

    private void applyFilters() {
        if (binding == null) return;
        List<GuardRuleEntity> filtered = new ArrayList<>();
        String query = currentQuery.trim().toLowerCase(Locale.ROOT);

        for (GuardRuleEntity rule : cachedRules) {
            if (!query.isEmpty()
                    && !rule.label.toLowerCase(Locale.ROOT).contains(query)
                    && !rule.target.toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }

            if (currentFilterId == R.id.guard_chip_apps && rule.isDomain) continue;
            if (currentFilterId == R.id.guard_chip_websites && !rule.isDomain) continue;
            if (currentFilterId == R.id.guard_chip_pause && !"PAUSE".equals(rule.mode)) continue;
            if (currentFilterId == R.id.guard_chip_budget && !"BUDGET".equals(rule.mode)) continue;
            if (currentFilterId == R.id.guard_chip_block && !"BLOCK".equals(rule.mode)) continue;

            filtered.add(rule);
        }

        render(filtered);
    }

    private void render(List<GuardRuleEntity> rules) {
        binding.guardEmpty.setVisibility(cachedRules.isEmpty() ? View.VISIBLE : View.GONE);
        binding.guardNoMatches.setVisibility(!cachedRules.isEmpty() && rules.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.submitList(rules);
    }

    private void confirmDelete(GuardRuleEntity rule) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.guard_delete)
                .setMessage(getString(R.string.guard_delete_confirm))
                .setPositiveButton(R.string.guard_delete, (dialog, which) -> {
                    viewModel.delete(rule.id);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAddWebsiteDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint(R.string.guard_add_website_hint);
        input.setSingleLine(true);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
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
                            requireContext(), normalized, normalized, true));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
