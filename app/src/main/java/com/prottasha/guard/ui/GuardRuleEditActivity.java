package com.prottasha.guard.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityGuardRuleEditBinding;
import com.prottasha.guard.data.GuardRuleEntity;

import java.time.LocalTime;
import java.util.Locale;

public final class GuardRuleEditActivity extends AppCompatActivity {
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_TARGET = "target";
    private static final String EXTRA_LABEL = "label";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_IS_DOMAIN = "is_domain";

    private ActivityGuardRuleEditBinding binding;
    private long ruleId;
    private String ruleTarget;
    private String ruleLabel;
    private boolean isDomain;
    private int startMinute = 0;
    private int endMinute = 0;

    public static Intent createIntent(Context context, String target, String label) {
        return createIntent(context, target, label, false);
    }

    public static Intent createIntent(Context context, String target, String label, boolean isDomain) {
        return new Intent(context, GuardRuleEditActivity.class)
                .putExtra(EXTRA_TARGET, target)
                .putExtra(EXTRA_LABEL, label)
                .putExtra(EXTRA_IS_DOMAIN, isDomain);
    }

    public static Intent editIntent(Context context, GuardRuleEntity rule) {
        return createIntent(context, rule.target, rule.label, rule.isDomain)
                .putExtra(EXTRA_ID, rule.id)
                .putExtra(EXTRA_MODE, rule.mode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuardRuleEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ruleId = getIntent().getLongExtra(EXTRA_ID, 0L);
        ruleTarget = getIntent().getStringExtra(EXTRA_TARGET);
        ruleLabel = getIntent().getStringExtra(EXTRA_LABEL);
        isDomain = getIntent().getBooleanExtra(EXTRA_IS_DOMAIN, false);

        if (ruleTarget == null) ruleTarget = "";
        if (ruleLabel == null) ruleLabel = ruleTarget;

        binding.guardRuleTarget.setText(ruleLabel);
        binding.guardRulePackageSub.setText(ruleTarget + (isDomain ? " (Website)" : " (App)"));

        String modeExtra = getIntent().getStringExtra(EXTRA_MODE);
        if ("BUDGET".equals(modeExtra)) {
            binding.guardModeBudget.setChecked(true);
            binding.guardBudgetContainer.setVisibility(View.VISIBLE);
        } else if ("BLOCK".equals(modeExtra)) {
            binding.guardModeBlock.setChecked(true);
            binding.guardBudgetContainer.setVisibility(View.GONE);
        } else {
            binding.guardModePause.setChecked(true);
            binding.guardBudgetContainer.setVisibility(View.GONE);
        }

        binding.guardModeGroup.setOnCheckedChangeListener((group, id) -> {
            binding.guardBudgetContainer.setVisibility(
                    id == R.id.guard_mode_budget ? View.VISIBLE : View.GONE);
        });

        binding.guardScheduleSwitch.setOnCheckedChangeListener((button, checked) -> {
            binding.guardScheduleButtonsContainer.setVisibility(checked ? View.VISIBLE : View.GONE);
            if (!checked) {
                startMinute = 0;
                endMinute = 0;
                updateScheduleButtons();
            }
        });

        binding.guardScheduleStartButton.setOnClickListener(v -> showTimePicker(true));
        binding.guardScheduleEndButton.setOnClickListener(v -> showTimePicker(false));

        binding.guardRuleSave.setOnClickListener(ignored -> save());

        if (ruleId > 0) {
            binding.guardRuleDelete.setVisibility(View.VISIBLE);
            binding.guardRuleDelete.setOnClickListener(ignored -> confirmDelete());
            loadExistingRule();
        } else {
            binding.guardRuleDelete.setVisibility(View.GONE);
            updateScheduleButtons();
        }
    }

    private void loadExistingRule() {
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().guardRuleRepository().getRule(ruleId, result -> runOnUiThread(() -> {
            if (!result.isSuccess() || result.valueOrNull() == null) return;
            GuardRuleEntity rule = result.valueOrNull();
            ruleTarget = rule.target;
            ruleLabel = rule.label;
            isDomain = rule.isDomain;

            binding.guardRuleTarget.setText(rule.label);
            binding.guardRulePackageSub.setText(rule.target + (rule.isDomain ? " (Website)" : " (App)"));
            binding.guardEnabledSwitch.setChecked(rule.enabled);

            if ("BUDGET".equals(rule.mode)) {
                binding.guardModeBudget.setChecked(true);
                binding.guardBudgetContainer.setVisibility(View.VISIBLE);
            } else if ("BLOCK".equals(rule.mode)) {
                binding.guardModeBlock.setChecked(true);
                binding.guardBudgetContainer.setVisibility(View.GONE);
            } else {
                binding.guardModePause.setChecked(true);
                binding.guardBudgetContainer.setVisibility(View.GONE);
            }

            binding.guardBudgetInput.setText(String.valueOf(Math.max(1, rule.budgetMinutes)));
            binding.guardGraceInput.setText(String.valueOf(Math.max(0, rule.graceMinutes)));
            binding.guardDelayInput.setText(String.valueOf(Math.max(1, rule.delaySeconds)));
            binding.guardCooldownInput.setText(String.valueOf(Math.max(1, rule.cooldownMinutes)));
            binding.guardEscalationSwitch.setChecked(rule.escalation);
            binding.guardReplacementInput.setText(rule.replacementAction);
            binding.guardProtectionSwitch.setChecked(rule.editProtection);

            startMinute = rule.startMinute;
            endMinute = rule.endMinute;
            boolean hasSchedule = (startMinute != 0 || endMinute != 0);
            binding.guardScheduleSwitch.setChecked(hasSchedule);
            binding.guardScheduleButtonsContainer.setVisibility(hasSchedule ? View.VISIBLE : View.GONE);
            updateScheduleButtons();

            checkEditProtection(rule);
        }));
    }

    private void checkEditProtection(GuardRuleEntity rule) {
        if (!rule.editProtection) return;
        int currentMinute = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
        boolean inside = insideSchedule(currentMinute, rule.startMinute, rule.endMinute);
        if (inside) {
            binding.guardProtectionBanner.setVisibility(View.VISIBLE);
            binding.guardRuleSave.setEnabled(false);
            binding.guardRuleDelete.setEnabled(false);
            binding.guardEnabledSwitch.setEnabled(false);
            binding.guardModePause.setEnabled(false);
            binding.guardModeBudget.setEnabled(false);
            binding.guardModeBlock.setEnabled(false);
            binding.guardBudgetInput.setEnabled(false);
            binding.guardGraceInput.setEnabled(false);
            binding.guardDelayInput.setEnabled(false);
            binding.guardCooldownInput.setEnabled(false);
            binding.guardEscalationSwitch.setEnabled(false);
            binding.guardReplacementInput.setEnabled(false);
            binding.guardScheduleSwitch.setEnabled(false);
            binding.guardScheduleStartButton.setEnabled(false);
            binding.guardScheduleEndButton.setEnabled(false);
            binding.guardProtectionSwitch.setEnabled(false);
        }
    }

    private static boolean insideSchedule(int minute, int start, int end) {
        if (start == end) return true;
        if (start < end) return minute >= start && minute < end;
        return minute >= start || minute < end;
    }

    private void showTimePicker(boolean isStart) {
        int initialMinute = isStart ? startMinute : endMinute;
        int hour = initialMinute / 60;
        int min = initialMinute % 60;
        new TimePickerDialog(this, (view, selectedHour, selectedMin) -> {
            int computed = selectedHour * 60 + selectedMin;
            if (isStart) {
                startMinute = computed;
            } else {
                endMinute = computed;
            }
            updateScheduleButtons();
        }, hour, min, true).show();
    }

    private void updateScheduleButtons() {
        binding.guardScheduleStartButton.setText(
                String.format(Locale.ROOT, "Start: %02d:%02d", startMinute / 60, startMinute % 60));
        binding.guardScheduleEndButton.setText(
                String.format(Locale.ROOT, "End: %02d:%02d", endMinute / 60, endMinute % 60));
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.guard_delete)
                .setMessage(getString(R.string.guard_delete_confirm))
                .setPositiveButton(R.string.guard_delete, (dialog, which) -> {
                    ProttashaApp app = (ProttashaApp) getApplication();
                    app.appGraph().guardRuleRepository().delete(ruleId, result -> runOnUiThread(() -> {
                        if (result.isSuccess()) {
                            finish();
                        }
                    }));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void save() {
        String mode;
        int checkedId = binding.guardModeGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.guard_mode_block) {
            mode = "BLOCK";
        } else if (checkedId == R.id.guard_mode_budget) {
            mode = "BUDGET";
        } else {
            mode = "PAUSE";
        }

        int budget = 0;
        int grace = 5;
        if ("BUDGET".equals(mode)) {
            try {
                budget = Integer.parseInt(binding.guardBudgetInput.getText().toString().trim());
                if (budget <= 0) throw new NumberFormatException();
            } catch (NumberFormatException invalid) {
                binding.guardBudgetLayout.setError(getString(R.string.guard_budget_required));
                return;
            }
            try {
                grace = Integer.parseInt(binding.guardGraceInput.getText().toString().trim());
            } catch (NumberFormatException ignored) {
                grace = 5;
            }
        }

        int delay = 5;
        try {
            delay = Math.max(1, Integer.parseInt(binding.guardDelayInput.getText().toString().trim()));
        } catch (NumberFormatException ignored) {}

        int cooldown = 15;
        try {
            cooldown = Math.max(1, Integer.parseInt(binding.guardCooldownInput.getText().toString().trim()));
        } catch (NumberFormatException ignored) {}

        boolean enabled = binding.guardEnabledSwitch.isChecked();
        boolean escalation = binding.guardEscalationSwitch.isChecked();
        boolean editProtection = binding.guardProtectionSwitch.isChecked();
        String replacement = binding.guardReplacementInput.getText() != null
                ? binding.guardReplacementInput.getText().toString().trim() : "";

        if (!binding.guardScheduleSwitch.isChecked()) {
            startMinute = 0;
            endMinute = 0;
        }

        GuardRuleEntity rule = new GuardRuleEntity(
                ruleId,
                ruleTarget,
                ruleLabel,
                mode,
                budget,
                startMinute,
                endMinute,
                enabled,
                cooldown,
                delay,
                grace,
                escalation,
                replacement,
                editProtection,
                isDomain
        );

        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().guardRuleRepository().save(rule, result -> runOnUiThread(() -> {
            if (result.isSuccess()) {
                finish();
            } else {
                binding.guardRuleTarget.setError(getString(R.string.guard_save_failed));
            }
        }));
    }
}
