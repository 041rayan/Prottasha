package com.prottasha.habits.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentHabitsBinding;
import com.prottasha.databinding.ItemHabitTodayBinding;
import com.prottasha.habits.domain.HabitModels;

import java.util.ArrayList;
import java.util.List;

public final class HabitsFragment extends Fragment {

    private FragmentHabitsBinding binding;
    private HabitsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProttashaApp application = (ProttashaApp) requireActivity().getApplication();
        viewModel = new ViewModelProvider(this, new HabitsViewModel.Factory(
                application.appGraph().habitRepository())).get(HabitsViewModel.class);

        binding.habitsAddButton.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), HabitEditActivity.class)));

        binding.habitsFilterGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.habits_filter_build) {
                viewModel.setFilter(HabitsViewModel.Filter.BUILD);
            } else if (checkedId == R.id.habits_filter_avoid) {
                viewModel.setFilter(HabitsViewModel.Filter.AVOID);
            } else if (checkedId == R.id.habits_filter_amount) {
                viewModel.setFilter(HabitsViewModel.Filter.AMOUNT);
            } else if (checkedId == R.id.habits_filter_archived) {
                viewModel.setFilter(HabitsViewModel.Filter.ARCHIVED);
            } else {
                viewModel.setFilter(HabitsViewModel.Filter.ALL);
            }
        });

        viewModel.filter().observe(getViewLifecycleOwner(), filter -> updateList());
        viewModel.items().observe(getViewLifecycleOwner(), items -> updateList());
        viewModel.activeHabits().observe(getViewLifecycleOwner(), active -> updateList());
        viewModel.archivedHabits().observe(getViewLifecycleOwner(), archived -> updateList());
    }

    private void updateList() {
        if (viewModel == null || binding == null) return;
        HabitsViewModel.Filter currentFilter = viewModel.filter().getValue();
        if (currentFilter == null) currentFilter = HabitsViewModel.Filter.ALL;

        if (currentFilter == HabitsViewModel.Filter.ARCHIVED) {
            renderArchived(viewModel.archivedHabits().getValue());
        } else {
            renderActive(viewModel.items().getValue(), currentFilter);
        }
    }

    private void renderActive(List<HabitModels.HabitTodayItem> items, HabitsViewModel.Filter filter) {
        binding.habitsContainer.removeAllViews();
        List<HabitModels.HabitTodayItem> filtered = new ArrayList<>();
        if (items != null) {
            for (HabitModels.HabitTodayItem item : items) {
                if (filter == HabitsViewModel.Filter.ALL) {
                    filtered.add(item);
                } else if (filter == HabitsViewModel.Filter.BUILD && item.habit.type == HabitModels.Type.BUILD) {
                    filtered.add(item);
                } else if (filter == HabitsViewModel.Filter.AVOID && item.habit.type == HabitModels.Type.AVOID) {
                    filtered.add(item);
                } else if (filter == HabitsViewModel.Filter.AMOUNT && item.habit.type == HabitModels.Type.AMOUNT) {
                    filtered.add(item);
                }
            }
        }

        binding.habitsEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        List<HabitModels.HabitSnapshot> activeList = viewModel.activeHabits().getValue();
        if (activeList == null) activeList = new ArrayList<>();

        for (int i = 0; i < filtered.size(); i++) {
            HabitModels.HabitTodayItem item = filtered.get(i);
            ItemHabitTodayBinding row = ItemHabitTodayBinding.inflate(
                    inflater, binding.habitsContainer, false);
            row.habitTitle.setText(item.habit.title);
            row.habitType.setText(item.habit.type.name());

            String unit = item.habit.unit.isEmpty() ? "" : " " + item.habit.unit;
            row.habitProgress.setText(getString(
                    R.string.habit_progress, item.value, item.habit.target) + unit);

            if (!item.habit.cue.isEmpty()) {
                row.habitCueSummary.setText(item.habit.cue);
                row.habitCueSummary.setVisibility(View.VISIBLE);
            }

            row.habitAction.setText(actionText(item));
            row.habitAction.setEnabled(!item.completed);
            row.habitAction.setOnClickListener(ignored -> {
                row.habitAction.setEnabled(false);
                viewModel.act(item, result -> {
                    if (result.isSuccess() && result.valueOrNull() != null) {
                        long eventId = result.valueOrNull().eventId;
                        if (eventId > 0 && getView() != null) {
                            Snackbar.make(binding.getRoot(), R.string.habit_note_saved, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.habit_undo, undoView ->
                                            viewModel.undo(item.habit.id, eventId, undoResult -> {}))
                                    .show();
                        }
                    }
                });
            });

            row.habitArchive.setOnClickListener(ignored -> viewModel.archive(item.habit.id));

            final List<HabitModels.HabitSnapshot> listRef = activeList;
            row.habitMoveUp.setOnClickListener(ignored -> viewModel.moveUp(item.habit, listRef));
            row.habitMoveDown.setOnClickListener(ignored -> viewModel.moveDown(item.habit, listRef));

            row.getRoot().setOnClickListener(ignored -> startActivity(
                    new Intent(requireContext(), HabitDetailActivity.class)
                            .putExtra(HabitDetailActivity.EXTRA_HABIT_ID, item.habit.id)));

            binding.habitsContainer.addView(row.getRoot());
        }
    }

    private void renderArchived(List<HabitModels.HabitSnapshot> archived) {
        binding.habitsContainer.removeAllViews();
        if (archived == null || archived.isEmpty()) {
            binding.habitsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        binding.habitsEmpty.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (HabitModels.HabitSnapshot habit : archived) {
            ItemHabitTodayBinding row = ItemHabitTodayBinding.inflate(
                    inflater, binding.habitsContainer, false);
            row.habitTitle.setText(habit.title);
            row.habitType.setText(habit.type.name() + " (Archived)");
            row.habitProgress.setText("Target: " + habit.target);

            row.habitAction.setVisibility(View.GONE);
            row.habitArchive.setVisibility(View.GONE);
            row.habitMoveUp.setVisibility(View.GONE);
            row.habitMoveDown.setVisibility(View.GONE);

            row.habitRestore.setVisibility(View.VISIBLE);
            row.habitRestore.setOnClickListener(ignored -> viewModel.restore(habit.id));

            row.habitDelete.setVisibility(View.VISIBLE);
            row.habitDelete.setOnClickListener(ignored -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.habit_delete_title)
                        .setMessage(R.string.habit_delete_message)
                        .setPositiveButton(R.string.habit_delete, (d, w) -> viewModel.delete(habit.id))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            });

            row.getRoot().setOnClickListener(ignored -> startActivity(
                    new Intent(requireContext(), HabitDetailActivity.class)
                            .putExtra(HabitDetailActivity.EXTRA_HABIT_ID, habit.id)));

            binding.habitsContainer.addView(row.getRoot());
        }
    }

    private String actionText(HabitModels.HabitTodayItem item) {
        if (item.completed) return getString(R.string.habit_action_done);
        if (item.habit.type == HabitModels.Type.AMOUNT) {
            int inc = item.habit.incrementVal > 0 ? item.habit.incrementVal : 1;
            return "+" + inc;
        }
        if (item.habit.type == HabitModels.Type.AVOID) return getString(R.string.habit_action_lapse);
        return getString(R.string.habit_action_done);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
