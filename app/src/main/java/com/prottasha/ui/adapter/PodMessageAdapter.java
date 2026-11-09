package com.prottasha.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.data.model.PodMessage;
import com.prottasha.databinding.ItemPodMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class PodMessageAdapter extends RecyclerView.Adapter<PodMessageAdapter.PodMessageViewHolder> {

    private final List<PodMessage> messages = new ArrayList<>();

    public void addMessage(PodMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<PodMessage> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PodMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPodMessageBinding binding = ItemPodMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PodMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PodMessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class PodMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemPodMessageBinding binding;

        public PodMessageViewHolder(ItemPodMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(PodMessage message) {
            binding.tvSender.setText(message.getSender());
            binding.tvMessageText.setText(message.getText());
        }
    }
}
