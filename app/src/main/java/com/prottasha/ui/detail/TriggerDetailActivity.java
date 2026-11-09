package com.prottasha.ui.detail;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.data.local.entity.TriggerEntity;
import com.prottasha.data.repository.TriggerRepository;
import com.prottasha.databinding.ActivityTriggerDetailBinding;

public class TriggerDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TRIGGER_ID = "EXTRA_TRIGGER_ID";

    private ActivityTriggerDetailBinding binding;
    private TriggerRepository triggerRepository;
    private long triggerId = -1L;
    private TriggerEntity currentTrigger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTriggerDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        triggerRepository = new TriggerRepository(getApplication());
        triggerId = getIntent().getLongExtra(EXTRA_TRIGGER_ID, -1L);

        binding.tvTriggerIdLabel.setText("Trigger Plan ID: #" + triggerId);

        if (triggerId != -1L) {
            triggerRepository.getTriggerById(triggerId).observe(this, trigger -> {
                if (trigger != null) {
                    currentTrigger = trigger;
                    binding.etSituation.setText(trigger.getSituationCue());
                    binding.etMotive.setText(trigger.getUnderlyingMotive());
                    binding.etIfThenRule.setText(trigger.getIfThenPlan());
                }
            });
        }

        binding.btnSaveTrigger.setOnClickListener(v -> {
            if (currentTrigger != null) {
                currentTrigger.setSituationCue(binding.etSituation.getText().toString().trim());
                currentTrigger.setUnderlyingMotive(binding.etMotive.getText().toString().trim());
                currentTrigger.setIfThenPlan(binding.etIfThenRule.getText().toString().trim());

                triggerRepository.update(currentTrigger);
                Toast.makeText(this, "Plan updated!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        binding.btnDeleteTrigger.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Plan?")
                    .setMessage("Are you sure you want to remove this If-Then plan?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        triggerRepository.deleteById(triggerId);
                        Toast.makeText(this, "Plan removed", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
