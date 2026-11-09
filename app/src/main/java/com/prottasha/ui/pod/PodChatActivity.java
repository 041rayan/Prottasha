package com.prottasha.ui.pod;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prottasha.data.firebase.FirebaseAuthManager;
import com.prottasha.data.firebase.FirebasePodManager;
import com.prottasha.databinding.ActivityPodChatBinding;
import com.prottasha.ui.adapter.PodMessageAdapter;

public class PodChatActivity extends AppCompatActivity {

    private ActivityPodChatBinding binding;
    private FirebaseAuthManager authManager;
    private FirebasePodManager podManager;
    private PodMessageAdapter messageAdapter;
    private String userPseudonym = "Warrior";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPodChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new FirebaseAuthManager();
        podManager = new FirebasePodManager();

        setupRecyclerView();
        signInAndListen();

        binding.btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void setupRecyclerView() {
        messageAdapter = new PodMessageAdapter();
        binding.rvPodMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPodMessages.setAdapter(messageAdapter);
    }

    private void signInAndListen() {
        // Firebase Authentication (Anonymous)
        authManager.signInAnonymously(new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String userId) {
                userPseudonym = "Seeker-" + (Math.abs(userId.hashCode()) % 100);
                listenToMessages();
            }

            @Override
            public void onFailure(String error) {
                userPseudonym = "Seeker-Local";
                listenToMessages();
            }
        });
    }

    private void listenToMessages() {
        podManager.listenForMessages(messages -> {
            runOnUiThread(() -> {
                messageAdapter.setMessages(messages);
                if (messages.size() > 0) {
                    binding.rvPodMessages.smoothScrollToPosition(messages.size() - 1);
                }
            });
        });
    }

    private void sendMessage() {
        String text = binding.etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        podManager.sendMessage(userPseudonym, text);
        binding.etMessage.setText("");
        Toast.makeText(this, "Encouragement shared with pod! (AH-10)", Toast.LENGTH_SHORT).show();
    }
}
