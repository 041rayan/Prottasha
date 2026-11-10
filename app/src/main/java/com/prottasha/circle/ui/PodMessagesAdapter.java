package com.prottasha.circle.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prottasha.R;
import com.prottasha.circle.domain.PodMessage;
import com.prottasha.databinding.ItemPodMessageBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PodMessagesAdapter extends RecyclerView.Adapter<PodMessagesAdapter.ViewHolder> {

    public interface MessageActionListener {
        void onEdit(PodMessage message);
        void onDelete(PodMessage message);
        void onReact(PodMessage message, String emoji);
        void onReport(PodMessage message);
        void onBlock(String senderUid, String pseudonym);
    }

    private final List<PodMessage> items = new ArrayList<>();
    private final String currentUid;
    private final MessageActionListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public PodMessagesAdapter(
            @NonNull String currentUid,
            @NonNull MessageActionListener listener) {
        this.currentUid = currentUid;
        this.listener = listener;
    }

    public void submitList(List<PodMessage> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPodMessageBinding binding = ItemPodMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    final class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPodMessageBinding binding;

        ViewHolder(ItemPodMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PodMessage message) {
            Context context = itemView.getContext();

            // Avatar
            int avatarColor = DeterministicAvatar.getColor(message.pseudonym);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(avatarColor);
            binding.messageAvatarFrame.setBackground(shape);
            binding.messageAvatarInitials.setText(DeterministicAvatar.getInitials(message.pseudonym));
            binding.messageAvatarFrame.setContentDescription("Avatar for " + message.pseudonym);

            // Pseudonym and timestamp
            binding.messageSenderPseudonym.setText(message.pseudonym);
            binding.messageTimestamp.setText(message.timestampMillis > 0
                    ? timeFormat.format(new Date(message.timestampMillis))
                    : "");
            binding.messageEditedBadge.setVisibility(message.isEdited() ? View.VISIBLE : View.GONE);

            // Message text
            binding.messageText.setText(message.text);

            // Reactions
            Map<String, Integer> counts = message.reactionCounts();
            binding.messageReactionsContainer.removeAllViews();
            if (!counts.isEmpty()) {
                binding.messageReactionsContainer.setVisibility(View.VISIBLE);
                for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                    TextView chip = new TextView(context);
                    chip.setText(entry.getKey() + " " + entry.getValue());
                    chip.setTextSize(12f);
                    chip.setPadding(12, 4, 12, 4);
                    chip.setTextColor(context.getColor(R.color.color_text_primary));
                    chip.setBackgroundResource(R.drawable.shape_pill);
                    binding.messageReactionsContainer.addView(chip);
                }
            } else {
                binding.messageReactionsContainer.setVisibility(View.GONE);
            }

            // Options menu
            binding.messageOptions.setOnClickListener(v -> showMenu(v, message));
        }

        private void showMenu(View anchor, PodMessage message) {
            PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
            boolean isOwn = message.senderUid != null && message.senderUid.equals(currentUid);

            if (isOwn) {
                menu.getMenu().add(0, 1, 0, "Edit message");
                menu.getMenu().add(0, 2, 1, "Delete message");
            } else {
                menu.getMenu().add(0, 10, 0, "React 👍");
                menu.getMenu().add(0, 11, 1, "React ❤️");
                menu.getMenu().add(0, 12, 2, "React 🙏");
                menu.getMenu().add(0, 13, 3, "Report message");
                menu.getMenu().add(0, 14, 4, "Block " + message.pseudonym);
            }

            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == 1) {
                    listener.onEdit(message);
                    return true;
                } else if (id == 2) {
                    listener.onDelete(message);
                    return true;
                } else if (id == 10) {
                    listener.onReact(message, "👍");
                    return true;
                } else if (id == 11) {
                    listener.onReact(message, "❤️");
                    return true;
                } else if (id == 12) {
                    listener.onReact(message, "🙏");
                    return true;
                } else if (id == 13) {
                    listener.onReport(message);
                    return true;
                } else if (id == 14) {
                    listener.onBlock(message.senderUid, message.pseudonym);
                    return true;
                }
                return false;
            });
            menu.show();
        }
    }
}
