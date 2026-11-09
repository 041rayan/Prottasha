package com.prottasha.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.data.model.TherapistItem;
import com.prottasha.databinding.ItemTherapistBinding;

import java.util.ArrayList;
import java.util.List;

public class TherapistAdapter extends RecyclerView.Adapter<TherapistAdapter.TherapistViewHolder> {

    public interface OnTherapistCallListener {
        void onCallClick(TherapistItem item);
    }

    private final List<TherapistItem> items = new ArrayList<>();
    private final OnTherapistCallListener listener;

    public TherapistAdapter(OnTherapistCallListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TherapistItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TherapistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTherapistBinding binding = ItemTherapistBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TherapistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TherapistViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class TherapistViewHolder extends RecyclerView.ViewHolder {
        private final ItemTherapistBinding binding;

        public TherapistViewHolder(ItemTherapistBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnDialClinic.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCallClick(items.get(pos));
                }
            });
        }

        public void bind(TherapistItem item) {
            binding.tvClinicName.setText(item.getName());
            binding.tvClinicSpecialization.setText(item.getSpecialization());
            binding.tvClinicHours.setText("Hours: " + item.getHours() + " • Phone: " + item.getPhone());
        }
    }
}
