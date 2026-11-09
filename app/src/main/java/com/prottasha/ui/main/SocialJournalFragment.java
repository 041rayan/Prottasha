package com.prottasha.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prottasha.data.local.entity.JournalEntryEntity;
import com.prottasha.data.repository.JournalRepository;
import com.prottasha.databinding.FragmentSocialJournalBinding;
import com.prottasha.ui.pod.PodChatActivity;

public class SocialJournalFragment extends Fragment {

    private static final String KEY_DRAFT_WINS = "KEY_DRAFT_WINS";
    private static final String KEY_DRAFT_CHAIN = "KEY_DRAFT_CHAIN";

    private FragmentSocialJournalBinding binding;
    private JournalRepository journalRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSocialJournalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        journalRepository = new JournalRepository(requireActivity().getApplication());

        // Restore state if rotated (Auto-rotation state preserved requirement)
        if (savedInstanceState != null) {
            String draftWins = savedInstanceState.getString(KEY_DRAFT_WINS, "");
            String draftChain = savedInstanceState.getString(KEY_DRAFT_CHAIN, "");
            binding.etWins.setText(draftWins);
            binding.etChainReview.setText(draftChain);
        }

        binding.btnOpenPodChat.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PodChatActivity.class);
            startActivity(intent);
        });

        binding.btnSaveJournalEntry.setOnClickListener(v -> saveJournalEntry());
    }

    private void saveJournalEntry() {
        String wins = binding.etWins.getText() != null ? binding.etWins.getText().toString().trim() : "";
        String chain = binding.etChainReview.getText() != null ? binding.etChainReview.getText().toString().trim() : "";

        if (wins.isEmpty() && chain.isEmpty()) {
            Toast.makeText(requireContext(), "Please write a win or reflection to log.", Toast.LENGTH_SHORT).show();
            return;
        }

        long epochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24);
        JournalEntryEntity entry = new JournalEntryEntity(
                epochDay,
                9, // alignment score
                5, // positive votes
                0, // negative votes
                wins,
                chain,
                false
        );

        journalRepository.insert(entry);
        binding.etWins.setText("");
        binding.etChainReview.setText("");
        Toast.makeText(requireContext(), "Journal entry saved! Added to Success Memory Bank (PC-09).", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            if (binding.etWins.getText() != null) {
                outState.putString(KEY_DRAFT_WINS, binding.etWins.getText().toString());
            }
            if (binding.etChainReview.getText() != null) {
                outState.putString(KEY_DRAFT_CHAIN, binding.etChainReview.getText().toString());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
