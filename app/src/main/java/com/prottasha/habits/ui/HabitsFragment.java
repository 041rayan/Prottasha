package com.prottasha.habits.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentHabitsBinding;
import com.prottasha.databinding.ItemHabitTodayBinding;
import com.prottasha.habits.domain.HabitModels;

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
        viewModel.items().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(List<HabitModels.HabitTodayItem> items) {
        binding.habitsContainer.removeAllViews();
        binding.habitsEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (HabitModels.HabitTodayItem item : items) {
            ItemHabitTodayBinding row = ItemHabitTodayBinding.inflate(
                    inflater, binding.habitsContainer, false);
            row.habitTitle.setText(item.habit.title);
            row.habitType.setText(item.habit.type.name());
            row.habitProgress.setText(getString(
                    R.string.habit_progress, item.value, item.habit.target));
            row.habitAction.setText(actionText(item));
            row.habitAction.setEnabled(!item.completed);
            row.habitAction.setOnClickListener(ignored -> viewModel.act(item));
            row.habitArchive.setOnClickListener(ignored -> viewModel.archive(item.habit.id));
            row.getRoot().setOnClickListener(ignored -> startActivity(
                    new Intent(requireContext(), HabitDetailActivity.class)
                            .putExtra(HabitDetailActivity.EXTRA_HABIT_ID, item.habit.id)));
            binding.habitsContainer.addView(row.getRoot());
        }
    }

    private int actionText(HabitModels.HabitTodayItem item) {
        switch (item.habit.type) {
            case AVOID:
                return R.string.habit_action_lapse;
            case AMOUNT:
                return R.string.habit_action_increment;
            default:
                return R.string.habit_action_done;
        }
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
