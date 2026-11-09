package com.prottasha.ui.detail;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.data.local.entity.HabitEntity;
import com.prottasha.data.repository.HabitRepository;
import com.prottasha.databinding.ActivityHabitDetailBinding;

public class HabitDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "EXTRA_HABIT_ID";

    private ActivityHabitDetailBinding binding;
    private HabitRepository habitRepository;
    private long habitId = -1L;
    private HabitEntity currentHabit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habitRepository = new HabitRepository(getApplication());
        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, -1L);

        binding.tvHabitIdLabel.setText("Habit ID: #" + habitId);

        if (habitId != -1L) {
            loadHabitData();
        }

        setupEventHandlers();
    }

    private void loadHabitData() {
        habitRepository.getHabitById(habitId).observe(this, habit -> {
            if (habit != null) {
                currentHabit = habit;
                binding.etDetailTitle.setText(habit.getTitle());
                binding.etDetailCue.setText(habit.getCue());
                binding.etDetailCraving.setText(habit.getCraving());
                binding.etDetailResponse.setText(habit.getResponse());
                binding.etDetailReward.setText(habit.getReward());
                binding.switchDetailGoodHabit.setChecked(habit.isGoodHabit());
            }
        });
    }

    private void setupEventHandlers() {
        // Text change listener requirement
        binding.etDetailTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() > 50) {
                    binding.etDetailTitle.setError("Keep habit title concise (AH-14)");
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Switch change listener requirement
        binding.switchDetailGoodHabit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "Building positive habit" : "Breaking bad habit (inverted laws)";
            Toast.makeText(this, state, Toast.LENGTH_SHORT).show();
        });

        // Click save
        binding.btnSaveHabit.setOnClickListener(v -> {
            if (currentHabit != null) {
                currentHabit.setTitle(binding.etDetailTitle.getText().toString().trim());
                currentHabit.setCue(binding.etDetailCue.getText().toString().trim());
                currentHabit.setCraving(binding.etDetailCraving.getText().toString().trim());
                currentHabit.setResponse(binding.etDetailResponse.getText().toString().trim());
                currentHabit.setReward(binding.etDetailReward.getText().toString().trim());
                currentHabit.setGoodHabit(binding.switchDetailGoodHabit.isChecked());

                habitRepository.update(currentHabit);
                Toast.makeText(this, "Habit updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Long click / click delete
        binding.btnDeleteHabit.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Habit?")
                    .setMessage("Are you sure you want to delete this habit?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        habitRepository.deleteById(habitId);
                        Toast.makeText(this, "Habit deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
