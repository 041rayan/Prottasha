package com.prottasha.insights.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentInsightsBinding;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.insights.data.InsightsRepository;
import com.prottasha.insights.data.JournalEntryEntity;
import com.prottasha.insights.domain.InsightsCalculator;
import com.prottasha.methodology.MethodologyActivity;
import com.prottasha.triggers.data.TriggerPlanEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public final class InsightsFragment extends Fragment {
    private FragmentInsightsBinding binding;
    private InsightsCalculator.Range selectedRange = InsightsCalculator.Range.LAST_30_DAYS;

    private TargetBreakdownAdapter targetAdapter;
    private ReflectionsAdapter reflectionsAdapter;
    private TriggerPlansAdapter triggersAdapter;
    private SuccessMemoriesAdapter memoriesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        setupRecyclerViews();
        setupRangeSelectors();
        setupButtons();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void setupRecyclerViews() {
        targetAdapter = new TargetBreakdownAdapter();
        binding.insightsTargetsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.insightsTargetsRecycler.setAdapter(targetAdapter);

        reflectionsAdapter = new ReflectionsAdapter(entry -> {
            Intent intent = new Intent(requireContext(), ReflectionActivity.class);
            intent.putExtra(ReflectionActivity.EXTRA_REFLECTION_ID, entry.id);
            startActivity(intent);
        });
        binding.insightsReflectionsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.insightsReflectionsRecycler.setAdapter(reflectionsAdapter);

        triggersAdapter = new TriggerPlansAdapter(plan -> {
            ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
            app.appGraph().executors().disk().execute(() -> {
                app.appGraph().triggerRepository().delete(plan.id);
                requireActivity().runOnUiThread(this::loadData);
            });
        });
        binding.insightsTriggersRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.insightsTriggersRecycler.setAdapter(triggersAdapter);

        memoriesAdapter = new SuccessMemoriesAdapter(memory -> {
            ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
            app.appGraph().executors().disk().execute(() -> {
                app.appGraph().identityRepository().deleteSuccessMemory(memory.id);
                requireActivity().runOnUiThread(this::loadData);
            });
        });
        binding.insightsMemoriesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.insightsMemoriesRecycler.setAdapter(memoriesAdapter);
    }

    private void setupRangeSelectors() {
        binding.insightsRangeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_today)) {
                selectedRange = InsightsCalculator.Range.TODAY;
            } else if (checkedIds.contains(R.id.chip_week)) {
                selectedRange = InsightsCalculator.Range.LAST_7_DAYS;
            } else {
                selectedRange = InsightsCalculator.Range.LAST_30_DAYS;
            }
            loadData();
        });
    }

    private void setupButtons() {
        binding.insightsReflect.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), ReflectionActivity.class)));

        binding.insightsMethodology.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), MethodologyActivity.class)));

        binding.insightsRehearsal.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), RehearsalActivity.class)));

        binding.insightsAddTrigger.setOnClickListener(ignored -> showAddTriggerDialog());

        binding.insightsAddMemory.setOnClickListener(ignored -> showAddMemoryDialog());
    }

    private void showAddTriggerDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        layout.setPadding(padding, padding, padding, padding);

        final EditText cueInput = new EditText(requireContext());
        cueInput.setHint("When (e.g., Feeling overwhelmed at work)");
        layout.addView(cueInput);

        final EditText planInput = new EditText(requireContext());
        planInput.setHint("Then (e.g., Take 3 deep breaths and write in journal)");
        layout.addView(planInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Trigger Plan")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String cue = cueInput.getText().toString().trim();
                    String plan = planInput.getText().toString().trim();
                    if (!cue.isEmpty() && !plan.isEmpty()) {
                        ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
                        app.appGraph().executors().disk().execute(() -> {
                            app.appGraph().triggerRepository().save(cue, plan);
                            requireActivity().runOnUiThread(this::loadData);
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddMemoryDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = getResources().getDimensionPixelSize(R.dimen.spacing_medium);
        layout.setPadding(padding, padding, padding, padding);

        final EditText memoryInput = new EditText(requireContext());
        memoryInput.setHint("Success memory (e.g., Paused and read book instead of doomscrolling)");
        layout.addView(memoryInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add Success Memory")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String text = memoryInput.getText().toString().trim();
                    if (!text.isEmpty()) {
                        ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
                        app.appGraph().executors().disk().execute(() -> {
                            app.appGraph().identityRepository().saveSuccessMemory(text);
                            requireActivity().runOnUiThread(this::loadData);
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadData() {
        if (!isAdded()) return;
        ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
        long epochDay = LocalDate.now().toEpochDay();
        ZoneId zoneId = ZoneId.systemDefault();

        app.appGraph().executors().disk().execute(() -> {
            InsightsCalculator.Snapshot snapshot = app.appGraph().insightsRepository()
                    .snapshot(selectedRange, epochDay, zoneId);
            List<JournalEntryEntity> reflections = app.appGraph().insightsRepository().listReflections(10);
            List<TriggerPlanEntity> triggers = app.appGraph().triggerRepository().list();
            List<SuccessMemoryEntity> memories = app.appGraph().identityRepository().listSuccessMemories();
            int rehearsals = app.appGraph().insightsRepository().rehearsalCount();

            if (isAdded()) {
                requireActivity().runOnUiThread(() ->
                        render(snapshot, reflections, triggers, memories, rehearsals));
            }
        });
    }

    private void render(
            InsightsCalculator.Snapshot snapshot,
            List<JournalEntryEntity> reflections,
            List<TriggerPlanEntity> triggers,
            List<SuccessMemoryEntity> memories,
            int rehearsals) {
        if (binding == null) return;

        // Summary and Trend
        if (snapshot.decisions > 0) {
            binding.insightsSummary.setText(getString(
                    R.string.insights_rate, Math.round(snapshot.followThroughRate * 100), snapshot.decisions));
        } else {
            binding.insightsSummary.setText(getString(R.string.insights_no_data));
        }

        binding.insightsTrend.setData(
                snapshot.followThroughRate, snapshot.decisions,
                snapshot.stepBackCount, snapshot.rapidReopenCount, snapshot.continuedCount);

        binding.insightsSample.setText(snapshot.adequateSample
                ? R.string.insights_sample_adequate : R.string.insights_sample_limited);

        // Detailed Text Breakdown
        binding.insightsTextBreakdown.setText(String.format(Locale.ROOT,
                "• Follow-Through: %d\n• Step Backs: %d\n• Continued: %d\n• Rapid Reopens: %d\n• Habits Completed: %d\n• Focus Minutes: %d",
                snapshot.followThroughCount, snapshot.stepBackCount, snapshot.continuedCount,
                snapshot.rapidReopenCount, snapshot.habitsCompleted, snapshot.focusMinutes));

        // Usage Overview
        long minutes = snapshot.totalUsageMillis / 60_000L;
        if (snapshot.totalUsageMillis > 0 || snapshot.totalOpens > 0) {
            String durationStr = minutes >= 60
                    ? String.format(Locale.ROOT, "%dh %dm", minutes / 60, minutes % 60)
                    : String.format(Locale.ROOT, "%dm", minutes);
            binding.insightsUsageOverview.setText(String.format(Locale.ROOT,
                    "%s monitored usage across %d opens · %d sessions (%s)",
                    durationStr, snapshot.totalOpens, snapshot.totalSessions, snapshot.usageQuality.name()));
        } else {
            binding.insightsUsageOverview.setText("No monitored usage recorded in this period.");
        }

        // Observed Patterns
        if (snapshot.adequateSample && snapshot.patternSummary != null && snapshot.patternSummary.adequateSample) {
            String windowName = snapshot.patternSummary.peakWindow != null
                    ? snapshot.patternSummary.peakWindow.name() : "N/A";
            String topTarget = snapshot.patternSummary.topTarget != null
                    ? snapshot.patternSummary.topTarget : "None";
            binding.insightsPatternText.setText(String.format(Locale.ROOT,
                    "• Peak Intervention Window: %s\n• Most Challenged Target: %s",
                    windowName, topTarget));
        } else {
            binding.insightsPatternText.setText("More data needed (minimum 7 decisions) to identify patterns.");
        }

        // Target Drill-Down
        if (snapshot.targetBreakdowns.isEmpty()) {
            binding.insightsTargetsEmpty.setVisibility(View.VISIBLE);
            binding.insightsTargetsRecycler.setVisibility(View.GONE);
        } else {
            binding.insightsTargetsEmpty.setVisibility(View.GONE);
            binding.insightsTargetsRecycler.setVisibility(View.VISIBLE);
            targetAdapter.setItems(snapshot.targetBreakdowns);
        }

        // Reflections
        if (reflections.isEmpty()) {
            binding.insightsReflectionsEmpty.setVisibility(View.VISIBLE);
            binding.insightsReflectionsRecycler.setVisibility(View.GONE);
        } else {
            binding.insightsReflectionsEmpty.setVisibility(View.GONE);
            binding.insightsReflectionsRecycler.setVisibility(View.VISIBLE);
            reflectionsAdapter.setItems(reflections);
        }

        // Triggers
        if (triggers.isEmpty()) {
            binding.insightsTriggersEmpty.setVisibility(View.VISIBLE);
            binding.insightsTriggersRecycler.setVisibility(View.GONE);
        } else {
            binding.insightsTriggersEmpty.setVisibility(View.GONE);
            binding.insightsTriggersRecycler.setVisibility(View.VISIBLE);
            triggersAdapter.setItems(triggers);
        }

        // Memories
        if (memories.isEmpty()) {
            binding.insightsMemoriesEmpty.setVisibility(View.VISIBLE);
            binding.insightsMemoriesRecycler.setVisibility(View.GONE);
        } else {
            binding.insightsMemoriesEmpty.setVisibility(View.GONE);
            binding.insightsMemoriesRecycler.setVisibility(View.VISIBLE);
            memoriesAdapter.setItems(memories);
        }

        // Rehearsal Status
        binding.insightsRehearsalStatus.setText(String.format(Locale.ROOT,
                "%d mental rehearsal sessions completed.", rehearsals));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
