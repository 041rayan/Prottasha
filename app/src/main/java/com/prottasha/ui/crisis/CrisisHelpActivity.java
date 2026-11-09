package com.prottasha.ui.crisis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.R;
import com.prottasha.databinding.ActivitySupportBinding;
import com.prottasha.support.data.DirectoryEntryEntity;
import com.prottasha.support.data.SupportDirectoryRepository;

public final class CrisisHelpActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        ActivitySupportBinding binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        for (DirectoryEntryEntity entry : new SupportDirectoryRepository().entries()) {
            TextView row = new TextView(this);
            row.setText(getString(R.string.support_entry, entry.name, entry.phone,
                    entry.hours, entry.status, entry.source, entry.verifiedDate));
            row.setTextSize(16);
            row.setPadding(16, 20, 16, 20);
            row.setOnClickListener(ignored -> startActivity(new Intent(
                    Intent.ACTION_DIAL, Uri.parse("tel:" + entry.phone))));
            binding.supportEntries.addView(row);
        }
    }
}
