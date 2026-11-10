package com.prottasha.circle.ui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.data.AccountabilityPartner;
import com.prottasha.circle.data.AccountabilityPartnerStore;
import com.prottasha.databinding.ActivityAccountabilityBinding;
import com.prottasha.habits.data.HabitEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class AccountabilityActivity extends AppCompatActivity {

    private ActivityAccountabilityBinding binding;
    private AccountabilityPartnerStore partnerStore;
    private SessionUser user;
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private List<String> activeHabitNames = Collections.emptyList();

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityAccountabilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProttashaApp app = (ProttashaApp) getApplication();
        try {
            user = app.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException noUser) {
            finish();
            return;
        }

        partnerStore = new AccountabilityPartnerStore(this, user.uid());

        registerContactPicker();
        setupActions();
        loadActiveHabitsAndPartner(app);
    }

    private void registerContactPicker() {
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        if (contactUri != null) {
                            extractContact(contactUri);
                        }
                    }
                }
        );
    }

    private void extractContact(Uri contactUri) {
        try (Cursor cursor = getContentResolver().query(contactUri,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                if (nameIdx >= 0) {
                    binding.accountabilityNameInput.setText(cursor.getString(nameIdx));
                }
                if (phoneIdx >= 0) {
                    String rawPhone = cursor.getString(phoneIdx);
                    if (rawPhone != null) {
                        binding.accountabilityPhoneInput.setText(rawPhone.replaceAll("\\s+", ""));
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Could not read contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupActions() {
        binding.accountabilityPick.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            contactPickerLauncher.launch(pickIntent);
        });

        binding.accountabilitySaveButton.setOnClickListener(v -> savePartner());

        binding.accountabilityRevoke.setOnClickListener(v -> {
            partnerStore.revoke();
            renderPartner(null);
            updatePreview(null);
            Toast.makeText(this, "Partner disconnected", Toast.LENGTH_SHORT).show();
        });

        binding.accountabilityShare.setOnClickListener(v -> {
            String preview = binding.accountabilityPreview.getText() != null
                    ? binding.accountabilityPreview.getText().toString().trim()
                    : "";
            if (preview.isEmpty()) {
                Toast.makeText(this, "Note is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent share = new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, preview);
            startActivity(Intent.createChooser(share, "Share accountability note"));
        });
    }

    private void savePartner() {
        String name = binding.accountabilityNameInput.getText() != null
                ? binding.accountabilityNameInput.getText().toString().trim()
                : "";
        String phone = binding.accountabilityPhoneInput.getText() != null
                ? binding.accountabilityPhoneInput.getText().toString().trim()
                : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter partner name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!AccountabilityPartner.isValidPhoneNumber(phone)) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountabilityPartner partner = new AccountabilityPartner(name, phone, System.currentTimeMillis(), true);
        partnerStore.save(partner);
        renderPartner(partner);
        updatePreview(partner);
        binding.accountabilityNameInput.setText("");
        binding.accountabilityPhoneInput.setText("");
        Toast.makeText(this, "Accountability partner saved", Toast.LENGTH_SHORT).show();
    }

    private void loadActiveHabitsAndPartner(ProttashaApp app) {
        app.appGraph().executors().disk().execute(() -> {
            List<String> names = new ArrayList<>();
            try {
                List<HabitEntity> habits = app.appGraph().userSessionDatabaseProvider()
                        .requireOpen().habitDao().listActive();
                for (HabitEntity h : habits) {
                    names.add(h.title);
                }
            } catch (Exception ignored) {
            }

            AccountabilityPartner partner = partnerStore.get();
            runOnUiThread(() -> {
                this.activeHabitNames = names;
                renderPartner(partner);
                updatePreview(partner);
            });
        });
    }

    private void renderPartner(@Nullable AccountabilityPartner partner) {
        if (partner != null && partner.isActive) {
            binding.accountabilityCurrentPartnerCard.setVisibility(View.VISIBLE);
            binding.accountabilityPartnerDetails.setText(partner.name + " (" + partner.phoneNumber + ")");
            binding.accountabilityPartnerStatus.setText("Connected on " + dateFormat.format(new Date(partner.connectedAt)));
        } else {
            binding.accountabilityCurrentPartnerCard.setVisibility(View.GONE);
        }
    }

    private void updatePreview(@Nullable AccountabilityPartner partner) {
        String partnerName = partner != null && !partner.name.isEmpty() ? partner.name : "Partner";
        String note = AccountabilityPartner.generateShareNote(partnerName, activeHabitNames);
        binding.accountabilityPreview.setText(note);
    }
}
