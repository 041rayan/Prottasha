package com.prottasha.insights.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.core.security.StoredPrivateImage;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.databinding.ActivityReflectionBinding;
import com.prottasha.insights.data.InsightsRepository;
import com.prottasha.insights.data.JournalEntryEntity;
import com.prottasha.insights.data.JournalImageEntity;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

public final class ReflectionActivity extends AppCompatActivity {
    public static final String EXTRA_REFLECTION_ID = "extra_reflection_id";

    private ActivityReflectionBinding binding;
    private long reflectionId = 0L;
    private String currentStorageId = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityReflectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reflectionId = getIntent().getLongExtra(EXTRA_REFLECTION_ID, 0L);

        binding.reflectionAttachImage.setOnClickListener(ignored ->
                imagePickerLauncher.launch("image/*"));

        binding.reflectionRemoveImage.setOnClickListener(ignored -> {
            currentStorageId = null;
            binding.reflectionImageContainer.setVisibility(View.GONE);
            binding.reflectionAttachImage.setVisibility(View.VISIBLE);
        });

        if (reflectionId > 0) {
            binding.reflectionDelete.setVisibility(View.VISIBLE);
            loadExistingReflection();
        }

        binding.reflectionSave.setOnClickListener(ignored -> saveReflection());

        binding.reflectionDelete.setOnClickListener(ignored -> deleteReflection());
    }

    private void loadExistingReflection() {
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().disk().execute(() -> {
            JournalEntryEntity entry = app.appGraph().userSessionDatabaseProvider()
                    .requireOpen().insightsDao().findById(reflectionId);
            if (entry != null) {
                List<JournalImageEntity> images = app.appGraph().insightsRepository()
                        .getImagesForReflection(reflectionId);
                String storageId = (!images.isEmpty()) ? images.get(0).storageId : null;
                Bitmap previewBitmap = null;
                if (storageId != null) {
                    try {
                        SessionUser user = app.appGraph().userSessionManager().requireUser();
                        if (user != null) {
                            byte[] bytes = app.appGraph().encryptedPrivateFileStore().readImage(
                                    UserDatabaseIdentity.fromUid(user.uid()),
                                    EncryptedPrivateFileStore.Category.JOURNAL,
                                    storageId);
                            previewBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        }
                    } catch (Exception ignored) { }
                }
                final Bitmap finalBitmap = previewBitmap;
                final String finalStorageId = storageId;
                runOnUiThread(() -> {
                    binding.reflectionFact.setText(entry.fact);
                    binding.reflectionInterpretation.setText(entry.interpretation);
                    binding.reflectionLesson.setText(entry.lesson);
                    if (finalBitmap != null) {
                        currentStorageId = finalStorageId;
                        binding.reflectionImagePreview.setImageBitmap(finalBitmap);
                        binding.reflectionImageContainer.setVisibility(View.VISIBLE);
                        binding.reflectionAttachImage.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null) return;
        ProttashaApp app = (ProttashaApp) getApplication();
        SessionUser user;
        try {
            user = app.appGraph().userSessionManager().requireUser();
        } catch (Exception e) {
            return;
        }

        app.appGraph().executors().disk().execute(() -> {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                if (is == null) return;
                StoredPrivateImage stored = app.appGraph().encryptedPrivateFileStore().storeImage(
                        UserDatabaseIdentity.fromUid(user.uid()),
                        EncryptedPrivateFileStore.Category.JOURNAL,
                        is);
                byte[] bytes = app.appGraph().encryptedPrivateFileStore().readImage(
                        UserDatabaseIdentity.fromUid(user.uid()),
                        EncryptedPrivateFileStore.Category.JOURNAL,
                        stored.storageId());
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                runOnUiThread(() -> {
                    currentStorageId = stored.storageId();
                    binding.reflectionImagePreview.setImageBitmap(bitmap);
                    binding.reflectionImageContainer.setVisibility(View.VISIBLE);
                    binding.reflectionAttachImage.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to import image", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveReflection() {
        String fact = binding.reflectionFact.getText() != null
                ? binding.reflectionFact.getText().toString().trim() : "";
        String interpretation = binding.reflectionInterpretation.getText() != null
                ? binding.reflectionInterpretation.getText().toString().trim() : "";
        String lesson = binding.reflectionLesson.getText() != null
                ? binding.reflectionLesson.getText().toString().trim() : "";

        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().disk().execute(() -> {
            long epochDay = LocalDate.now().toEpochDay();
            if (reflectionId > 0) {
                app.appGraph().insightsRepository().updateReflection(
                        reflectionId, epochDay, fact, interpretation, lesson, currentStorageId);
            } else {
                app.appGraph().insightsRepository().saveReflectionWithImage(
                        epochDay, fact, interpretation, lesson, currentStorageId);
            }
            runOnUiThread(this::finish);
        });
    }

    private void deleteReflection() {
        if (reflectionId <= 0) return;
        ProttashaApp app = (ProttashaApp) getApplication();
        app.appGraph().executors().disk().execute(() -> {
            app.appGraph().insightsRepository().deleteReflection(reflectionId);
            runOnUiThread(this::finish);
        });
    }
}
