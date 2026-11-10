package com.prottasha.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import com.prottasha.R;
import com.prottasha.databinding.FragmentOnboardingTargetsBinding;
import com.prottasha.guard.platform.LaunchableAppSource;

import java.util.ArrayList;
import java.util.List;

public final class TargetSelectionFragment extends Fragment {

    private FragmentOnboardingTargetsBinding binding;
    private final List<String> selectedPackages = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingTargetsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();
        selectedPackages.clear();
        selectedPackages.addAll(viewModel.draft().selectedPackages);

        LaunchableAppSource appSource = new LaunchableAppSource(requireContext());
        List<LaunchableAppSource.AppInfo> apps = appSource.listNonEssential();

        for (LaunchableAppSource.AppInfo app : apps) {
            Chip chip = new Chip(requireContext());
            chip.setText(app.label);
            chip.setCheckable(true);
            chip.setChecked(selectedPackages.contains(app.packageName));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedPackages.contains(app.packageName)) {
                        selectedPackages.add(app.packageName);
                    }
                } else {
                    selectedPackages.remove(app.packageName);
                }
                viewModel.setSelectedPackages(selectedPackages);
            });
            binding.onboardingTargetsChipGroup.addView(chip);
        }

        binding.onboardingTargetsBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.onboardingTargetsContinueButton.setOnClickListener(ignored -> {
            viewModel.setSelectedPackages(selectedPackages);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_targetSelectionFragment_to_ruleSetupFragment);
        });

        binding.onboardingTargetsSkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
