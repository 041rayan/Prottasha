package com.prottasha.insights.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.databinding.ItemInsightMemoryBinding;
import com.prottasha.identity.data.SuccessMemoryEntity;

import java.util.ArrayList;
import java.util.List;

public final class SuccessMemoriesAdapter extends RecyclerView.Adapter<SuccessMemoriesAdapter.ViewHolder> {
    public interface OnDeleteClickListener {
        void onDeleteClick(SuccessMemoryEntity memory);
    }

    private final List<SuccessMemoryEntity> items = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public SuccessMemoriesAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<SuccessMemoryEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemInsightMemoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SuccessMemoryEntity item = items.get(position);
        holder.binding.memoryText.setText(item.text);
        holder.binding.memoryDeleteButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ItemInsightMemoryBinding binding;
        ViewHolder(ItemInsightMemoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
