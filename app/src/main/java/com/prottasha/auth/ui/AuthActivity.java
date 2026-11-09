package com.prottasha.auth.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.AuthState;
import com.prottasha.databinding.ActivityAuthBinding;

public final class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private NavController navController;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.auth_nav_host);
        if (host == null) {
            throw new IllegalStateException("Auth navigation host is missing");
        }
        navController = host.getNavController();

        ProttashaApp application = (ProttashaApp) getApplication();
        viewModel = new ViewModelProvider(this, new AuthViewModel.Factory(
                application.appGraph().authRepository(),
                application.appGraph().cloudUserProfileRepository()))
                .get(AuthViewModel.class);
        viewModel.state().observe(this, this::renderState);
    }

    public AuthViewModel authViewModel() {
        return viewModel;
    }

    private void renderState(AuthState state) {
        binding.authProgress.setVisibility(
                state.status() == AuthState.Status.LOADING ? View.VISIBLE : View.GONE);
        binding.authStatusMessage.setVisibility(
                state.messageOrNull() == null
                        && state.status() != AuthState.Status.UNCONFIGURED
                        && state.status() != AuthState.Status.VERIFIED_ONLINE
                        && state.status() != AuthState.Status.VERIFIED_OFFLINE
                        ? View.GONE : View.VISIBLE);

        if (state.messageOrNull() != null) {
            binding.authStatusMessage.setText(messageResource(state.messageOrNull()));
        } else if (state.status() == AuthState.Status.UNCONFIGURED) {
            binding.authStatusMessage.setText(R.string.auth_error_unconfigured);
        } else if (state.status() == AuthState.Status.VERIFIED_ONLINE
                || state.status() == AuthState.Status.VERIFIED_OFFLINE) {
            binding.authStatusMessage.setText(R.string.auth_verified_secure_setup_pending);
        }

        if (state.status() == AuthState.Status.UNVERIFIED && state.userOrNull() != null) {
            viewModel.ensureProfile(state.userOrNull());
            if (navController.getCurrentDestination() == null
                    || navController.getCurrentDestination().getId() != R.id.verifyEmailFragment) {
                navController.navigate(R.id.verifyEmailFragment);
            }
        } else if (state.status() == AuthState.Status.SIGNED_OUT
                && navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.verifyEmailFragment) {
            navController.popBackStack(R.id.signInFragment, false);
        }
    }

    @StringRes
    private static int messageResource(AuthState.MessageCode code) {
        switch (code) {
            case OFFLINE:
                return R.string.auth_status_offline;
            case NETWORK_REQUIRED:
                return R.string.auth_error_network_required;
            case VERIFICATION_SENT:
                return R.string.auth_status_verification_sent;
            case VERIFICATION_SEND_FAILED:
                return R.string.auth_error_verification_send;
            case INVALID_CREDENTIALS:
                return R.string.auth_error_invalid_credentials;
            case EMAIL_IN_USE:
                return R.string.auth_error_email_in_use;
            case WEAK_PASSWORD:
                return R.string.auth_error_weak_password;
            case TRY_LATER:
                return R.string.auth_error_try_later;
            case RESET_EMAIL_SENT:
                return R.string.auth_status_reset_sent;
            default:
                return R.string.auth_error_unavailable;
        }
    }
}
