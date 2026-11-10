package com.prottasha.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.data.local.entity.HabitEntity;
import com.prottasha.databinding.ItemHabitBinding;

import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface OnHabitInteractionListener {
        void onHabitClick(HabitEntity habit);
        void onHabitLongClick(HabitEntity habit);
        void onPositiveVoteClick(HabitEntity habit);
        void onMissClick(HabitEntity habit);
    }

    private final List<HabitEntity> habits = new ArrayList<>();
    private final OnHabitInteractionListener listener;

    public HabitAdapter(OnHabitInteractionListener listener) {
        this.listener = listener;
    }

    public void setHabits(List<HabitEntity> newHabits) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return habits.size(); }
            @Override
            public int getNewListSize() { return newHabits.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return habits.get(oldItemPosition).getId() == newHabits.get(newItemPosition).getId();
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                HabitEntity oldItem = habits.get(oldItemPosition);
                HabitEntity newItem = newHabits.get(newItemPosition);
                return oldItem.getVotesFor() == newItem.getVotesFor() &&
                       oldItem.getVotesAgainst() == newItem.getVotesAgainst() &&
                       oldItem.getCurrentStreak() == newItem.getCurrentStreak() &&
                       oldItem.getConsecutiveMisses() == newItem.getConsecutiveMisses() &&
                       oldItem.getTitle().equals(newItem.getTitle());
            }
        });
        habits.clear();
        habits.addAll(newHabits);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitBinding binding = ItemHabitBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(habits.get(position));
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitBinding binding;

        public HabitViewHolder(ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHabitClick(habits.get(pos));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHabitLongClick(habits.get(pos));
                    return true;
                }
                return false;
            });

            binding.btnVotePositive.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPositiveVoteClick(habits.get(pos));
                }
            });

            binding.btnRecordMiss.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMissClick(habits.get(pos));
                }
            });
        }

        public void bind(HabitEntity habit) {
            binding.tvHabitTitle.setText(habit.getTitle());
            binding.tvHabitBadge.setText(habit.isGoodHabit() ? "GOOD HABIT" : "HABIT TO BREAK");
            binding.tvHabitBadge.setTextColor(habit.isGoodHabit() ? 0xFF10B981 : 0xFFF59E0B);

            // Rebound Badge (AH-18 Never Miss Twice)
            if (habit.getConsecutiveMisses() == 1) {
                binding.tvReboundBadge.setVisibility(View.VISIBLE);
            } else {
                binding.tvReboundBadge.setVisibility(View.GONE);
            }

            String responseText = "Step: " + (habit.getResponse() != null ? habit.getResponse() : "Take 2-minute starter action");
            binding.tvHabitResponse.setText(responseText);

            String stats = habit.getCurrentStreak() + "-day streak • " + habit.getVotesFor() + " votes cast";
            binding.tvStreakAndVotes.setText(stats);
        }
    }
}
