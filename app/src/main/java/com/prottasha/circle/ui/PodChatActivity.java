package com.prottasha.circle.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.domain.PodMessage;
import com.prottasha.databinding.ActivityPodChatNewBinding;

public final class PodChatActivity extends AppCompatActivity implements PodMessagesAdapter.MessageActionListener {

    private ActivityPodChatNewBinding binding;
    private CircleViewModel viewModel;
    private PodMessagesAdapter adapter;
    private SessionUser user;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityPodChatNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ProttashaApp app = (ProttashaApp) getApplication();
        try {
            user = app.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException noUser) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CircleViewModel.class);

        setupRecyclerView();
        setupInput();
        setupToolbar();
        observeViewModel();

        viewModel.init(user);
    }

    private void setupRecyclerView() {
        adapter = new PodMessagesAdapter(user.uid(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.podRecyclerView.setLayoutManager(layoutManager);
        binding.podRecyclerView.setAdapter(adapter);
    }

    private void setupInput() {
        binding.podSend.setOnClickListener(v -> {
            String text = binding.podMessageInput.getText() != null
                    ? binding.podMessageInput.getText().toString().trim()
                    : "";
            if (!text.isEmpty()) {
                viewModel.sendMessage(text);
                binding.podMessageInput.setText("");
            }
        });

        binding.podMessageInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    viewModel.onUserTyping();
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupToolbar() {
        binding.podBackButton.setOnClickListener(v -> finish());
        binding.podLeaveButton.setOnClickListener(v -> showLeaveConfirmation());
    }

    private void observeViewModel() {
        viewModel.messages().observe(this, messages -> {
            adapter.submitList(messages);
            binding.podEmptyView.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
            if (!messages.isEmpty()) {
                binding.podRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        });

        viewModel.onlineCount().observe(this, count -> {
            if (count == null || count <= 0) {
                binding.podOnlineCount.setText("Offline");
                binding.podOnlineCount.setTextColor(getColor(R.color.color_text_muted));
            } else if (count == 1) {
                binding.podOnlineCount.setText("1 member online");
                binding.podOnlineCount.setTextColor(getColor(R.color.color_hope));
            } else {
                binding.podOnlineCount.setText(count + " members online");
                binding.podOnlineCount.setTextColor(getColor(R.color.color_hope));
            }
        });

        viewModel.typingText().observe(this, text -> {
            if (text != null && !text.isEmpty()) {
                binding.podTypingIndicator.setText(text);
                binding.podTypingIndicator.setVisibility(View.VISIBLE);
            } else {
                binding.podTypingIndicator.setVisibility(View.GONE);
            }
        });

        viewModel.isPodMember().observe(this, isMember -> {
            if (!isMember) {
                binding.podStatus.setText(R.string.circle_membership_required);
                binding.podStatus.setVisibility(View.VISIBLE);
                binding.podSend.setEnabled(false);
                binding.podMessageInput.setEnabled(false);
            } else {
                binding.podStatus.setVisibility(View.GONE);
                binding.podSend.setEnabled(true);
                binding.podMessageInput.setEnabled(true);
            }
        });

        viewModel.leftPod().observe(this, left -> {
            if (Boolean.TRUE.equals(left)) {
                Toast.makeText(this, "You left the pod", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.error().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLeaveConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Pod")
                .setMessage("Are you sure you want to leave this closed pod? You will lose access to pod discussions.")
                .setPositiveButton("Leave", (dialog, which) -> viewModel.leavePod())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(PodMessage message) {
        EditText input = new EditText(this);
        input.setText(message.text);
        input.setSelection(message.text.length());
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Edit Message")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updated = input.getText().toString().trim();
                    if (!updated.isEmpty()) {
                        viewModel.editMessage(message.id, updated);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(PodMessage message) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Delete this message for everyone in the pod?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteMessage(message.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onReact(PodMessage message, String emoji) {
        viewModel.setReaction(message.id, emoji);
    }

    @Override
    public void onReport(PodMessage message) {
        EditText input = new EditText(this);
        input.setHint("Reason for report (e.g. offensive, spam)");
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_large);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Report Message")
                .setMessage("Reports are reviewed confidentially by pod moderators.")
                .setView(input)
                .setPositiveButton("Submit Report", (dialog, which) -> {
                    String reason = input.getText().toString().trim();
                    if (!reason.isEmpty()) {
                        viewModel.reportMessage(message.id, reason);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBlock(String senderUid, String pseudonym) {
        new AlertDialog.Builder(this)
                .setTitle("Block " + pseudonym)
                .setMessage("Messages from " + pseudonym + " will be hidden on your device.")
                .setPositiveButton("Block", (dialog, which) -> viewModel.blockUser(senderUid))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
