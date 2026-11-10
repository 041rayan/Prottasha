package com.prottasha.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.R;
import com.prottasha.databinding.FragmentOnboardingInterventionPreviewBinding;

public final class InterventionPreviewFragment extends Fragment {

    private FragmentOnboardingInterventionPreviewBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingInterventionPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();

        binding.onboardingPreviewStepBackButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Mindful exit! Returning to your goal.",
                    Toast.LENGTH_SHORT).show();
            viewModel.complete(false);
        });

        binding.onboardingPreviewContinueAction.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Intentional continuation recorded.",
                    Toast.LENGTH_SHORT).show();
            viewModel.complete(false);
        });

        binding.onboardingPreviewFinishButton.setOnClickListener(v ->
                viewModel.complete(false));

        binding.onboardingPreviewBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.onboardingPreviewSkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
