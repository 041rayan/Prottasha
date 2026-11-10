package com.prottasha.habits.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.R;
import com.prottasha.databinding.ItemHabitTodayBinding;
import com.prottasha.habits.domain.HabitModels;

import java.util.ArrayList;
import java.util.List;

public final class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.ViewHolder> {

    public interface Listener {
        void onAct(HabitModels.HabitTodayItem item, View actionBtn);
        void onArchive(long habitId);
        void onMoveUp(HabitModels.HabitSnapshot habit, List<HabitModels.HabitSnapshot> list);
        void onMoveDown(HabitModels.HabitSnapshot habit, List<HabitModels.HabitSnapshot> list);
        void onClick(long habitId);
        void onRestore(long habitId);
        void onDelete(long habitId);
    }

    private final Listener listener;
    private final List<HabitModels.HabitTodayItem> activeItems = new ArrayList<>();
    private final List<HabitModels.HabitSnapshot> archivedItems = new ArrayList<>();
    private List<HabitModels.HabitSnapshot> fullActiveList = new ArrayList<>();
    private boolean isArchivedMode = false;

    public HabitsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setActiveItems(List<HabitModels.HabitTodayItem> items, List<HabitModels.HabitSnapshot> activeList) {
        this.isArchivedMode = false;
        this.activeItems.clear();
        if (items != null) this.activeItems.addAll(items);
        this.archivedItems.clear();
        this.fullActiveList = activeList != null ? activeList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setArchivedItems(List<HabitModels.HabitSnapshot> items) {
        this.isArchivedMode = true;
        this.archivedItems.clear();
        if (items != null) this.archivedItems.addAll(items);
        this.activeItems.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitTodayBinding binding = ItemHabitTodayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isArchivedMode) {
            HabitModels.HabitSnapshot habit = archivedItems.get(position);
            holder.bindArchived(habit, listener);
        } else {
            HabitModels.HabitTodayItem item = activeItems.get(position);
            holder.bindActive(item, fullActiveList, listener);
        }
    }

    @Override
    public int getItemCount() {
        return isArchivedMode ? archivedItems.size() : activeItems.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ItemHabitTodayBinding binding;

        ViewHolder(ItemHabitTodayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindActive(HabitModels.HabitTodayItem item, List<HabitModels.HabitSnapshot> fullList, Listener listener) {
            Context ctx = itemView.getContext();
            binding.habitTitle.setText(item.habit.title);
            binding.habitType.setText(item.habit.type.name());

            String unit = item.habit.unit.isEmpty() ? "" : " " + item.habit.unit;
            binding.habitProgress.setText(ctx.getString(
                    R.string.habit_progress, item.value, item.habit.target) + unit);

            if (!item.habit.cue.isEmpty()) {
                binding.habitCueSummary.setText(item.habit.cue);
                binding.habitCueSummary.setVisibility(View.VISIBLE);
            } else {
                binding.habitCueSummary.setVisibility(View.GONE);
            }

            binding.habitAction.setVisibility(View.VISIBLE);
            binding.habitAction.setText(actionText(ctx, item));
            binding.habitAction.setEnabled(!item.completed);
            binding.habitAction.setOnClickListener(v -> {
                binding.habitAction.setEnabled(false);
                if (listener != null) listener.onAct(item, binding.habitAction);
            });

            binding.habitArchive.setVisibility(View.VISIBLE);
            binding.habitArchive.setOnClickListener(v -> {
                if (listener != null) listener.onArchive(item.habit.id);
            });

            binding.habitMoveUp.setVisibility(View.VISIBLE);
            binding.habitMoveUp.setOnClickListener(v -> {
                if (listener != null) listener.onMoveUp(item.habit, fullList);
            });

            binding.habitMoveDown.setVisibility(View.VISIBLE);
            binding.habitMoveDown.setOnClickListener(v -> {
                if (listener != null) listener.onMoveDown(item.habit, fullList);
            });

            binding.habitRestore.setVisibility(View.GONE);
            binding.habitDelete.setVisibility(View.GONE);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(item.habit.id);
            });
        }

        void bindArchived(HabitModels.HabitSnapshot habit, Listener listener) {
            binding.habitTitle.setText(habit.title);
            binding.habitType.setText(habit.type.name() + " (Archived)");
            binding.habitProgress.setText("Target: " + habit.target);
            binding.habitCueSummary.setVisibility(View.GONE);

            binding.habitAction.setVisibility(View.GONE);
            binding.habitArchive.setVisibility(View.GONE);
            binding.habitMoveUp.setVisibility(View.GONE);
            binding.habitMoveDown.setVisibility(View.GONE);

            binding.habitRestore.setVisibility(View.VISIBLE);
            binding.habitRestore.setOnClickListener(v -> {
                if (listener != null) listener.onRestore(habit.id);
            });

            binding.habitDelete.setVisibility(View.VISIBLE);
            binding.habitDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(habit.id);
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(habit.id);
            });
        }

        private String actionText(Context ctx, HabitModels.HabitTodayItem item) {
            if (item.completed) return ctx.getString(R.string.habit_action_done);
            if (item.habit.type == HabitModels.Type.AMOUNT) {
                int inc = item.habit.incrementVal > 0 ? item.habit.incrementVal : 1;
                return "+" + inc;
            }
            if (item.habit.type == HabitModels.Type.AVOID) return ctx.getString(R.string.habit_action_lapse);
            return ctx.getString(R.string.habit_action_done);
        }
    }
}
