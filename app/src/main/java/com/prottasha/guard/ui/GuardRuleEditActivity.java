package com.prottasha.guard.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityGuardRuleEditBinding;
import com.prottasha.guard.data.GuardRuleEntity;

public final class GuardRuleEditActivity extends AppCompatActivity {
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_TARGET = "target";
    private static final String EXTRA_LABEL = "label";
    private static final String EXTRA_MODE = "mode";
    private ActivityGuardRuleEditBinding binding;

    static Intent createIntent(Context context, String target, String label) {
        return new Intent(context, GuardRuleEditActivity.class)
                .putExtra(EXTRA_TARGET, target).putExtra(EXTRA_LABEL, label);
    }

    static Intent editIntent(Context context, GuardRuleEntity rule) {
        return createIntent(context, rule.target, rule.label)
                .putExtra(EXTRA_ID, rule.id).putExtra(EXTRA_MODE, rule.mode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGuardRuleEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.guardRuleTarget.setText(getIntent().getStringExtra(EXTRA_LABEL));
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if ("BUDGET".equals(mode)) binding.guardModeBudget.setChecked(true);
        else if ("BLOCK".equals(mode)) binding.guardModeBlock.setChecked(true);
        binding.guardModeGroup.setOnCheckedChangeListener((group, id) ->
                binding.guardBudgetLayout.setVisibility(
                        id == R.id.guard_mode_budget ? View.VISIBLE : View.GONE));
        binding.guardRuleSave.setOnClickListener(ignored -> save());
    }

    private void save() {
        String mode = binding.guardModeGroup.getCheckedRadioButtonId() == R.id.guard_mode_block
                ? "BLOCK" : binding.guardModeGroup.getCheckedRadioButtonId() == R.id.guard_mode_budget
                ? "BUDGET" : "PAUSE";
        int budget = 0;
        if ("BUDGET".equals(mode)) {
            try { budget = Math.max(1, Integer.parseInt(binding.guardBudgetInput.getText().toString())); }
            catch (NumberFormatException invalid) {
                binding.guardBudgetLayout.setError(getString(R.string.guard_budget_required));
                return;
            }
        }
        GuardRuleEntity rule = new GuardRuleEntity(
                getIntent().getLongExtra(EXTRA_ID, 0L),
                getIntent().getStringExtra(EXTRA_TARGET),
                getIntent().getStringExtra(EXTRA_LABEL), mode, budget, 0, 0, true);
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().guardRuleRepository().save(rule, result -> runOnUiThread(() -> {
            if (result.isSuccess()) finish();
            else binding.guardRuleTarget.setError(getString(R.string.guard_save_failed));
        }));
    }
}
