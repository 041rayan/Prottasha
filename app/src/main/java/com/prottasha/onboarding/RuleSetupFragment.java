package com.prottasha.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.R;
import com.prottasha.databinding.FragmentOnboardingRulesBinding;

public final class RuleSetupFragment extends Fragment {

    private FragmentOnboardingRulesBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingRulesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();

        String currentMode = viewModel.draft().targetMode;
        if ("BUDGET".equals(currentMode)) {
            binding.onboardingModeBudgetRadio.setChecked(true);
        } else if ("BLOCK".equals(currentMode)) {
            binding.onboardingModeBlockRadio.setChecked(true);
        } else {
            binding.onboardingModePauseRadio.setChecked(true);
        }

        binding.onboardingRulesBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.onboardingRulesContinueButton.setOnClickListener(ignored -> {
            String selectedMode = "PAUSE";
            int checkedId = binding.onboardingModeGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.onboarding_mode_budget_radio) {
                selectedMode = "BUDGET";
            } else if (checkedId == R.id.onboarding_mode_block_radio) {
                selectedMode = "BLOCK";
            }
            viewModel.setTargetMode(selectedMode);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_ruleSetupFragment_to_onboardingDisclosureFragment);
        });

        binding.onboardingRulesSkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
