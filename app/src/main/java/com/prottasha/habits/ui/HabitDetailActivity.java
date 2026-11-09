package com.prottasha.habits.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.ActivityHabitHistoryBinding;
import com.prottasha.habits.data.HabitDayNoteEntity;
import com.prottasha.habits.data.HabitEntity;

import java.time.LocalDate;

public final class HabitDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HABIT_ID = "habit_id";

    private ActivityHabitHistoryBinding binding;
    private long habitId;
    private long epochDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHabitHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, 0L);
        epochDay = LocalDate.now().toEpochDay();
        if (habitId == 0L) {
            finish();
            return;
        }
        load();
        binding.habitNoteSave.setOnClickListener(ignored -> saveNote());
    }

    private void load() {
        ProttashaApp application = (ProttashaApp) getApplication();
        application.appGraph().executors().disk().execute(() -> {
            HabitEntity habit = application.appGraph().userSessionDatabaseProvider()
                    .requireOpen().habitDao().find(habitId);
            HabitDayNoteEntity note = application.appGraph().userSessionDatabaseProvider()
                    .requireOpen().habitDao().findNote(habitId, epochDay);
            java.util.List<com.prottasha.habits.data.HabitDayAggregateEntity> history =
                    application.appGraph().userSessionDatabaseProvider().requireOpen()
                            .habitDao().listAggregates(habitId, 28);
            runOnUiThread(() -> {
                binding.habitHistoryTitle.setText(habit == null ? "" : habit.title);
                binding.habitHistoryCalendar.setValues(history);
                binding.habitNoteInput.setText(note == null ? "" : note.note);
            });
        });
    }

    private void saveNote() {
        String note = binding.habitNoteInput.getText() == null
                ? "" : binding.habitNoteInput.getText().toString();
        ProttashaApp application = (ProttashaApp) getApplication();
        application.appGraph().executors().disk().execute(() -> {
            application.appGraph().userSessionDatabaseProvider().requireOpen().habitDao()
                    .upsertNote(new HabitDayNoteEntity(
                            habitId, epochDay, note, System.currentTimeMillis()));
            runOnUiThread(() -> binding.habitNoteSave.setText(R.string.habit_note_saved));
        });
    }
}
