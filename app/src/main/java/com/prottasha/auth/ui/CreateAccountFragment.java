package com.prottasha.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prottasha.R;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.auth.domain.PasswordPolicy;
import com.prottasha.databinding.FragmentCreateAccountBinding;

public final class CreateAccountFragment extends Fragment {

    private FragmentCreateAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthViewModel viewModel = ((AuthActivity) requireActivity()).authViewModel();
        binding.authCreateButton.setOnClickListener(ignored -> submit(viewModel));
        binding.authLegalLink.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), LegalActivity.class)));
        viewModel.state().observe(getViewLifecycleOwner(), state ->
                binding.authCreateButton.setEnabled(state.status() != AuthState.Status.LOADING));
    }

    private void submit(AuthViewModel viewModel) {
        clearErrors();
        String name = text(binding.authDisplayNameInput).trim();
        String email = text(binding.authEmailInput).trim();
        String password = text(binding.authPasswordInput);
        String confirmation = text(binding.authConfirmPasswordInput);

        if (name.isEmpty()) {
            binding.authDisplayNameLayout.setError(getString(R.string.auth_error_name_required));
            showSummary(R.string.auth_error_name_required);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.authEmailLayout.setError(getString(R.string.auth_error_email_invalid));
            showSummary(R.string.auth_error_email_invalid);
            return;
        }
        int policyError = passwordError(PasswordPolicy.validate(password));
        if (policyError != 0) {
            binding.authPasswordLayout.setError(getString(policyError));
            showSummary(policyError);
            return;
        }
        if (!password.equals(confirmation)) {
            binding.authConfirmPasswordLayout.setError(
                    getString(R.string.auth_error_password_mismatch));
            showSummary(R.string.auth_error_password_mismatch);
            return;
        }
        if (!binding.authTermsCheckbox.isChecked()) {
            showSummary(R.string.auth_error_terms_required);
            return;
        }
        viewModel.createAccount(name, email, password);
    }

    private void clearErrors() {
        binding.authErrorSummary.setVisibility(View.GONE);
        binding.authDisplayNameLayout.setError(null);
        binding.authEmailLayout.setError(null);
        binding.authPasswordLayout.setError(null);
        binding.authConfirmPasswordLayout.setError(null);
    }

    private void showSummary(int message) {
        binding.authErrorSummary.setText(message);
        binding.authErrorSummary.setVisibility(View.VISIBLE);
        binding.authErrorSummary.requestFocus();
    }

    private static int passwordError(PasswordPolicy.Result result) {
        switch (result) {
            case TOO_SHORT:
                return R.string.auth_error_password_too_short;
            case TOO_LONG:
                return R.string.auth_error_password_too_long;
            case NEEDS_UPPERCASE:
                return R.string.auth_error_password_uppercase;
            case NEEDS_LOWERCASE:
                return R.string.auth_error_password_lowercase;
            case NEEDS_NUMBER:
                return R.string.auth_error_password_number;
            default:
                return 0;
        }
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
