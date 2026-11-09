package com.prottasha.ui.pod;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prottasha.databinding.ActivityPodChatBinding;
import com.prottasha.ui.adapter.PodMessageAdapter;

public class PodChatActivity extends AppCompatActivity {

    private ActivityPodChatBinding binding;
    private PodMessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPodChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        binding.etMessage.setEnabled(false);
        binding.btnSendMessage.setEnabled(false);
    }

    private void setupRecyclerView() {
        messageAdapter = new PodMessageAdapter();
        binding.rvPodMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPodMessages.setAdapter(messageAdapter);
    }

}
