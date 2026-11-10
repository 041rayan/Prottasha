package com.prottasha.settings.privacy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.databinding.ActivityPrivacyBinding;
import com.prottasha.settings.security.AppLockPolicy;
import com.prottasha.settings.security.AppLockSetupActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public final class PrivacyActivity extends AppCompatActivity {

    private ActivityPrivacyBinding binding;
    private char[] pendingExportPassword;
    private char[] pendingImportPassword;
    private SessionUser user;
    private String uidHash;

    private final LogicalArchiveRepository archiveRepository = new LogicalArchiveRepository();

    private final ActivityResultLauncher<String> createArchive = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"), this::writeArchive);

    private final ActivityResultLauncher<String[]> openArchive = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), this::readArchive);

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProttashaApp app = (ProttashaApp) getApplication();
        try {
            user = app.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException noUser) {
            finish();
            return;
        }

        uidHash = UserDatabaseIdentity.fromUid(user.uid()).uidHash();

        setupLockSection();
        setupExportSection();
        setupImportSection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLockStatus();
    }

    private void setupLockSection() {
        binding.privacyLockSetupButton.setOnClickListener(v ->
                startActivity(new Intent(this, AppLockSetupActivity.class)));
        updateLockStatus();
    }

    private void updateLockStatus() {
        AppLockPolicy policy = AppLockPolicy.load(this, uidHash);
        if (policy.isLockEnabled() && policy.hasPin()) {
            binding.privacyLockStatusText.setText("App Lock is ACTIVE (" + policy.getTimeoutSeconds() + "s timeout)");
            binding.privacyLockStatusText.setTextColor(getColor(R.color.color_hope));
        } else {
            binding.privacyLockStatusText.setText("App Lock is DISABLED");
            binding.privacyLockStatusText.setTextColor(getColor(R.color.color_text_muted));
        }
    }

    private void setupExportSection() {
        binding.privacyExport.setOnClickListener(v -> {
            String password = binding.privacyPassword.getText() != null
                    ? binding.privacyPassword.getText().toString()
                    : "";
            if (password.length() < 8) {
                binding.privacyPasswordLayout.setError(getString(R.string.privacy_password_error));
                return;
            }
            binding.privacyPasswordLayout.setError(null);
            pendingExportPassword = password.toCharArray();
            binding.privacyPassword.setText("");
            createArchive.launch("prottasha-backup.pta");
        });
    }

    private void writeArchive(Uri destination) {
        if (destination == null || pendingExportPassword == null) {
            clearPasswords();
            return;
        }

        binding.privacyStatus.setText("Exporting encrypted archive…");
        binding.privacySummaryDetails.setVisibility(View.GONE);

        ProttashaApp app = (ProttashaApp) getApplication();
        char[] password = pendingExportPassword;
        pendingExportPassword = null;

        app.appGraph().executors().disk().execute(() -> {
            try (OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
                UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
                LogicalArchiveRepository.ExportSummary summary = archiveRepository.exportArchive(
                        db,
                        app.appGraph().encryptedPrivateFileStore(),
                        uidHash,
                        output,
                        password
                );

                runOnUiThread(() -> {
                    binding.privacyStatus.setText(R.string.privacy_export_complete);
                    binding.privacySummaryDetails.setText("Exported: " + summary.habitsCount + " habits, "
                            + summary.reflectionsCount + " reflections, "
                            + summary.rulesCount + " rules, "
                            + summary.triggersCount + " trigger plans.");
                    binding.privacySummaryDetails.setVisibility(View.VISIBLE);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    binding.privacyStatus.setText(R.string.privacy_export_failed);
                    binding.privacySummaryDetails.setVisibility(View.GONE);
                });
            } finally {
                Arrays.fill(password, '\0');
            }
        });
    }

    private void setupImportSection() {
        binding.privacyImport.setOnClickListener(v -> {
            String password = binding.privacyImportPassword.getText() != null
                    ? binding.privacyImportPassword.getText().toString()
                    : "";
            if (password.length() < 8) {
                binding.privacyImportPasswordLayout.setError(getString(R.string.privacy_password_error));
                return;
            }
            binding.privacyImportPasswordLayout.setError(null);
            pendingImportPassword = password.toCharArray();
            binding.privacyImportPassword.setText("");
            openArchive.launch(new String[]{"*/*"});
        });
    }

    private void readArchive(Uri source) {
        if (source == null || pendingImportPassword == null) {
            clearPasswords();
            return;
        }

        binding.privacyStatus.setText("Validating and restoring archive…");
        binding.privacySummaryDetails.setVisibility(View.GONE);

        ProttashaApp app = (ProttashaApp) getApplication();
        char[] password = pendingImportPassword;
        pendingImportPassword = null;

        app.appGraph().executors().disk().execute(() -> {
            try (InputStream input = getContentResolver().openInputStream(source)) {
                UserDatabase db = app.appGraph().userSessionDatabaseProvider().requireOpen();
                LogicalArchiveRepository.ImportSummary summary = archiveRepository.importArchive(
                        db,
                        app.appGraph().encryptedPrivateFileStore(),
                        uidHash,
                        input,
                        password
                );

                runOnUiThread(() -> {
                    binding.privacyStatus.setText(R.string.privacy_import_complete);
                    binding.privacySummaryDetails.setText("Restored: " + summary.habitsRestored + " habits, "
                            + summary.reflectionsRestored + " reflections, "
                            + summary.rulesRestored + " rules, "
                            + summary.triggersRestored + " trigger plans.");
                    binding.privacySummaryDetails.setVisibility(View.VISIBLE);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    binding.privacyStatus.setText(R.string.privacy_import_failed);
                    binding.privacySummaryDetails.setText(error.getMessage() != null ? error.getMessage() : "Corrupted or invalid password");
                    binding.privacySummaryDetails.setVisibility(View.VISIBLE);
                });
            } finally {
                Arrays.fill(password, '\0');
            }
        });
    }

    private void clearPasswords() {
        if (pendingExportPassword != null) Arrays.fill(pendingExportPassword, '\0');
        if (pendingImportPassword != null) Arrays.fill(pendingImportPassword, '\0');
        pendingExportPassword = null;
        pendingImportPassword = null;
    }

    @Override
    protected void onDestroy() {
        clearPasswords();
        super.onDestroy();
    }
}
