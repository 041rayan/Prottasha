package com.prottasha.ui.crisis;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.R;
import com.prottasha.databinding.ActivitySupportBinding;
import com.prottasha.databinding.ItemSupportDirectoryEntryBinding;
import com.prottasha.support.data.DirectoryEntryEntity;
import com.prottasha.support.data.SupportDirectoryRepository;

public final class CrisisHelpActivity extends AppCompatActivity {

    private ActivitySupportBinding binding;
    private SupportDirectoryRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new SupportDirectoryRepository();

        binding.supportRefreshButton.setOnClickListener(v -> refreshDirectory());
        repository.loadDirectory(this, this::renderEntries);
    }

    private void refreshDirectory() {
        binding.supportRefreshButton.setEnabled(false);
        Toast.makeText(this, "Refreshing directory…", Toast.LENGTH_SHORT).show();
        repository.refreshRemote(this, null, result -> {
            binding.supportRefreshButton.setEnabled(true);
            renderEntries(result);
            if (result.sourceType == SupportDirectoryRepository.SourceType.LIVE) {
                Toast.makeText(this, "Directory updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Network unavailable. Showing verified local copy.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderEntries(SupportDirectoryRepository.DirectoryResult result) {
        if (result.sourceType == SupportDirectoryRepository.SourceType.LIVE) {
            binding.supportStatusBadge.setText("LIVE VERIFIED DIRECTORY");
            binding.supportStatusBadge.setTextColor(getColor(R.color.color_hope));
        } else if (result.sourceType == SupportDirectoryRepository.SourceType.CACHED) {
            binding.supportStatusBadge.setText("CACHED VERIFIED DIRECTORY");
            binding.supportStatusBadge.setTextColor(getColor(R.color.color_hope));
        } else if (result.sourceType == SupportDirectoryRepository.SourceType.BUNDLED) {
            binding.supportStatusBadge.setText("BUNDLED VERIFIED DIRECTORY");
            binding.supportStatusBadge.setTextColor(getColor(R.color.color_identity));
        } else {
            binding.supportStatusBadge.setText("OFFLINE MODE — VERIFIED BACKUP");
            binding.supportStatusBadge.setTextColor(getColor(R.color.color_friction));
        }

        binding.supportLastUpdated.setText("Updated " + result.lastUpdated);

        binding.supportEntries.removeAllViews();
        for (DirectoryEntryEntity entry : result.entries) {
            ItemSupportDirectoryEntryBinding itemBinding = ItemSupportDirectoryEntryBinding.inflate(
                    getLayoutInflater(), binding.supportEntries, false);

            itemBinding.supportEntryName.setText(entry.name);
            itemBinding.supportEntryBadge.setText(entry.status);
            itemBinding.supportEntryHours.setText("Hours: " + entry.hours);
            itemBinding.supportEntrySource.setText("Source: " + entry.source + " • Checked " + entry.verifiedDate);

            itemBinding.supportEntryDialButton.setText("Call " + entry.phone);
            itemBinding.supportEntryDialButton.setOnClickListener(v -> {
                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + entry.phone));
                startActivity(dial);
            });

            if (entry.website != null && SupportDirectoryRepository.isSafeHttps(entry.website)) {
                itemBinding.supportEntryWebButton.setVisibility(View.VISIBLE);
                itemBinding.supportEntryWebButton.setOnClickListener(v -> {
                    Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(entry.website));
                    startActivity(web);
                });
            } else {
                itemBinding.supportEntryWebButton.setVisibility(View.GONE);
            }

            binding.supportEntries.addView(itemBinding.getRoot());
        }
    }
}
