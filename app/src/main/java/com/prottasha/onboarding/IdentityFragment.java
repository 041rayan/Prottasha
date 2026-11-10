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
import com.prottasha.databinding.FragmentOnboardingIdentityBinding;

public final class IdentityFragment extends Fragment {

    private FragmentOnboardingIdentityBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingIdentityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();
        binding.onboardingIdentityInput.setText(viewModel.draft().identityStatement);
        binding.onboardingSuccessMemoryInput.setText(viewModel.draft().successMemory);

        binding.onboardingBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.onboardingContinueButton.setOnClickListener(ignored -> {
            String statement = identityText();
            if (statement.trim().isEmpty()) {
                binding.onboardingIdentityLayout.setError(
                        getString(R.string.onboarding_identity_required));
                return;
            }
            binding.onboardingIdentityLayout.setError(null);
            viewModel.setIdentityStatement(statement);
            viewModel.setSuccessMemory(successMemoryText());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_identityFragment_to_targetSelectionFragment);
        });

        binding.onboardingFinishButton.setOnClickListener(ignored -> {
            String statement = identityText();
            if (statement.trim().isEmpty()) {
                binding.onboardingIdentityLayout.setError(
                        getString(R.string.onboarding_identity_required));
                return;
            }
            viewModel.setIdentityStatement(statement);
            viewModel.setSuccessMemory(successMemoryText());
            viewModel.complete(false);
        });

        binding.onboardingIdentitySkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    private String identityText() {
        return binding.onboardingIdentityInput.getText() == null
                ? "" : binding.onboardingIdentityInput.getText().toString();
    }

    private String successMemoryText() {
        return binding.onboardingSuccessMemoryInput.getText() == null
                ? "" : binding.onboardingSuccessMemoryInput.getText().toString();
    }

    @Override
    public void onStop() {
        OnboardingViewModel vm = ((OnboardingActivity) requireActivity()).onboardingViewModel();
        vm.setIdentityStatement(identityText());
        vm.setSuccessMemory(successMemoryText());
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
