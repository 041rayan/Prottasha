package com.prottasha.circle.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.databinding.ActivityAccountabilityBinding;

public final class AccountabilityActivity extends AppCompatActivity {
    private ActivityAccountabilityBinding binding;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityAccountabilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.accountabilityPick.setOnClickListener(ignored -> startActivity(new Intent(
                Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)));
        binding.accountabilityShare.setOnClickListener(ignored -> {
            String preview = binding.accountabilityPreview.getText().toString();
            Intent share = new Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, preview);
            startActivity(Intent.createChooser(share, "Share accountability note"));
        });
    }
}
