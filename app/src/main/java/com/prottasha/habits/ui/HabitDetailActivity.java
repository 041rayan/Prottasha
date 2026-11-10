package com.prottasha.habits.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityHabitHistoryBinding;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitSubstepEntity;
import com.prottasha.habits.data.HabitVacationEntity;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;

import java.time.LocalDate;
import java.util.List;

public final class HabitDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";

    private ActivityHabitHistoryBinding binding;
    private HabitDetailViewModel viewModel;
    private long habitId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, 0L);
        if (habitId == 0L) {
            finish();
            return;
        }

        ProttashaApp application = (ProttashaApp) getApplication();
        viewModel = new ViewModelProvider(this, new HabitDetailViewModel.Factory(
                application.appGraph().habitRepository(), habitId)).get(HabitDetailViewModel.class);

        binding.habitNoteSave.setOnClickListener(ignored -> {
            String note = binding.habitNoteInput.getText() == null ? ""
                    : binding.habitNoteInput.getText().toString();
            viewModel.saveNote(note);
            Toast.makeText(this, R.string.habit_note_saved, Toast.LENGTH_SHORT).show();
        });

        binding.habitEditButton.setOnClickListener(ignored ->
                startActivity(new Intent(this, HabitEditActivity.class)
                        .putExtra(HabitEditActivity.EXTRA_HABIT_ID, habitId)));

        binding.habitBackfillButton.setOnClickListener(ignored -> showBackfillDialog());
        binding.habitAddVacationButton.setOnClickListener(ignored -> showAddVacationDialog());

        binding.habitDeleteButton.setOnClickListener(ignored -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.habit_delete_title)
                    .setMessage(R.string.habit_delete_message)
                    .setPositiveButton(R.string.habit_delete, (dialog, which) ->
                            viewModel.delete(result -> runOnUiThread(this::finish)))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        viewModel.habit().observe(this, this::bindHabit);
        viewModel.stats().observe(this, this::bindStats);
        viewModel.history().observe(this, this::bindHistory);
        viewModel.substeps().observe(this, this::bindSubsteps);
        viewModel.vacations().observe(this, this::bindVacations);
        viewModel.note().observe(this, noteText -> {
            if (binding.habitNoteInput.getText() == null
                    || !binding.habitNoteInput.getText().toString().equals(noteText)) {
                binding.habitNoteInput.setText(noteText);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void bindHabit(HabitModels.HabitSnapshot h) {
        if (h == null) return;
        binding.habitHistoryTitle.setText(h.title);

        String unit = h.unit.isEmpty() ? "" : " " + h.unit;
        binding.habitHistorySubtitle.setText(
                h.type.name() + " · Target: " + h.target + unit + " · " + h.schedule.frequency.name());

        boolean hasCue = !h.cue.isEmpty();
        binding.habitDetailCue.setText("Cue: " + h.cue);
        binding.habitDetailCue.setVisibility(hasCue ? View.VISIBLE : View.GONE);

        boolean hasIfThen = !h.ifThenPlan.isEmpty();
        binding.habitDetailIfThen.setText("Plan: " + h.ifThenPlan);
        binding.habitDetailIfThen.setVisibility(hasIfThen ? View.VISIBLE : View.GONE);

        boolean hasRebound = !h.reboundPlan.isEmpty();
        binding.habitDetailRebound.setText("Rebound: " + h.reboundPlan);
        binding.habitDetailRebound.setVisibility(hasRebound ? View.VISIBLE : View.GONE);

        binding.habitMindfulCard.setVisibility(
                hasCue || hasIfThen || hasRebound ? View.VISIBLE : View.GONE);

        binding.habitDetailSubstepsCard.setVisibility(
                h.type == HabitModels.Type.BUILD ? View.VISIBLE : View.GONE);

        if (h.archived) {
            binding.habitArchiveButton.setText(R.string.habit_restore);
            binding.habitArchiveButton.setOnClickListener(v -> viewModel.restore());
        } else {
            binding.habitArchiveButton.setText(R.string.habit_archive);
            binding.habitArchiveButton.setOnClickListener(v -> viewModel.archive());
        }
    }

    private void bindStats(HabitModels.HabitStats s) {
        if (s == null || !s.hasData) {
            binding.habitStatsCurrentStreak.setText("Current streak: 0");
            binding.habitStatsBestStreak.setText("Best streak: 0");
            binding.habitStatsRate.setText("Consistency: 0%");
            binding.habitStatsTotal.setText("Total completions: 0");
            return;
        }
        binding.habitStatsCurrentStreak.setText(
                getString(R.string.habit_stats_current_streak, s.currentStreak));
        binding.habitStatsBestStreak.setText(
                getString(R.string.habit_stats_best_streak, s.bestStreak));
        binding.habitStatsRate.setText(
                getString(R.string.habit_stats_rate, (int) Math.round(s.consistency * 100)));
        binding.habitStatsTotal.setText(
                getString(R.string.habit_stats_total, s.totalCompletions));
    }

    private void bindHistory(List<HabitDayAggregateEntity> list) {
        binding.habitHistoryCalendar.setValues(list);
    }

    private void bindSubsteps(List<HabitSubstepEntity> list) {
        binding.habitDetailSubstepsContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No substeps defined.");
            empty.setTextColor(getResources().getColor(R.color.color_text_secondary, null));
            binding.habitDetailSubstepsContainer.addView(empty);
            return;
        }
        for (HabitSubstepEntity s : list) {
            TextView row = new TextView(this);
            row.setText("• " + s.title);
            row.setPadding(0, 4, 0, 4);
            row.setTextColor(getResources().getColor(R.color.color_text_primary, null));
            binding.habitDetailSubstepsContainer.addView(row);
        }
    }

    private void bindVacations(List<HabitVacationEntity> list) {
        binding.habitVacationsContainer.removeAllViews();
        if (list == null || list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No rest periods scheduled.");
            empty.setTextColor(getResources().getColor(R.color.color_text_secondary, null));
            binding.habitVacationsContainer.addView(empty);
            return;
        }
        for (HabitVacationEntity v : list) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 4, 0, 4);

            LocalDate start = LocalDate.ofEpochDay(v.startEpochDay);
            LocalDate end = LocalDate.ofEpochDay(v.endEpochDay);
            TextView text = new TextView(this);
            text.setText("Rest: " + start + " to " + end);
            text.setTextColor(getResources().getColor(R.color.color_text_primary, null));
            text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            MaterialButton removeBtn = new MaterialButton(this, null, androidx.appcompat.R.attr.borderlessButtonStyle);
            removeBtn.setText("✕");
            removeBtn.setOnClickListener(click -> viewModel.removeVacation(v.startEpochDay));

            row.addView(text);
            row.addView(removeBtn);
            binding.habitVacationsContainer.addView(row);
        }
    }

    private void showBackfillDialog() {
        String[] options = {"Yesterday", "2 days ago", "3 days ago"};
        new AlertDialog.Builder(this)
                .setTitle(R.string.habit_backfill_title)
                .setItems(options, (dialog, which) -> {
                    long targetDay = LocalDate.now().toEpochDay() - (which + 1);
                    showBackfillActionDialog(targetDay);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showBackfillActionDialog(long targetDay) {
        String[] actions = {"Mark Done", "Record Lapse"};
        new AlertDialog.Builder(this)
                .setTitle("Select Outcome for " + LocalDate.ofEpochDay(targetDay))
                .setItems(actions, (dialog, which) -> {
                    HabitAction.Kind kind = which == 0 ? HabitAction.Kind.COMPLETE : HabitAction.Kind.LAPSE;
                    viewModel.backfill(targetDay, kind, 1, result ->
                            runOnUiThread(() -> Toast.makeText(this, "Logged backfill", Toast.LENGTH_SHORT).show()));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAddVacationDialog() {
        String[] options = {"Next 3 Days", "Next 7 Days", "Next 14 Days"};
        new AlertDialog.Builder(this)
                .setTitle(R.string.habit_add_vacation)
                .setItems(options, (dialog, which) -> {
                    long startDay = LocalDate.now().toEpochDay();
                    long days = which == 0 ? 3 : (which == 1 ? 7 : 14);
                    viewModel.addVacation(startDay, startDay + days);
                    Toast.makeText(this, "Rest period added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
