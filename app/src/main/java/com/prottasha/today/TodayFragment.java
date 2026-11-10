package com.prottasha.today;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentTodayBinding;
import com.prottasha.focus.ui.FocusActivity;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.intervention.platform.AccessibilityServiceHealth;
import com.prottasha.intervention.ui.AccessibilityDisclosureActivity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TodayFragment extends Fragment {

    private FragmentTodayBinding binding;
    private TodayViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTodayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProttashaApp application = (ProttashaApp) requireActivity().getApplication();
        viewModel = new ViewModelProvider(this, new TodayViewModel.Factory(
                application.appGraph())).get(TodayViewModel.class);

        binding.todayFocusButton.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), FocusActivity.class)));

        View.OnClickListener openDisclosure = ignored ->
                startActivity(new Intent(requireContext(), AccessibilityDisclosureActivity.class));
        binding.todayHealthBanner.setOnClickListener(openDisclosure);
        binding.todayHealthAction.setOnClickListener(openDisclosure);

        viewModel.serviceHealth().observe(getViewLifecycleOwner(), this::bindHealth);
        viewModel.activeIdentity().observe(getViewLifecycleOwner(), this::bindIdentity);
        viewModel.latestSuccessMemory().observe(getViewLifecycleOwner(), memory -> {
            if (memory != null && memory.text != null && !memory.text.trim().isEmpty()) {
                binding.todaySuccessMemory.setText(
                        getString(R.string.onboarding_success_memory_hint) + ": " + memory.text);
                binding.todaySuccessMemory.setVisibility(View.VISIBLE);
            } else {
                binding.todaySuccessMemory.setVisibility(View.GONE);
            }
        });
        viewModel.hopeHorizon().observe(getViewLifecycleOwner(), this::bindHopeHorizon);
        viewModel.dueHabits().observe(getViewLifecycleOwner(), this::bindHabits);
        viewModel.activeRules().observe(getViewLifecycleOwner(), this::bindBoundaries);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    private void bindHealth(AccessibilityServiceHealth.State state) {
        if (state == null || state == AccessibilityServiceHealth.State.HEALTHY) {
            binding.todayHealthBanner.setVisibility(View.GONE);
            return;
        }
        binding.todayHealthBanner.setVisibility(View.VISIBLE);
        if (state == AccessibilityServiceHealth.State.REVOKED) {
            binding.todayHealthTitle.setText(R.string.today_health_title);
            binding.todayHealthBody.setText(R.string.today_health_body);
        } else if (state == AccessibilityServiceHealth.State.STALE) {
            binding.todayHealthTitle.setText(R.string.today_health_title);
            binding.todayHealthBody.setText(R.string.today_health_body);
        } else if (state == AccessibilityServiceHealth.State.BATTERY_RISK) {
            binding.todayHealthTitle.setText("Battery Optimization Alert");
            binding.todayHealthBody.setText("Battery restrictions may pause intervention service.");
        }
    }

    private void bindIdentity(IdentityEntity identity) {
        boolean hasIdentity = identity != null;
        binding.todayIdentityCard.setVisibility(hasIdentity ? View.VISIBLE : View.GONE);
        if (hasIdentity) {
            binding.todayIdentityStatement.setText(identity.statement);
            if (identity.purpose != null && !identity.purpose.trim().isEmpty()) {
                binding.todayIdentityPurpose.setText(identity.purpose);
                binding.todayIdentityPurpose.setVisibility(View.VISIBLE);
            } else {
                binding.todayIdentityPurpose.setVisibility(View.GONE);
            }
            binding.todayEmptyTitle.setVisibility(View.GONE);
            binding.todayEmptyBody.setVisibility(View.GONE);
        } else {
            binding.todayEmptyTitle.setVisibility(View.VISIBLE);
            binding.todayEmptyBody.setVisibility(View.VISIBLE);
        }
    }

    private void bindHopeHorizon(HopeHorizonState state) {
        if (state == null || !state.hasData()) {
            binding.todayHopeHorizonEmpty.setVisibility(View.VISIBLE);
            binding.todayHopeHorizonContent.setVisibility(View.GONE);
            return;
        }
        binding.todayHopeHorizonEmpty.setVisibility(View.GONE);
        binding.todayHopeHorizonContent.setVisibility(View.VISIBLE);
        binding.todayHopeHorizonSummary.setText(
                state.mindfulExitPercentage() + "% mindful exits");
        binding.todayHopeHorizonDetails.setText(
                state.mindfulExits + " mindful exits · " + state.intentionalContinues
                        + " intentional continues (" + state.totalPrompts + " total prompts)");
    }

    private void bindHabits(List<HabitModels.HabitTodayItem> items) {
        binding.todayHabitsContainer.removeAllViews();
        if (items == null || items.isEmpty()) {
            binding.todayHabitsEmpty.setVisibility(View.VISIBLE);
            return;
        }
        binding.todayHabitsEmpty.setVisibility(View.GONE);
        binding.todayEmptyTitle.setVisibility(View.GONE);
        binding.todayEmptyBody.setVisibility(View.GONE);

        long epochDay = LocalDate.now().toEpochDay();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (HabitModels.HabitTodayItem item : items) {
            View itemView = inflater.inflate(android.R.layout.simple_list_item_2,
                    binding.todayHabitsContainer, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            String status = item.completed ? "Completed" : (item.lapsed ? "Lapsed" : "Due");
            text1.setText(item.habit.title + " (" + status + ")");

            StringBuilder subtitle = new StringBuilder();
            subtitle.append("Target: ").append(item.habit.target).append(" · Current: ").append(item.value);
            if (item.lapsed) {
                subtitle.append("\n").append(getString(R.string.today_rebound_prompt));
            }
            text2.setText(subtitle.toString());

            MaterialButton actionButton = new MaterialButton(requireContext());
            actionButton.setMinHeight(getResources().getDimensionPixelSize(R.dimen.touch_target_min));

            if (item.habit.type == HabitModels.Type.BUILD) {
                actionButton.setText(item.completed ? "Done" : "Mark Done");
                actionButton.setEnabled(!item.completed);
                actionButton.setOnClickListener(v -> {
                    actionButton.setEnabled(false);
                    viewModel.recordHabitAction(
                            HabitAction.complete(UUID.randomUUID().toString()).forHabit(item.habit.id, epochDay),
                            result -> {});
                });
            } else if (item.habit.type == HabitModels.Type.AMOUNT) {
                actionButton.setText("+1");
                actionButton.setOnClickListener(v -> {
                    viewModel.recordHabitAction(
                            HabitAction.increment(UUID.randomUUID().toString(), 1).forHabit(item.habit.id, epochDay),
                            result -> {});
                });
            } else {
                actionButton.setText("Check In");
                actionButton.setOnClickListener(v -> {
                    viewModel.recordHabitAction(
                            HabitAction.complete(UUID.randomUUID().toString()).forHabit(item.habit.id, epochDay),
                            result -> {});
                });
            }

            LinearLayout itemWrapper = new LinearLayout(requireContext());
            itemWrapper.setOrientation(LinearLayout.VERTICAL);
            itemWrapper.addView(itemView);
            itemWrapper.addView(actionButton);
            binding.todayHabitsContainer.addView(itemWrapper);
        }
    }

    private void bindBoundaries(List<GuardRuleEntity> rules) {
        if (rules == null || rules.isEmpty()) {
            binding.todayBoundariesEmpty.setVisibility(View.VISIBLE);
            binding.todayBoundariesCount.setVisibility(View.GONE);
            binding.todayBoundariesList.setVisibility(View.GONE);
            return;
        }
        binding.todayBoundariesEmpty.setVisibility(View.GONE);
        binding.todayBoundariesCount.setVisibility(View.VISIBLE);
        binding.todayBoundariesList.setVisibility(View.VISIBLE);

        binding.todayBoundariesCount.setText(rules.size() + " app boundaries guarded");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rules.size(); i++) {
            GuardRuleEntity rule = rules.get(i);
            sb.append("• ").append(rule.target).append(" (").append(rule.mode).append(")");
            if (i < rules.size() - 1) sb.append("\n");
        }
        binding.todayBoundariesList.setText(sb.toString());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
