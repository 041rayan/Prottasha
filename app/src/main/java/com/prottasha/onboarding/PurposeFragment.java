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
import com.prottasha.databinding.FragmentOnboardingPurposeBinding;

public final class PurposeFragment extends Fragment {

    private FragmentOnboardingPurposeBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPurposeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();
        binding.onboardingPurposeInput.setText(viewModel.draft().purpose);
        binding.onboardingContinueButton.setOnClickListener(ignored -> {
            viewModel.setPurpose(text());
            NavHostFragment.findNavController(this).navigate(R.id.identityFragment);
        });
        binding.onboardingSkipButton.setOnClickListener(ignored -> viewModel.complete(true));
    }

    private String text() {
        return binding.onboardingPurposeInput.getText() == null
                ? "" : binding.onboardingPurposeInput.getText().toString();
    }

    @Override
    public void onStop() {
        ((OnboardingActivity) requireActivity()).onboardingViewModel().setPurpose(text());
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
