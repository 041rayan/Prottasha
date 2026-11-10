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
import com.prottasha.databinding.FragmentOnboardingDisclosureBinding;

public final class OnboardingDisclosureFragment extends Fragment {

    private FragmentOnboardingDisclosureBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingDisclosureBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();

        binding.onboardingDisclosureBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.onboardingDisclosureContinueButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_onboardingDisclosureFragment_to_permissionSetupFragment));

        binding.onboardingDisclosureSkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
