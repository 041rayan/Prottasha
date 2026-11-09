package com.prottasha.ui.main;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.data.local.entity.TriggerEntity;
import com.prottasha.data.repository.TriggerRepository;
import com.prottasha.databinding.FragmentMindfulToolkitBinding;
import com.prottasha.service.ProttashaFocusService;
import com.prottasha.ui.adapter.TriggerAdapter;
import com.prottasha.ui.detail.TriggerDetailActivity;
import com.prottasha.util.GrayscaleManager;

public class MindfulToolkitFragment extends Fragment implements TriggerAdapter.OnTriggerInteractionListener {

    private FragmentMindfulToolkitBinding binding;
    private TriggerRepository triggerRepository;
    private TriggerAdapter triggerAdapter;
    private CountDownTimer urgeTimer;
    private ValueAnimator breathingAnimator;
    private boolean isSurfing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMindfulToolkitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        triggerRepository = new TriggerRepository(requireActivity().getApplication());

        setupTriggersRecyclerView();
        setupBreathingVisualizer();
        setupServiceAndGrayscaleSwitches();

        binding.btnStartUrgeSurf.setOnClickListener(v -> {
            if (!isSurfing) {
                startUrgeSurfing(120);
            } else {
                stopUrgeSurfing();
            }
        });

        binding.btnResetUrgeSurf.setOnClickListener(v -> stopUrgeSurfing());
        binding.btnAddTrigger.setOnClickListener(v -> showAddTriggerDialog());
    }

    private void setupTriggersRecyclerView() {
        triggerAdapter = new TriggerAdapter(this);
        binding.rvTriggers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTriggers.setAdapter(triggerAdapter);

        triggerRepository.getAllTriggers().observe(getViewLifecycleOwner(), triggers -> {
            if (triggers != null) {
                triggerAdapter.setTriggers(triggers);
            }
        });
    }

    private void setupBreathingVisualizer() {
        breathingAnimator = ValueAnimator.ofFloat(1.0f, 1.25f);
        breathingAnimator.setDuration(4000);
        breathingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breathingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            if (binding != null) {
                binding.breathingCircleContainer.setScaleX(scale);
                binding.breathingCircleContainer.setScaleY(scale);

                if (scale > 1.12f) {
                    binding.tvBreathInstruction.setText("Hold & exhale slowly…");
                } else {
                    binding.tvBreathInstruction.setText("Inhale deeply…");
                }
            }
        });
    }

    private void startUrgeSurfing(int seconds) {
        isSurfing = true;
        binding.btnStartUrgeSurf.setText("Surfing (Stop)");
        breathingAnimator.start();

        urgeTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (binding != null) {
                    binding.tvUrgeCountdown.setText((millisUntilFinished / 1000) + "s");
                }
            }

            @Override
            public void onFinish() {
                stopUrgeSurfing();
                Toast.makeText(requireContext(), "Urge successfully surfed! You let the bell ring (PC-13).", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void stopUrgeSurfing() {
        isSurfing = false;
        if (urgeTimer != null) {
            urgeTimer.cancel();
        }
        if (breathingAnimator != null) {
            breathingAnimator.cancel();
        }
        if (binding != null) {
            binding.breathingCircleContainer.setScaleX(1.0f);
            binding.breathingCircleContainer.setScaleY(1.0f);
            binding.tvUrgeCountdown.setText("120s");
            binding.tvBreathInstruction.setText("Inhale slowly…");
            binding.btnStartUrgeSurf.setText("Ride the Wave");
        }
    }

    private void setupServiceAndGrayscaleSwitches() {
        binding.switchFocusService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent serviceIntent = new Intent(requireContext(), ProttashaFocusService.class);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    requireContext().startForegroundService(serviceIntent);
                } else {
                    requireContext().startService(serviceIntent);
                }
                Toast.makeText(requireContext(), "Deep Focus Service started with tray updates!", Toast.LENGTH_SHORT).show();
            } else {
                requireContext().stopService(serviceIntent);
                Toast.makeText(requireContext(), "Deep Focus Service stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.switchGrayscale.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean success = GrayscaleManager.setGrayscaleEnabled(requireContext(), isChecked);
            if (!success && isChecked) {
                Toast.makeText(requireContext(), "Grayscale requires WRITE_SECURE_SETTINGS via ADB or Shizuku (see Settings for setup)", Toast.LENGTH_LONG).show();
                binding.switchGrayscale.setChecked(false);
            }
        });
    }

    private void showAddTriggerDialog() {
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
        final EditText etSituation = new EditText(requireContext());
        etSituation.setHint("When this cue happens (e.g. Bedtime boredom)");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("New If-Then Coping Plan")
                .setView(etSituation)
                .setPositiveButton("Save Plan", (dialog, which) -> {
                    String sit = etSituation.getText().toString().trim();
                    if (!sit.isEmpty()) {
                        String ifThen = "When " + sit + ", I will pause 10s and do a 2-minute breath reset.";
                        TriggerEntity trigger = new TriggerEntity(sit, "Dopamine relief", ifThen, 120);
                        triggerRepository.insert(trigger);
                        Toast.makeText(requireContext(), "If-Then plan saved! (AH-05)", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void OnTriggerClick(TriggerEntity trigger) {
        Intent intent = new Intent(requireContext(), TriggerDetailActivity.class);
        intent.putExtra("EXTRA_TRIGGER_ID", trigger.getId());
        startActivity(intent);
    }

    @Override
    public void OnTriggerLongClick(TriggerEntity trigger) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Trigger Plan?")
                .setMessage("Delete plan for: '" + trigger.getSituationCue() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    triggerRepository.deleteById(trigger.getId());
                    Toast.makeText(requireContext(), "Plan deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onOvercomeClick(TriggerEntity trigger) {
        triggerRepository.recordEncounter(trigger.getId(), true);
        Toast.makeText(requireContext(), "Overcame trigger! Rep recorded. (AH-24)", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopUrgeSurfing();
        binding = null;
    }
}
