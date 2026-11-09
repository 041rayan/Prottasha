package com.prottasha.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.data.local.entity.HabitEntity;
import com.prottasha.data.local.entity.IdentityEntity;
import com.prottasha.data.remote.QuoteApiClient;
import com.prottasha.data.repository.HabitRepository;
import com.prottasha.data.repository.IdentityRepository;
import com.prottasha.databinding.DialogAddHabitBinding;
import com.prottasha.databinding.FragmentIdentityHabitsBinding;
import com.prottasha.domain.FourLawsEngine;
import com.prottasha.ui.adapter.HabitAdapter;
import com.prottasha.ui.detail.HabitDetailActivity;

public class IdentityHabitsFragment extends Fragment implements HabitAdapter.OnHabitInteractionListener {

    private FragmentIdentityHabitsBinding binding;
    private IdentityRepository identityRepository;
    private HabitRepository habitRepository;
    private HabitAdapter habitAdapter;
    private long currentIdentityId = 1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIdentityHabitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        identityRepository = new IdentityRepository(requireActivity().getApplication());
        habitRepository = new HabitRepository(requireActivity().getApplication());

        setupRecyclerView();
        observeIdentity();
        observeHabits();
        fetchDailyExternalQuote();

        binding.btnAddHabit.setOnClickListener(v -> showAddHabitDialog());
    }

    private void setupRecyclerView() {
        habitAdapter = new HabitAdapter(this);
        binding.rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHabits.setAdapter(habitAdapter);
    }

    private void observeIdentity() {
        identityRepository.getActiveIdentity().observe(getViewLifecycleOwner(), identity -> {
            if (identity != null) {
                currentIdentityId = identity.getId();
                binding.tvActiveIdentityStatement.setText(identity.getIdentityStatement());
                binding.tvCoreValues.setText("Values: " + identity.getCoreValues());
            }
        });
    }

    private void observeHabits() {
        habitRepository.getAllHabits().observe(getViewLifecycleOwner(), habits -> {
            if (habits != null) {
                habitAdapter.setHabits(habits);

                int totalFor = 0;
                int totalAgainst = 0;
                for (HabitEntity h : habits) {
                    totalFor += h.getVotesFor();
                    totalAgainst += h.getVotesAgainst();
                }
                int score = FourLawsEngine.calculateAlignmentScore(totalFor, totalAgainst);
                binding.tvIdentityScore.setText("Identity Alignment: " + score + "% majority votes (" + totalFor + " for, " + totalAgainst + " against)");
            }
        });
    }

    private void fetchDailyExternalQuote() {
        QuoteApiClient.fetchDailyQuote(new QuoteApiClient.QuoteCallback() {
            @Override
            public void onSuccess(String quote, String author) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.tvDailyQuote.setText("“" + quote + "”");
                        binding.tvQuoteAuthor.setText("— " + author);
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                // Keeps default curated quote
            }
        });
    }

    private void showAddHabitDialog() {
        DialogAddHabitBinding dialogBinding = DialogAddHabitBinding.inflate(getLayoutInflater());
        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Create Habit", (dialog, which) -> {
                    String title = dialogBinding.etHabitTitle.getText() != null ? dialogBinding.etHabitTitle.getText().toString().trim() : "";
                    String cue = dialogBinding.etHabitCue.getText() != null ? dialogBinding.etHabitCue.getText().toString().trim() : "";
                    String response = dialogBinding.etHabitResponse.getText() != null ? dialogBinding.etHabitResponse.getText().toString().trim() : "";
                    boolean isGood = dialogBinding.switchIsGood.isChecked();

                    if (!title.isEmpty()) {
                        String scaledResponse = response.isEmpty() ? FourLawsEngine.scaleToTwoMinuteVersion(title) : response;
                        HabitEntity newHabit = new HabitEntity(
                                currentIdentityId,
                                title,
                                isGood,
                                cue,
                                "Mindful intention",
                                scaledResponse,
                                "1 vote cast for ideal identity"
                        );
                        habitRepository.insert(newHabit);
                        Toast.makeText(requireContext(), "Habit created: " + title, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onHabitClick(HabitEntity habit) {
        // Explicit Intent passing habit ID
        Intent intent = new Intent(requireContext(), HabitDetailActivity.class);
        intent.putExtra("EXTRA_HABIT_ID", habit.getId());
        startActivity(intent);
    }

    @Override
    public void onHabitLongClick(HabitEntity habit) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Habit?")
                .setMessage("Delete '" + habit.getTitle() + "' and its vote history?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    habitRepository.deleteById(habit.getId());
                    Toast.makeText(requireContext(), "Deleted " + habit.getTitle(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPositiveVoteClick(HabitEntity habit) {
        habitRepository.recordPositiveVote(habit.getId());
        Toast.makeText(requireContext(), "Vote cast for " + habit.getTitle() + "! (AH-01)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMissClick(HabitEntity habit) {
        habitRepository.recordMiss(habit.getId());
        Toast.makeText(requireContext(), "Slip logged. Rebound tomorrow! (AH-18)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
