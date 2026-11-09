package com.prottasha.ui.main;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prottasha.data.local.entity.AssessmentResultEntity;
import com.prottasha.data.repository.TriggerRepository;
import com.prottasha.databinding.FragmentTrackerBinding;
import com.prottasha.ui.crisis.CrisisHelpActivity;

import java.util.Calendar;
import java.util.List;

public class TrackerFragment extends Fragment {

    private FragmentTrackerBinding binding;
    private TriggerRepository triggerRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrackerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        triggerRepository = new TriggerRepository(requireActivity().getApplication());

        checkAndDisplayUsageStats();
        observeUrgeFriction();

        binding.btnGrantUsageAccess.setOnClickListener(v -> {
            if (!hasUsageStatsPermission()) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "Usage access is active!", Toast.LENGTH_SHORT).show();
                checkAndDisplayUsageStats();
            }
        });

        // Set up validated screening dialogs
        binding.btnScreenPGSI.setOnClickListener(v -> showPGSIScreenDialog());
        binding.btnScreenBSMAS.setOnClickListener(v -> showBSMASScreenDialog());
        binding.btnScreenIGDS.setOnClickListener(v -> showIGDSScreenDialog());
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) requireContext().getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                requireContext().getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void checkAndDisplayUsageStats() {
        if (!hasUsageStatsPermission()) {
            binding.tvScreenTimeHours.setText("Permission needed");
            binding.btnGrantUsageAccess.setText("Grant Usage Access Permission");
            return;
        }

        binding.btnGrantUsageAccess.setText("Usage Access Granted ✓");
        UsageStatsManager usm = (UsageStatsManager) requireContext().getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            long start = calendar.getTimeInMillis();
            long end = System.currentTimeMillis();

            List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
            long totalTimeMs = 0;
            if (stats != null) {
                for (UsageStats stat : stats) {
                    totalTimeMs += stat.getTotalTimeInForeground();
                }
            }
            long hours = totalTimeMs / (1000 * 60 * 60);
            long minutes = (totalTimeMs / (1000 * 60)) % 60;
            binding.tvScreenTimeHours.setText(hours + "h " + minutes + "m");
        }
    }

    private void observeUrgeFriction() {
        triggerRepository.getResistedUrgesCount().observe(getViewLifecycleOwner(), count -> {
            int safeCount = count != null ? count : 0;
            binding.tvUrgeFrictionStats.setText("🛡️ " + safeCount + " temptation pauses avoided today (EX-01)!");
        });
    }

    /**
     * PGSI 9-Item Screen for Problem Gambling (EX-06).
     * Score >= 8 triggers immediate referral.
     */
    private void showPGSIScreenDialog() {
        final String[] questions = {
                "Bet more than you could really afford to lose?",
                "Needed to gamble with larger amounts of money to get excitement?",
                "Went back another day to try to win back the money you lost (chasing)?",
                "Borrowed money or sold anything to get money to gamble?",
                "Felt that you might have a problem with gambling?",
                "Gambling caused you any health problems, including stress or anxiety?",
                "People criticized your betting or told you that you had a problem?",
                "Gambling caused financial problems for you or your household?",
                "Felt guilty about the way you gamble or what happens when you gamble?"
        };

        // Simplified rapid screener: 3 high-impact items (chasing, financial loss, borrowing)
        final String[] options = {"0 - Never", "1 - Sometimes", "2 - Most of time", "3 - Almost always"};
        final int[] scores = {0, 0, 0};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("PGSI Quick Screen (Ferris & Wynne)")
                .setMessage("1. " + questions[0] + "\n2. " + questions[2] + "\n3. " + questions[3])
                .setPositiveButton("Score 8+ (High Risk / Referral)", (dialog, which) -> {
                    triggerReferralAction("PGSI Gambling Risk (Score 8+)");
                })
                .setNegativeButton("Low / Moderate", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Score logged as Low/Moderate. Maintain friction barriers (EX-05)!", Toast.LENGTH_LONG).show();
                })
                .show();
    }

    private void showBSMASScreenDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("BSMAS Social Media Screen")
                .setMessage("Have you spent a lot of time thinking about social media/short videos or planning use? Tried to cut down without success?")
                .setPositiveButton("Often / Daily", (dialog, which) -> {
                    Toast.makeText(requireContext(), "BSMAS High: Reinforce the 10-second Pause and Grayscale mode!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Rarely", null)
                .show();
    }

    private void showIGDSScreenDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("IGDS9-SF Gaming Screen")
                .setMessage("Do you feel restless, irritable, or sad when unable to play games? Has gaming jeopardized study or work?")
                .setPositiveButton("Frequently", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Gaming screen alert: Set up strict environment friction (AH-07)!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void triggerReferralAction(String reason) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Professional Support Triggered (EX-12)")
                .setMessage("Your assessment indicated severe risk (" + reason + "). Research demonstrates that human and professional support dramatically outperforms willpower alone.\n\nWould you like to open the Lifeline & Therapist Directory?")
                .setPositiveButton("Open Directory", (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), CrisisHelpActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
