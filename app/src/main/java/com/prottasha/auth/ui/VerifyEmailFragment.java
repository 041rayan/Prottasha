package com.prottasha.auth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prottasha.R;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.databinding.FragmentVerifyEmailBinding;
import com.prottasha.settings.account.AccountActivity;

public final class VerifyEmailFragment extends Fragment {

    private FragmentVerifyEmailBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentVerifyEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AuthViewModel viewModel = ((AuthActivity) requireActivity()).authViewModel();
        binding.authResendVerificationButton.setOnClickListener(ignored ->
                viewModel.sendVerification());
        binding.authRefreshVerificationButton.setOnClickListener(ignored ->
                viewModel.refreshVerification());
        binding.authSignOutButton.setOnClickListener(ignored -> viewModel.signOut());
        binding.authManageAccountButton.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), AccountActivity.class)));
        viewModel.state().observe(getViewLifecycleOwner(), state -> render(state));
    }

    private void render(AuthState state) {
        if (state.userOrNull() != null) {
            binding.authVerificationEmail.setText(getString(
                    R.string.auth_verify_destination, state.userOrNull().email()));
        }
        boolean loading = state.status() == AuthState.Status.LOADING;
        binding.authResendVerificationButton.setEnabled(!loading);
        binding.authRefreshVerificationButton.setEnabled(!loading);
        if (state.status() == AuthState.Status.VERIFIED_ONLINE
                || state.status() == AuthState.Status.VERIFIED_OFFLINE) {
            binding.authVerificationStatus.setText(R.string.auth_verified_secure_setup_pending);
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
