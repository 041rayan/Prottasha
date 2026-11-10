package com.prottasha.insights.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.databinding.ItemInsightTargetBinding;
import com.prottasha.insights.domain.InsightsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TargetBreakdownAdapter extends RecyclerView.Adapter<TargetBreakdownAdapter.ViewHolder> {
    private final List<InsightsCalculator.TargetUsageSummary> items = new ArrayList<>();

    public void setItems(List<InsightsCalculator.TargetUsageSummary> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemInsightTargetBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InsightsCalculator.TargetUsageSummary item = items.get(position);
        holder.binding.targetName.setText(item.target);

        long minutes = item.totalMillis / 60_000L;
        String durationText = minutes >= 60
                ? String.format(Locale.ROOT, "%dh %dm", minutes / 60, minutes % 60)
                : String.format(Locale.ROOT, "%dm", minutes);

        holder.binding.targetStats.setText(String.format(
                Locale.ROOT, "%s · %d opens · %d sessions", durationText, item.opens, item.sessions));
        holder.binding.targetQualityBadge.setText(item.quality.name());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final ItemInsightTargetBinding binding;
        ViewHolder(ItemInsightTargetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
