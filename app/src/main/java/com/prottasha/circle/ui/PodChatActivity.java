package com.prottasha.circle.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.data.FirebasePodDataSource;
import com.prottasha.circle.data.PodRepository;
import com.prottasha.circle.data.PresenceRepository;
import com.prottasha.circle.domain.PodMessage;
import com.prottasha.databinding.ActivityPodChatNewBinding;

import java.util.List;

public final class PodChatActivity extends AppCompatActivity {
    private ActivityPodChatNewBinding binding;
    private PodRepository repository;
    private ListenerRegistration listener;
    private PresenceRepository presence;
    private SessionUser user;

    @Override protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        binding = ActivityPodChatNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ProttashaApp app = (ProttashaApp) getApplication();
        try { user = app.appGraph().userSessionManager().requireUser(); }
        catch (IllegalStateException noUser) { finish(); return; }
        repository = new PodRepository(new FirebasePodDataSource());
        presence = new PresenceRepository();
        binding.podSend.setOnClickListener(ignored -> send());
        listener = repository.listen(new FirebasePodDataSource.MessageListener() {
            @Override public void onMessages(List<PodMessage> messages) { render(messages); }
            @Override public void onError() {
                binding.podStatus.setText(R.string.circle_membership_required);
            }
        });
    }

    @Override protected void onStart() {
        super.onStart();
        if (presence != null && user != null) {
            presence.connect(PodRepository.SHOWCASE_POD_ID, user.uid());
        }
    }

    private void send() {
        String text = binding.podMessageInput.getText().toString().trim();
        if (text.isEmpty()) return;
        FirebaseFirestore.getInstance().collection("users").document(user.uid()).get()
                .addOnSuccessListener(profile -> {
                    String pseudonym = profile.getString("podPseudonym");
                    if (pseudonym == null) return;
                    repository.send(user, pseudonym, text, result -> runOnUiThread(() -> {
                        if (result.isSuccess()) binding.podMessageInput.setText("");
                        else binding.podStatus.setText(R.string.circle_send_rejected);
                    }));
                });
    }

    private void render(List<PodMessage> messages) {
        binding.podMessages.removeAllViews();
        binding.podStatus.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        for (PodMessage message : messages) {
            TextView row = new TextView(this);
            row.setText(getString(R.string.circle_message, message.pseudonym, message.text));
            row.setPadding(12, 12, 12, 12);
            binding.podMessages.addView(row);
        }
    }

    @Override protected void onDestroy() {
        if (listener != null) listener.remove();
        if (presence != null) presence.disconnect();
        super.onDestroy();
    }
}
