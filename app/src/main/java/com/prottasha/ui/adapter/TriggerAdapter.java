package com.prottasha.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.data.local.entity.TriggerEntity;
import com.prottasha.databinding.ItemTriggerBinding;

import java.util.ArrayList;
import java.util.List;

public class TriggerAdapter extends RecyclerView.Adapter<TriggerAdapter.TriggerViewHolder> {

    public interface OnTriggerInteractionListener {
        void OnTriggerClick(TriggerEntity trigger);
        void OnTriggerLongClick(TriggerEntity trigger);
        void onOvercomeClick(TriggerEntity trigger);
    }

    private final List<TriggerEntity> triggers = new ArrayList<>();
    private final OnTriggerInteractionListener listener;

    public TriggerAdapter(OnTriggerInteractionListener listener) {
        this.listener = listener;
    }

    public void setTriggers(List<TriggerEntity> newTriggers) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return triggers.size(); }
            @Override
            public int getNewListSize() { return newTriggers.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return triggers.get(oldItemPosition).getId() == newTriggers.get(newItemPosition).getId();
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                TriggerEntity oldItem = triggers.get(oldItemPosition);
                TriggerEntity newItem = newTriggers.get(newItemPosition);
                return oldItem.getTimesEncountered() == newItem.getTimesEncountered() &&
                       oldItem.getTimesOvercome() == newItem.getTimesOvercome() &&
                       oldItem.getSituationCue().equals(newItem.getSituationCue());
            }
        });
        triggers.clear();
        triggers.addAll(newTriggers);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public TriggerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTriggerBinding binding = ItemTriggerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TriggerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TriggerViewHolder holder, int position) {
        holder.bind(triggers.get(position));
    }

    @Override
    public int getItemCount() {
        return triggers.size();
    }

    class TriggerViewHolder extends RecyclerView.ViewHolder {
        private final ItemTriggerBinding binding;

        public TriggerViewHolder(ItemTriggerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.OnTriggerClick(triggers.get(pos));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.OnTriggerLongClick(triggers.get(pos));
                    return true;
                }
                return false;
            });

            binding.btnOvercomeTrigger.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOvercomeClick(triggers.get(pos));
                }
            });
        }

        public void bind(TriggerEntity trigger) {
            binding.tvTriggerSituation.setText(trigger.getSituationCue());
            binding.tvIfThenPlan.setText(trigger.getIfThenPlan());

            int percentage = trigger.getTimesEncountered() > 0 ?
                    (int) Math.round(((double) trigger.getTimesOvercome() / trigger.getTimesEncountered()) * 100.0) : 100;
            String stats = "Encountered " + trigger.getTimesEncountered() + " times • Overcome "
                    + trigger.getTimesOvercome() + " times (" + percentage + "%)";
            binding.tvTriggerStats.setText(stats);
        }
    }
}
