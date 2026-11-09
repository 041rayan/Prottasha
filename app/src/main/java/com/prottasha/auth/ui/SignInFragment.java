package com.prottasha.auth.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.R;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.databinding.FragmentSignInBinding;

public final class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthViewModel viewModel = ((AuthActivity) requireActivity()).authViewModel();
        binding.authSignInButton.setOnClickListener(ignored -> submit(viewModel));
        binding.authCreateLink.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).navigate(R.id.createAccountFragment));
        binding.authResetLink.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).navigate(R.id.resetPasswordFragment));
        viewModel.state().observe(getViewLifecycleOwner(), state ->
                binding.authSignInButton.setEnabled(state.status() != AuthState.Status.LOADING));
    }

    private void submit(AuthViewModel viewModel) {
        String email = text(binding.authEmailInput).trim();
        String password = text(binding.authPasswordInput);
        binding.authEmailLayout.setError(email.isEmpty() ? getString(R.string.auth_error_email_required) : null);
        binding.authPasswordLayout.setError(
                password.isEmpty() ? getString(R.string.auth_error_password_required) : null);
        if (email.isEmpty() || password.isEmpty()) {
            return;
        }
        viewModel.signIn(email, password);
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
