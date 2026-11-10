package com.prottasha.insights.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.databinding.ItemInsightTriggerBinding;
import com.prottasha.triggers.data.TriggerPlanEntity;

import java.util.ArrayList;
import java.util.List;

public final class TriggerPlansAdapter extends RecyclerView.Adapter<TriggerPlansAdapter.ViewHolder> {
    public interface OnDeleteClickListener {
        void onDeleteClick(TriggerPlanEntity plan);
    }

    private final List<TriggerPlanEntity> items = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public TriggerPlansAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TriggerPlanEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemInsightTriggerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TriggerPlanEntity item = items.get(position);
        holder.binding.triggerCueText.setText("When: " + item.cue);
        holder.binding.triggerPlanText.setText("Then: " + item.plan);
        holder.binding.triggerDeleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ItemInsightTriggerBinding binding;
        ViewHolder(ItemInsightTriggerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
