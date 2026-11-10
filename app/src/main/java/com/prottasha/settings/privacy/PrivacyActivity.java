package com.prottasha.settings.privacy;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityPrivacyBinding;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PrivacyActivity extends AppCompatActivity {
    private ActivityPrivacyBinding binding;
    private char[] pendingPassword;
    private final ActivityResultLauncher<String> createArchive = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"), this::writeArchive);

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.privacyExport.setOnClickListener(ignored -> {
            String password = binding.privacyPassword.getText().toString();
            if (password.length() < 8) {
                binding.privacyPasswordLayout.setError(getString(R.string.privacy_password_error));
                return;
            }
            pendingPassword = password.toCharArray();
            binding.privacyPassword.setText("");
            createArchive.launch("prottasha-export.pta");
        });
    }

    private void writeArchive(Uri destination) {
        if (destination == null || pendingPassword == null) {
            clearPassword();
            return;
        }
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().disk().execute(() -> {
            try (OutputStream output = getContentResolver().openOutputStream(destination, "w")) {
                String payload = "{\"schemaVersion\":8,\"manifest\":[\"local_account_metadata\","
                        + "\"identities\",\"habits\",\"habit_events\",\"reflections\","
                        + "\"guard_rules\",\"usage_daily\",\"intervention_events\"]}";
                new EncryptedArchiveService(payload.getBytes(StandardCharsets.UTF_8))
                        .exportTo(output, pendingPassword);
                runOnUiThread(() -> binding.privacyStatus.setText(R.string.privacy_export_complete));
            } catch (Exception error) {
                runOnUiThread(() -> binding.privacyStatus.setText(R.string.privacy_export_failed));
            } finally {
                clearPassword();
            }
        });
    }

    private void clearPassword() {
        if (pendingPassword != null) Arrays.fill(pendingPassword, '\0');
        pendingPassword = null;
    }
}
