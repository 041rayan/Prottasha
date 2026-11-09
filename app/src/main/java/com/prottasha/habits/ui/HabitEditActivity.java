package com.prottasha.habits.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityHabitEditBinding;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;

import java.time.LocalDate;

public final class HabitEditActivity extends AppCompatActivity {

    private ActivityHabitEditBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.habitTypeGroup.setOnCheckedChangeListener((group, checkedId) ->
                binding.habitTargetLayout.setVisibility(
                        checkedId == R.id.habit_type_amount ? View.VISIBLE : View.GONE));
        binding.habitSaveButton.setOnClickListener(ignored -> save());
    }

    private void save() {
        String title = binding.habitTitleInput.getText() == null
                ? "" : binding.habitTitleInput.getText().toString().trim();
        if (title.isEmpty()) {
            binding.habitTitleLayout.setError(getString(R.string.habit_title_required));
            return;
        }
        HabitModels.Type type = binding.habitTypeGroup.getCheckedRadioButtonId()
                == R.id.habit_type_avoid ? HabitModels.Type.AVOID
                : binding.habitTypeGroup.getCheckedRadioButtonId() == R.id.habit_type_amount
                ? HabitModels.Type.AMOUNT : HabitModels.Type.BUILD;
        int target = 1;
        if (type == HabitModels.Type.AMOUNT) {
            try {
                target = Math.max(1, Integer.parseInt(
                        binding.habitTargetInput.getText().toString()));
            } catch (NumberFormatException invalid) {
                binding.habitTargetLayout.setError(getString(R.string.habit_target_required));
                return;
            }
        }
        ProttashaApp application = (ProttashaApp) getApplication();
        application.appGraph().habitRepository().save(new HabitModels.HabitSnapshot(
                0L, title, type, target,
                HabitSchedule.daily(LocalDate.now().toEpochDay())), result ->
                runOnUiThread(() -> {
                    if (result.isSuccess()) {
                        finish();
                    } else {
                        binding.habitTitleLayout.setError(
                                getString(R.string.habit_save_failed));
                    }
                }));
    }
}
