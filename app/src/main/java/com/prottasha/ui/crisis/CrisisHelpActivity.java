package com.prottasha.ui.crisis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prottasha.data.model.TherapistItem;
import com.prottasha.data.remote.TherapistApiClient;
import com.prottasha.databinding.ActivityCrisisHelpBinding;
import com.prottasha.ui.adapter.TherapistAdapter;

public class CrisisHelpActivity extends AppCompatActivity {

    private ActivityCrisisHelpBinding binding;
    private TherapistAdapter therapistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrisisHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupImplicitIntents();
        setupTherapistDirectory();
    }

    private void setupImplicitIntents() {
        // Implicit Intent: Dial 999
        binding.btnDial999.setOnClickListener(v -> dialPhoneNumber("999"));

        // Implicit Intent: Dial Talk Hope
        binding.btnDialTalkHope.setOnClickListener(v -> dialPhoneNumber("+8809638881888"));

        // Implicit Intent: Dial Kaan Pete Roi
        binding.btnDialKaanPeteRoi.setOnClickListener(v -> dialPhoneNumber("09612119911"));

        // Implicit Intent: Share Habit Contract / Lifeline Message (ACTION_SEND)
        binding.btnShareHabitContract.setOnClickListener(v -> {
            String shareMessage = "Prottasha Identity Commitment:\nI have committed to breaking compulsive digital loops and showing up for my target identity. As my accountability observer, I ask for your support! (AH-20)";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Prottasha Habit Commitment");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share Habit Contract via"));
        });

        // Implicit Intent: Open external clinical directory URL (ACTION_VIEW)
        binding.btnOpenClinicalLink.setOnClickListener(v -> {
            String url = "https://findahelpline.com/countries/bd";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
    }

    private void dialPhoneNumber(String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        startActivity(dialIntent);
    }

    private void setupTherapistDirectory() {
        therapistAdapter = new TherapistAdapter(item -> dialPhoneNumber(item.getPhone()));
        binding.rvTherapistDirectory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTherapistDirectory.setAdapter(therapistAdapter);

        TherapistApiClient.fetchClinics(this, (clinics, isRemote) -> {
            runOnUiThread(() -> {
                therapistAdapter.setItems(clinics);
                binding.tvDirectorySource.setText(isRemote ?
                        "Loaded via remote HTTP JSON API ✓" : "Loaded via verified local JSON database ✓");
            });
        });
    }
}
