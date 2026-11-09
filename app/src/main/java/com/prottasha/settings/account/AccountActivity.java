package com.prottasha.settings.account;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.DeletionState;
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
                application.appGraph().accountDeletionCoordinator()))
                .get(AccountViewModel.class);

        boolean hasSession = viewModel.hasAccountSession();
        binding.accountDeleteButton.setEnabled(hasSession);
        binding.accountStatus.setText(hasSession
                ? R.string.account_ready
                : R.string.account_no_verified_session);
        binding.accountDeleteButton.setOnClickListener(ignored -> submit());
        binding.accountRetryButton.setOnClickListener(ignored -> viewModel.retryPendingWork());
        viewModel.deletionState().observe(this, this::render);
    }

    private void submit() {
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
