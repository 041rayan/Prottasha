package com.prottasha.settings.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.DeletionState;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.auth.ui.AuthActivity;
import com.prottasha.databinding.ActivityAccountBinding;

public final class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding binding;
    private AccountViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProttashaApp application = (ProttashaApp) getApplication();
        viewModel = new ViewModelProvider(this, new AccountViewModel.Factory(
                application.appGraph().userSessionManager(),
                application.appGraph().authRepository(),
                application.appGraph().accountDeletionCoordinator(),
                application.appGraph().localDeletionService()))
                .get(AccountViewModel.class);

        boolean hasSession = viewModel.hasAccountSession();
        SessionUser user = viewModel.currentUserOrNull();
        if (user != null) {
            binding.accountEmailDisplay.setText(user.email());
        } else {
            binding.accountEmailDisplay.setText(R.string.account_no_verified_session);
        }

        binding.accountEditProfileButton.setEnabled(hasSession);
        binding.accountEditProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        binding.accountChangePasswordButton.setEnabled(hasSession);
        binding.accountChangePasswordButton.setOnClickListener(v -> handleChangePassword());

        binding.accountChangeEmailButton.setEnabled(hasSession);
        binding.accountChangeEmailButton.setOnClickListener(v -> handleChangeEmail());

        binding.accountSignOutButton.setEnabled(hasSession);
        binding.accountSignOutButton.setOnClickListener(v -> handleSignOut());

        binding.accountSwitchButton.setEnabled(hasSession);
        binding.accountSwitchButton.setOnClickListener(v -> handleSignOut());

        binding.accountRemoveLocalButton.setEnabled(hasSession);
        binding.accountRemoveLocalButton.setOnClickListener(v -> handleRemoveLocalData());

        binding.accountDeleteButton.setEnabled(hasSession);
        binding.accountStatus.setText(hasSession
                ? R.string.account_ready
                : R.string.account_no_verified_session);
        binding.accountDeleteButton.setOnClickListener(ignored -> submitDelete());
        binding.accountRetryButton.setOnClickListener(ignored -> viewModel.retryPendingWork());
        viewModel.deletionState().observe(this, this::render);
    }

    private void handleChangePassword() {
        String currentPassword = binding.accountReauthPasswordInput.getText() == null
                ? "" : binding.accountReauthPasswordInput.getText().toString();
        String newPassword = binding.accountNewPasswordInput.getText() == null
                ? "" : binding.accountNewPasswordInput.getText().toString();

        if (currentPassword.isEmpty()) {
            binding.accountReauthPasswordLayout.setError(getString(R.string.account_password_required));
            return;
        }
        binding.accountReauthPasswordLayout.setError(null);

        if (newPassword.isEmpty()) {
            binding.accountNewPasswordLayout.setError(getString(R.string.account_password_required));
            return;
        }
        binding.accountNewPasswordLayout.setError(null);

        viewModel.changePassword(currentPassword, newPassword, result -> runOnUiThread(() -> {
            if (result.isSuccess()) {
                binding.accountReauthPasswordInput.setText("");
                binding.accountNewPasswordInput.setText("");
                Toast.makeText(this, R.string.account_password_changed, Toast.LENGTH_SHORT).show();
            } else {
                String errorMsg = result.errorOrNull() != null
                        ? result.errorOrNull().getMessage() : "Failed to update password";
                binding.accountNewPasswordLayout.setError(errorMsg);
            }
        }));
    }

    private void handleChangeEmail() {
        String currentPassword = binding.accountReauthPasswordInput.getText() == null
                ? "" : binding.accountReauthPasswordInput.getText().toString();
        String newEmail = binding.accountNewEmailInput.getText() == null
                ? "" : binding.accountNewEmailInput.getText().toString();

        if (currentPassword.isEmpty()) {
            binding.accountReauthPasswordLayout.setError(getString(R.string.account_password_required));
            return;
        }
        binding.accountReauthPasswordLayout.setError(null);

        if (newEmail.isEmpty() || !newEmail.contains("@")) {
            binding.accountNewEmailLayout.setError("Enter a valid email address");
            return;
        }
        binding.accountNewEmailLayout.setError(null);

        viewModel.changeEmail(currentPassword, newEmail, result -> runOnUiThread(() -> {
            if (result.isSuccess()) {
                binding.accountReauthPasswordInput.setText("");
                binding.accountNewEmailInput.setText("");
                Toast.makeText(this, R.string.account_email_sent, Toast.LENGTH_LONG).show();
            } else {
                String errorMsg = result.errorOrNull() != null
                        ? result.errorOrNull().getMessage() : "Failed to update email";
                binding.accountNewEmailLayout.setError(errorMsg);
            }
        }));
    }

    private void handleSignOut() {
        viewModel.signOut();
        navigateToAuth();
    }

    private void handleRemoveLocalData() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.account_remove_local_data_dialog_title)
                .setMessage(R.string.account_remove_local_data_dialog_message)
                .setPositiveButton(R.string.account_remove_local_data_action, (dialog, which) -> {
                    viewModel.signOutAndRemoveLocalData(result -> runOnUiThread(this::navigateToAuth));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void navigateToAuth() {
        Intent intent = new Intent(this, AuthActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void submitDelete() {
        String password = binding.accountPasswordInput.getText() == null
                ? ""
                : binding.accountPasswordInput.getText().toString();
        if (password.isEmpty()) {
            binding.accountPasswordLayout.setError(getString(R.string.account_password_required));
            return;
        }
        if (!binding.accountConfirmCheckbox.isChecked()) {
            binding.accountStatus.setText(R.string.account_confirmation_required);
            return;
        }
        binding.accountPasswordLayout.setError(null);
        binding.accountPasswordInput.setText("");
        viewModel.deleteAccount(password);
    }

    private void render(DeletionState state) {
        boolean hasSession = viewModel.hasAccountSession();
        binding.accountStatus.setText(
                state.stage() == DeletionState.Stage.IDLE && !hasSession
                        ? R.string.account_no_verified_session
                        : statusText(state));
        boolean running = state.stage() != DeletionState.Stage.IDLE
                && state.stage() != DeletionState.Stage.COMPLETE
                && state.stage() != DeletionState.Stage.FAILED
                && state.stage() != DeletionState.Stage.RETRY_LOCAL_RESTORE
                && state.stage() != DeletionState.Stage.RETRY_LOCAL_CLEANUP;
        binding.accountDeleteButton.setEnabled(hasSession && !running);
        binding.accountRetryButton.setVisibility(
                state.stage() == DeletionState.Stage.RETRY_LOCAL_CLEANUP
                        || state.stage() == DeletionState.Stage.RETRY_LOCAL_RESTORE
                        ? View.VISIBLE : View.GONE);
    }

    private static int statusText(DeletionState state) {
        switch (state.stage()) {
            case REAUTHENTICATING:
                return R.string.account_status_reauthenticating;
            case QUARANTINING_LOCAL:
                return R.string.account_status_quarantining;
            case DELETING_CLOUD:
                return R.string.account_status_deleting_cloud;
            case DELETING_AUTH:
                return R.string.account_status_deleting_auth;
            case RESTORING_LOCAL:
                return R.string.account_status_restoring;
            case CLEANING_LOCAL:
                return R.string.account_status_cleaning;
            case RETRY_LOCAL_RESTORE:
                return R.string.account_status_restore_retry;
            case RETRY_LOCAL_CLEANUP:
                return R.string.account_status_cleanup_retry;
            case COMPLETE:
                return R.string.account_status_complete;
            case FAILED:
                return R.string.account_status_failed;
            default:
                return R.string.account_ready;
        }
    }
}
