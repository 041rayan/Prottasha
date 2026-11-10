package com.prottasha.settings.account;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.data.CloudUserProfile;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.databinding.ActivityProfileBinding;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public final class ProfileActivity extends AppCompatActivity {

    private static final List<String> PALETTES = Arrays.asList(
            "HOPE_EMERALD", "OCEAN_BLUE", "SUNSET_AMBER", "SLATE_DARK"
    );

    private static final List<String> STYLES = Arrays.asList(
            "INITIALS", "GEOMETRIC", "BOT"
    );

    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private ActivityResultLauncher<String> photoPickerLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProttashaApp app = (ProttashaApp) getApplication();
        SessionUser user = null;
        try {
            user = app.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException noActiveSession) {
            user = app.appGraph().authRepository().currentState().userOrNull();
        }

        if (user == null) {
            Toast.makeText(this, R.string.account_no_verified_session, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this, new ProfileViewModel.Factory(
                user,
                app.appGraph().authRepository(),
                app.appGraph().cloudUserProfileRepository(),
                app.appGraph().encryptedPrivateFileStore()
        )).get(ProfileViewModel.class);

        setupDropdowns();
        setupPhotoPicker();
        observeState();

        binding.profileSaveButton.setOnClickListener(v -> submitSave());
        binding.profileChoosePhotoButton.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        binding.profilePhotoEditButton.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
        binding.profileRemovePhotoButton.setOnClickListener(v -> viewModel.removeLocalPhoto());

        viewModel.loadProfile();
    }

    private void setupDropdowns() {
        ArrayAdapter<String> paletteAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, PALETTES);
        binding.profilePaletteDropdown.setAdapter(paletteAdapter);
        binding.profilePaletteDropdown.setText(PALETTES.get(0), false);

        ArrayAdapter<String> styleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, STYLES);
        binding.profileStyleDropdown.setAdapter(styleAdapter);
        binding.profileStyleDropdown.setText(STYLES.get(0), false);
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (Uri uri) -> {
                    if (uri != null) {
                        try (InputStream stream = getContentResolver().openInputStream(uri)) {
                            if (stream != null) {
                                viewModel.setLocalPhoto(stream);
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load photo: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void observeState() {
        viewModel.profile().observe(this, profile -> {
            if (profile == null) return;
            binding.profileDisplayNameInput.setText(profile.displayName());
            binding.profilePseudonymInput.setText(profile.podPseudonym());
            binding.profilePseudonymPreview.setText(profile.podPseudonym());

            String initials = getInitials(profile.displayName());
            binding.profileAvatarInitials.setText(initials);

            if (profile.avatarPaletteOrNull() != null && PALETTES.contains(profile.avatarPaletteOrNull())) {
                binding.profilePaletteDropdown.setText(profile.avatarPaletteOrNull(), false);
            }
            if (profile.avatarStyleOrNull() != null && STYLES.contains(profile.avatarStyleOrNull())) {
                binding.profileStyleDropdown.setText(profile.avatarStyleOrNull(), false);
            }
        });

        viewModel.localPhoto().observe(this, bytes -> {
            if (bytes != null && bytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.profilePhotoImage.setImageBitmap(bitmap);
                binding.profileAvatarInitials.setVisibility(View.GONE);
                binding.profileRemovePhotoButton.setVisibility(View.VISIBLE);
            } else {
                binding.profilePhotoImage.setImageDrawable(null);
                binding.profileAvatarInitials.setVisibility(View.VISIBLE);
                binding.profileRemovePhotoButton.setVisibility(View.GONE);
            }
        });

        viewModel.isLoading().observe(this, loading -> {
            binding.profileProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.profileSaveButton.setEnabled(!loading);
        });

        viewModel.errorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.profileErrorText.setVisibility(View.VISIBLE);
                binding.profileErrorText.setText(error);
            } else {
                binding.profileErrorText.setVisibility(View.GONE);
            }
        });

        viewModel.saveSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(this, R.string.profile_saved_toast, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void submitSave() {
        String displayName = binding.profileDisplayNameInput.getText() == null
                ? "" : binding.profileDisplayNameInput.getText().toString();
        String pseudonym = binding.profilePseudonymInput.getText() == null
                ? "" : binding.profilePseudonymInput.getText().toString();
        String palette = binding.profilePaletteDropdown.getText().toString();
        String style = binding.profileStyleDropdown.getText().toString();

        viewModel.saveProfile(displayName, pseudonym, palette, style);
    }

    private static String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
