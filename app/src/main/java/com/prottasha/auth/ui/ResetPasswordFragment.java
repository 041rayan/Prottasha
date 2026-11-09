package com.prottasha.auth.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.R;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.databinding.FragmentResetPasswordBinding;

public final class ResetPasswordFragment extends Fragment {

    private FragmentResetPasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthViewModel viewModel = ((AuthActivity) requireActivity()).authViewModel();
        binding.authResetButton.setOnClickListener(ignored -> {
            String email = text(binding.authEmailInput).trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.authEmailLayout.setError(getString(R.string.auth_error_email_invalid));
                return;
            }
            binding.authEmailLayout.setError(null);
            viewModel.sendPasswordReset(email);
        });
        binding.authResetBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());
        viewModel.state().observe(getViewLifecycleOwner(), state ->
                binding.authResetButton.setEnabled(state.status() != AuthState.Status.LOADING));
    }

    private static String text(android.widget.EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
