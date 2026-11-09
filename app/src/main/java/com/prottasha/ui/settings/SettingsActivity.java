package com.prottasha.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.prottasha.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "prottasha_prefs";
    public static final String KEY_PAUSE_DURATION_SECONDS = "pause_duration_seconds";
    public static final String KEY_PARTNER_NAME = "partner_name";
    public static final String KEY_PARTNER_PHONE = "partner_phone";

    private ActivitySettingsBinding binding;
    private SharedPreferences prefs;

    // Contact Picker Result Launcher using ContactsContract Content Provider
    private final ActivityResultLauncher<Intent> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri contactUri = result.getData().getData();
                    queryContactDetails(contactUri);
                }
            });

    // Runtime Permission Launcher for READ_CONTACTS
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchContactPicker();
                } else {
                    // Graceful handling of permission denial requirement
                    Toast.makeText(this, "Permission denied. You can manually enter an accountability partner instead.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupPauseDurationSlider();
        displaySavedPartner();

        binding.btnPickContact.setOnClickListener(v -> checkPermissionAndPickContact());
    }

    private void setupPauseDurationSlider() {
        int currentDuration = prefs.getInt(KEY_PAUSE_DURATION_SECONDS, 10);
        binding.sliderPauseDelay.setValue(currentDuration);
        binding.tvCurrentDelayLabel.setText("Friction Countdown: " + currentDuration + " seconds");

        binding.sliderPauseDelay.addOnChangeListener((slider, value, fromUser) -> {
            int seconds = (int) value;
            binding.tvCurrentDelayLabel.setText("Friction Countdown: " + seconds + " seconds");
            prefs.edit().putInt(KEY_PAUSE_DURATION_SECONDS, seconds).apply();
        });
    }

    private void displaySavedPartner() {
        String name = prefs.getString(KEY_PARTNER_NAME, null);
        String phone = prefs.getString(KEY_PARTNER_PHONE, null);
        if (name != null && phone != null) {
            binding.tvSelectedPartner.setText("Partner: " + name + " (" + phone + ")");
        }
    }

    private void checkPermissionAndPickContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            launchContactPicker();
        } else {
            // Runtime permissions with both grant and deny handled
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void launchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
    }

    /**
     * Reads contact details using Android Content Provider (ContactsContract).
     */
    private void queryContactDetails(Uri contactUri) {
        if (contactUri == null) return;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);

                prefs.edit()
                        .putString(KEY_PARTNER_NAME, name)
                        .putString(KEY_PARTNER_PHONE, number)
                        .apply();

                binding.tvSelectedPartner.setText("Partner: " + name + " (" + number + ")");
                Toast.makeText(this, "Accountability partner set: " + name, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to read contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
