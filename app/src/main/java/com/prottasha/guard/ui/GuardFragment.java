package com.prottasha.guard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentGuardBinding;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.platform.GrayscaleCapabilityController;
import com.prottasha.intervention.ui.AccessibilityDisclosureActivity;

import java.util.List;

public final class GuardFragment extends Fragment {
    private FragmentGuardBinding binding;
    private GuardViewModel viewModel;

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
        binding.guardEnableService.setOnClickListener(ignored -> startActivity(
                new Intent(requireContext(), AccessibilityDisclosureActivity.class)));
        GrayscaleCapabilityController grayscale = new GrayscaleCapabilityController(requireContext());
        binding.guardGrayscaleStatus.setText(grayscale.hasCapability()
                ? R.string.guard_grayscale_available : R.string.guard_grayscale_guidance);
        viewModel.rules().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(List<GuardRuleEntity> rules) {
        binding.guardRules.removeAllViews();
        binding.guardEmpty.setVisibility(rules.isEmpty() ? View.VISIBLE : View.GONE);
        for (GuardRuleEntity rule : rules) {
            TextView row = new TextView(requireContext());
            row.setText(getString(R.string.guard_rule_summary, rule.label, rule.mode,
                    rule.mode.equals("BUDGET") ? rule.budgetMinutes : 0));
            row.setTextColor(getResources().getColor(R.color.color_text_primary, null));
            row.setTextSize(16);
            row.setPadding(16, 20, 16, 20);
            row.setOnClickListener(ignored -> startActivity(
                    GuardRuleEditActivity.editIntent(requireContext(), rule)));
            row.setOnLongClickListener(ignored -> {
                viewModel.delete(rule.id);
                return true;
            });
            binding.guardRules.addView(row);
        }
    }

    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
