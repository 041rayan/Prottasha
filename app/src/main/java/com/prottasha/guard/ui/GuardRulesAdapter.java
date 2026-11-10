package com.prottasha.guard.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.R;
import com.prottasha.databinding.ItemGuardRuleBinding;
import com.prottasha.guard.data.GuardRuleEntity;

import java.util.Locale;
import java.util.Objects;

public final class GuardRulesAdapter extends ListAdapter<GuardRuleEntity, GuardRulesAdapter.ViewHolder> {

    public interface Listener {
        void onToggle(long ruleId, boolean enabled);
        void onClick(GuardRuleEntity rule);
        void onLongClick(GuardRuleEntity rule);
    }

    private final Listener listener;

    public GuardRulesAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGuardRuleBinding binding = ItemGuardRuleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuardRuleEntity rule = getItem(position);
        holder.bind(rule, listener);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuardRuleBinding binding;

        ViewHolder(ItemGuardRuleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(GuardRuleEntity rule, Listener listener) {
            binding.guardRuleItemLabel.setText(rule.label);
            binding.guardRuleItemTarget.setText(rule.target + (rule.isDomain ? " • Website" : " • App"));

            // Avoid triggering listener during rebind
            binding.guardRuleItemToggle.setOnCheckedChangeListener(null);
            binding.guardRuleItemToggle.setChecked(rule.enabled);
            binding.guardRuleItemToggle.setOnCheckedChangeListener((btn, isChecked) -> {
                if (listener != null) {
                    listener.onToggle(rule.id, isChecked);
                }
            });

            if ("BUDGET".equals(rule.mode)) {
                binding.guardRuleItemModeBadge.setText(String.format(Locale.ROOT, "Budget: %d min/day", rule.budgetMinutes));
                binding.guardRuleItemModeBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_hope));
                binding.guardRuleItemModeBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.color_hope_subtle));
            } else if ("BLOCK".equals(rule.mode)) {
                binding.guardRuleItemModeBadge.setText("Block");
                binding.guardRuleItemModeBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_crisis));
                binding.guardRuleItemModeBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.color_crisis_subtle));
            } else {
                binding.guardRuleItemModeBadge.setText(String.format(Locale.ROOT, "Pause (%ds delay)", rule.delaySeconds));
                binding.guardRuleItemModeBadge.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_friction));
                binding.guardRuleItemModeBadge.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.color_friction_subtle));
            }

            if (rule.startMinute != rule.endMinute) {
                binding.guardRuleItemSchedule.setVisibility(View.VISIBLE);
                binding.guardRuleItemSchedule.setText(String.format(Locale.ROOT, "%02d:%02d - %02d:%02d",
                        rule.startMinute / 60, rule.startMinute % 60,
                        rule.endMinute / 60, rule.endMinute % 60));
            } else {
                binding.guardRuleItemSchedule.setVisibility(View.GONE);
            }

            binding.guardRuleItemProtected.setVisibility(rule.editProtection ? View.VISIBLE : View.GONE);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onClick(rule);
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onLongClick(rule);
                    return true;
                }
                return false;
            });
        }
    }

    private static final DiffUtil.ItemCallback<GuardRuleEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull GuardRuleEntity oldItem, @NonNull GuardRuleEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull GuardRuleEntity oldItem, @NonNull GuardRuleEntity newItem) {
                    return oldItem.enabled == newItem.enabled
                            && Objects.equals(oldItem.mode, newItem.mode)
                            && oldItem.delaySeconds == newItem.delaySeconds
                            && oldItem.budgetMinutes == newItem.budgetMinutes
                            && oldItem.startMinute == newItem.startMinute
                            && oldItem.endMinute == newItem.endMinute
                            && oldItem.editProtection == newItem.editProtection
                            && Objects.equals(oldItem.label, newItem.label)
                            && Objects.equals(oldItem.target, newItem.target);
                }
            };
}
