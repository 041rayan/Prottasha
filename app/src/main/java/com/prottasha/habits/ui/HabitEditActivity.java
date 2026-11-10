package com.prottasha.habits.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityHabitEditBinding;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;

import java.time.LocalDate;
import java.util.List;

public final class HabitEditActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";

    private ActivityHabitEditBinding binding;
    private HabitEditViewModel viewModel;
    private long habitId;
    private boolean isPopulating = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, 0L);
        ProttashaApp application = (ProttashaApp) getApplication();
        viewModel = new ViewModelProvider(this, new HabitEditViewModel.Factory(
                application.appGraph().habitRepository(), habitId)).get(HabitEditViewModel.class);

        if (habitId > 0) {
            binding.habitEditScreenTitle.setText(R.string.habit_edit_existing_title);
        }

        setupTypeListeners();
        setupFrequencyListeners();
        setupDirtyTracking();
        setupSubsteps();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmExitIfDirty();
            }
        });

        binding.habitSaveButton.setOnClickListener(ignored -> save());

        viewModel.habit().observe(this, this::populateHabit);
        viewModel.substeps().observe(this, this::renderSubsteps);
    }

    private void setupTypeListeners() {
        binding.habitTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isPopulating) viewModel.markDirty();
            boolean isAmount = checkedId == R.id.habit_type_amount;
            boolean isBuild = checkedId == R.id.habit_type_build;

            binding.habitTargetLayout.setVisibility(isAmount ? View.VISIBLE : View.GONE);
            binding.habitAmountSection.setVisibility(isAmount ? View.VISIBLE : View.GONE);
            binding.habitSubstepsSection.setVisibility(isBuild ? View.VISIBLE : View.GONE);
        });
    }

    private void setupFrequencyListeners() {
        binding.habitFrequencyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (!isPopulating) viewModel.markDirty();
            boolean needsInterval = checkedId == R.id.habit_freq_every_n_days
                    || checkedId == R.id.habit_freq_every_n_weeks
                    || checkedId == R.id.habit_freq_from_last_done;
            binding.habitIntervalLayout.setVisibility(needsInterval ? View.VISIBLE : View.GONE);
        });
    }

    private void setupDirtyTracking() {
        TextWatcher dirtyWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isPopulating) viewModel.markDirty();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        binding.habitTitleInput.addTextChangedListener(dirtyWatcher);
        binding.habitTargetInput.addTextChangedListener(dirtyWatcher);
        binding.habitUnitInput.addTextChangedListener(dirtyWatcher);
        binding.habitIncrementInput.addTextChangedListener(dirtyWatcher);
        binding.habitMaximumInput.addTextChangedListener(dirtyWatcher);
        binding.habitIntervalInput.addTextChangedListener(dirtyWatcher);
        binding.habitCueInput.addTextChangedListener(dirtyWatcher);
        binding.habitTwoMinuteInput.addTextChangedListener(dirtyWatcher);
        binding.habitRewardInput.addTextChangedListener(dirtyWatcher);
        binding.habitIfThenInput.addTextChangedListener(dirtyWatcher);
        binding.habitReboundInput.addTextChangedListener(dirtyWatcher);
        binding.habitFocusMinutesInput.addTextChangedListener(dirtyWatcher);
        binding.habitAccountabilityInput.addTextChangedListener(dirtyWatcher);
    }

    private void setupSubsteps() {
        binding.habitAddSubstepButton.setOnClickListener(ignored -> {
            String text = binding.habitSubstepNewInput.getText() == null ? ""
                    : binding.habitSubstepNewInput.getText().toString();
            if (!text.trim().isEmpty()) {
                viewModel.addSubstep(text);
                binding.habitSubstepNewInput.setText("");
            }
        });
    }

    private void renderSubsteps(List<String> list) {
        binding.habitSubstepsContainer.removeAllViews();
        if (list == null) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            String text = list.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);

            TextView label = new TextView(this);
            label.setText((i + 1) + ". " + text);
            label.setTextColor(getResources().getColor(R.color.color_text_primary, null));
            label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            MaterialButton removeBtn = new MaterialButton(this, null, androidx.appcompat.R.attr.borderlessButtonStyle);
            removeBtn.setText("✕");
            removeBtn.setOnClickListener(v -> viewModel.removeSubstep(index));

            row.addView(label);
            row.addView(removeBtn);
            binding.habitSubstepsContainer.addView(row);
        }
    }

    private void populateHabit(HabitModels.HabitSnapshot h) {
        if (h == null) return;
        isPopulating = true;
        binding.habitTitleInput.setText(h.title);

        if (h.type == HabitModels.Type.AMOUNT) {
            binding.habitTypeGroup.check(R.id.habit_type_amount);
            binding.habitTargetInput.setText(String.valueOf(h.target));
            binding.habitUnitInput.setText(h.unit);
            binding.habitIncrementInput.setText(String.valueOf(h.incrementVal));
            binding.habitMaximumInput.setText(h.maximumVal > 0 ? String.valueOf(h.maximumVal) : "");
        } else if (h.type == HabitModels.Type.AVOID) {
            binding.habitTypeGroup.check(R.id.habit_type_avoid);
        } else {
            binding.habitTypeGroup.check(R.id.habit_type_build);
        }

        switch (h.schedule.frequency) {
            case WEEKDAYS:
                binding.habitFrequencyGroup.check(R.id.habit_freq_weekdays);
                break;
            case EVERY_N_DAYS:
                binding.habitFrequencyGroup.check(R.id.habit_freq_every_n_days);
                binding.habitIntervalInput.setText(String.valueOf(h.schedule.interval));
                break;
            case EVERY_N_WEEKS:
                binding.habitFrequencyGroup.check(R.id.habit_freq_every_n_weeks);
                binding.habitIntervalInput.setText(String.valueOf(h.schedule.interval));
                break;
            case FROM_LAST_DONE:
                binding.habitFrequencyGroup.check(R.id.habit_freq_from_last_done);
                binding.habitIntervalInput.setText(String.valueOf(h.schedule.interval));
                break;
            default:
                binding.habitFrequencyGroup.check(R.id.habit_freq_daily);
                break;
        }

        binding.habitCueInput.setText(h.cue);
        binding.habitTwoMinuteInput.setText(h.twoMinuteAction);
        binding.habitRewardInput.setText(h.reward);
        binding.habitIfThenInput.setText(h.ifThenPlan);
        binding.habitReboundInput.setText(h.reboundPlan);
        binding.habitFocusMinutesInput.setText(h.focusMinutes > 0 ? String.valueOf(h.focusMinutes) : "");
        binding.habitAccountabilityInput.setText(h.accountability);
        isPopulating = false;
    }

    private void confirmExitIfDirty() {
        if (Boolean.TRUE.equals(viewModel.isDirty().getValue())) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.habit_unsaved_title)
                    .setMessage(R.string.habit_unsaved_message)
                    .setPositiveButton(R.string.habit_discard, (dialog, which) -> finish())
                    .setNegativeButton(R.string.habit_keep_editing, null)
                    .show();
        } else {
            finish();
        }
    }

    private void save() {
        String title = binding.habitTitleInput.getText() == null
                ? "" : binding.habitTitleInput.getText().toString().trim();
        if (title.isEmpty()) {
            binding.habitTitleLayout.setError(getString(R.string.habit_title_required));
            return;
        }
        binding.habitTitleLayout.setError(null);

        HabitModels.Type type = HabitModels.Type.BUILD;
        int checkedTypeId = binding.habitTypeGroup.getCheckedRadioButtonId();
        if (checkedTypeId == R.id.habit_type_avoid) {
            type = HabitModels.Type.AVOID;
        } else if (checkedTypeId == R.id.habit_type_amount) {
            type = HabitModels.Type.AMOUNT;
        }

        int target = 1;
        int increment = 1;
        int max = 0;
        String unit = "";
        if (type == HabitModels.Type.AMOUNT) {
            try {
                target = Math.max(1, Integer.parseInt(
                        binding.habitTargetInput.getText().toString().trim()));
            } catch (NumberFormatException invalid) {
                binding.habitTargetLayout.setError(getString(R.string.habit_target_required));
                return;
            }
            binding.habitTargetLayout.setError(null);

            unit = binding.habitUnitInput.getText() == null ? "" : binding.habitUnitInput.getText().toString().trim();
            try {
                increment = Math.max(1, Integer.parseInt(binding.habitIncrementInput.getText().toString().trim()));
            } catch (Exception ignored) { increment = 1; }
            try {
                max = Integer.parseInt(binding.habitMaximumInput.getText().toString().trim());
            } catch (Exception ignored) { max = 0; }
        }

        long startDay = LocalDate.now().toEpochDay();
        int interval = 1;
        try {
            interval = Math.max(1, Integer.parseInt(binding.habitIntervalInput.getText().toString().trim()));
        } catch (Exception ignored) { interval = 1; }

        HabitSchedule schedule = HabitSchedule.daily(startDay);
        int freqId = binding.habitFrequencyGroup.getCheckedRadioButtonId();
        if (freqId == R.id.habit_freq_weekdays) {
            schedule = HabitSchedule.weekdays(startDay);
        } else if (freqId == R.id.habit_freq_every_n_days) {
            schedule = HabitSchedule.everyNDays(startDay, interval);
        } else if (freqId == R.id.habit_freq_every_n_weeks) {
            schedule = HabitSchedule.everyNWeeks(startDay, interval);
        } else if (freqId == R.id.habit_freq_from_last_done) {
            schedule = HabitSchedule.fromLastDone(startDay, interval);
        }

        String cue = textOf(binding.habitCueInput);
        String twoMinute = textOf(binding.habitTwoMinuteInput);
        String reward = textOf(binding.habitRewardInput);
        String ifThen = textOf(binding.habitIfThenInput);
        String rebound = textOf(binding.habitReboundInput);
        int focusMinutes = 0;
        try {
            focusMinutes = Integer.parseInt(textOf(binding.habitFocusMinutesInput));
        } catch (Exception ignored) {}
        String accountability = textOf(binding.habitAccountabilityInput);

        viewModel.save(
                title, type, target, schedule,
                cue, twoMinute, reward, ifThen, rebound,
                unit, increment, max, focusMinutes, accountability,
                result -> runOnUiThread(() -> {
                    if (result.isSuccess()) {
                        finish();
                    } else {
                        Toast.makeText(this, R.string.habit_save_failed, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private String textOf(TextView tv) {
        return tv.getText() == null ? "" : tv.getText().toString().trim();
    }
}
