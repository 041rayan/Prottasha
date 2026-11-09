package com.prottasha.auth.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.databinding.ActivityLegalBinding;

public final class LegalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLegalBinding binding = ActivityLegalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.authLegalClose.setOnClickListener(ignored -> finish());
    }
}
