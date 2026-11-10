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

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentHabitsBinding;
import com.prottasha.databinding.ItemHabitTodayBinding;
import com.prottasha.habits.domain.HabitModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HabitsFragment extends Fragment {

    private FragmentHabitsBinding binding;
    private HabitsViewModel viewModel;
    private HabitsAdapter adapter;

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

        adapter = new HabitsAdapter(new HabitsAdapter.Listener() {
            @Override
            public void onAct(HabitModels.HabitTodayItem item, View actionBtn) {
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
            }

            @Override
            public void onArchive(long habitId) {
                viewModel.archive(habitId);
            }

            @Override
            public void onMoveUp(HabitModels.HabitSnapshot habit, List<HabitModels.HabitSnapshot> list) {
                viewModel.moveUp(habit, list);
            }

            @Override
            public void onMoveDown(HabitModels.HabitSnapshot habit, List<HabitModels.HabitSnapshot> list) {
                viewModel.moveDown(habit, list);
            }

            @Override
            public void onClick(long habitId) {
                startActivity(new Intent(requireContext(), HabitDetailActivity.class)
                        .putExtra(HabitDetailActivity.EXTRA_HABIT_ID, habitId));
            }

            @Override
            public void onRestore(long habitId) {
                viewModel.restore(habitId);
            }

            @Override
            public void onDelete(long habitId) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.habit_delete_title)
                        .setMessage(R.string.habit_delete_message)
                        .setPositiveButton(R.string.habit_delete, (d, w) -> viewModel.delete(habitId))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        binding.habitsContainer.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.habitsContainer.setAdapter(adapter);

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
        List<HabitModels.HabitSnapshot> activeList = viewModel.activeHabits().getValue();
        adapter.setActiveItems(filtered, activeList);
    }

    private void renderArchived(List<HabitModels.HabitSnapshot> archived) {
        if (archived == null || archived.isEmpty()) {
            binding.habitsEmpty.setVisibility(View.VISIBLE);
            adapter.setArchivedItems(Collections.emptyList());
            return;
        }
        binding.habitsEmpty.setVisibility(View.GONE);
        adapter.setArchivedItems(archived);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
