package com.prottasha.insights.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.databinding.ItemInsightReflectionBinding;
import com.prottasha.insights.data.JournalEntryEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ReflectionsAdapter extends RecyclerView.Adapter<ReflectionsAdapter.ViewHolder> {
    public interface OnReflectionClickListener {
        void onReflectionClick(JournalEntryEntity entry);
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
    private final List<JournalEntryEntity> items = new ArrayList<>();
    private final OnReflectionClickListener listener;

    public ReflectionsAdapter(OnReflectionClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<JournalEntryEntity> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemInsightReflectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntryEntity item = items.get(position);
        LocalDate date = LocalDate.ofEpochDay(item.epochDay);
        holder.binding.reflectionDate.setText(date.format(DATE_FMT));
        holder.binding.reflectionFactDisplay.setText("Fact: " + item.fact);
        holder.binding.reflectionInterpretationDisplay.setText("Interpretation: " + item.interpretation);
        holder.binding.reflectionLessonDisplay.setText("Lesson: " + item.lesson);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onReflectionClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ItemInsightReflectionBinding binding;
        ViewHolder(ItemInsightReflectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
