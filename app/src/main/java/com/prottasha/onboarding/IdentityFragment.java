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
        binding.onboardingBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());
        binding.onboardingFinishButton.setOnClickListener(ignored -> {
            String statement = text();
            if (statement.trim().isEmpty()) {
                binding.onboardingIdentityLayout.setError(
                        getString(R.string.onboarding_identity_required));
                return;
            }
            viewModel.setIdentityStatement(statement);
            viewModel.complete(false);
        });
        binding.onboardingIdentitySkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    private String text() {
        return binding.onboardingIdentityInput.getText() == null
                ? "" : binding.onboardingIdentityInput.getText().toString();
    }

    @Override
    public void onStop() {
        ((OnboardingActivity) requireActivity()).onboardingViewModel()
                .setIdentityStatement(text());
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
