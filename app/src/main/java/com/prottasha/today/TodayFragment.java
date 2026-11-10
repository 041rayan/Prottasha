package com.prottasha.today;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.databinding.FragmentTodayBinding;
import com.prottasha.databinding.ItemTodayHabitBinding;
import com.prottasha.focus.ui.FocusActivity;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;
import com.prottasha.habits.ui.HabitConsistencyGridState;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.intervention.platform.AccessibilityServiceHealth;
import com.prottasha.intervention.ui.AccessibilityDisclosureActivity;
import com.prottasha.settings.account.ProfileActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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

        bindHeader(application);

        binding.todayFocusButton.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), FocusActivity.class)));
        binding.todayAvatar.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));

        View.OnClickListener openDisclosure = ignored ->
                startActivity(new Intent(requireContext(), AccessibilityDisclosureActivity.class));
        binding.todayHealthBanner.setOnClickListener(openDisclosure);
        binding.todayHealthAction.setOnClickListener(openDisclosure);

        viewModel.serviceHealth().observe(getViewLifecycleOwner(), this::bindHealth);
        viewModel.activeIdentity().observe(getViewLifecycleOwner(), this::bindIdentity);
        viewModel.latestSuccessMemory().observe(getViewLifecycleOwner(), memory -> {
            if (memory != null && memory.text != null && !memory.text.trim().isEmpty()) {
                binding.todaySuccessMemory.setText(getString(
                        R.string.today_success_memory_format, memory.text));
                binding.todaySuccessMemory.setVisibility(View.VISIBLE);
            } else {
                binding.todaySuccessMemory.setVisibility(View.GONE);
            }
        });
        viewModel.hopeHorizon().observe(getViewLifecycleOwner(), this::bindHopeHorizon);
        viewModel.consistency().observe(getViewLifecycleOwner(), this::bindConsistency);
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
            binding.todayHealthTitle.setText(R.string.today_health_battery_title);
            binding.todayHealthBody.setText(R.string.today_health_battery_body);
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
        binding.todayHopeHorizonSummary.setText(getString(
                R.string.today_horizon_summary_format, state.mindfulExitPercentage()));
        binding.todayHopeHorizonDetails.setText(getString(
                R.string.today_horizon_details_format,
                state.mindfulExits,
                state.intentionalContinues,
                state.totalPrompts));
    }

    private void bindConsistency(HabitConsistencyGridState state) {
        if (state == null) return;
        binding.todayConsistencyHeatmap.setState(state);
        binding.todayConsistencyActiveDays.setText(String.valueOf(state.activeDays));
        binding.todayConsistencyBestRun.setText(String.valueOf(state.bestRun));
        binding.todayConsistencyPercent.setText(getString(
                R.string.today_percent_format, state.consistencyPercent));
        binding.todayConsistencyEmpty.setVisibility(state.hasData ? View.GONE : View.VISIBLE);
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
            ItemTodayHabitBinding row = ItemTodayHabitBinding.inflate(
                    inflater, binding.todayHabitsContainer, false);
            row.todayHabitTitle.setText(item.habit.title);
            row.todayHabitMeta.setText(getString(
                    R.string.today_habit_meta_format,
                    typeLabel(item.habit.type),
                    scheduleLabel(item.habit.schedule),
                    habitStatus(item)));
            row.todayHabitRebound.setVisibility(item.lapsed ? View.VISIBLE : View.GONE);

            if (item.habit.type == HabitModels.Type.BUILD) {
                row.todayHabitAction.setText(R.string.today_habit_mark_done);
                row.todayHabitAction.setEnabled(!item.completed);
                row.todayHabitAction.setOnClickListener(v -> {
                    row.todayHabitAction.setEnabled(false);
                    viewModel.recordHabitAction(
                            HabitAction.complete(UUID.randomUUID().toString()).forHabit(item.habit.id, epochDay),
                            result -> viewModel.refreshConsistency());
                });
            } else if (item.habit.type == HabitModels.Type.AMOUNT) {
                row.todayHabitAction.setText(getString(R.string.today_habit_increment, 1));
                row.todayHabitAction.setOnClickListener(v -> {
                    viewModel.recordHabitAction(
                            HabitAction.increment(UUID.randomUUID().toString(), 1).forHabit(item.habit.id, epochDay),
                            result -> viewModel.refreshConsistency());
                });
            } else {
                row.todayHabitAction.setText(R.string.today_habit_stayed_clear);
                row.todayHabitAction.setOnClickListener(v -> {
                    viewModel.recordHabitAction(
                            HabitAction.complete(UUID.randomUUID().toString()).forHabit(item.habit.id, epochDay),
                            result -> viewModel.refreshConsistency());
                });
            }
            binding.todayHabitsContainer.addView(row.getRoot());
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

        binding.todayBoundariesCount.setText(getResources().getQuantityString(
                R.plurals.today_boundary_count, rules.size(), rules.size()));
        StringBuilder sb = new StringBuilder();
        int visibleRules = Math.min(2, rules.size());
        for (int i = 0; i < visibleRules; i++) {
            GuardRuleEntity rule = rules.get(i);
            sb.append(getString(R.string.today_boundary_item, rule.target, rule.mode));
            if (i < visibleRules - 1) sb.append("\n");
        }
        if (rules.size() > visibleRules) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(getString(R.string.today_boundary_more, rules.size() - visibleRules));
        }
        binding.todayBoundariesList.setText(sb.toString());
    }

    private void bindHeader(ProttashaApp application) {
        LocalDate today = LocalDate.now();
        binding.todayDate.setText(today.format(DateTimeFormatter.ofPattern(
                "EEEE · d MMMM", Locale.getDefault())));

        SessionUser user = application.appGraph().authRepository().currentState().userOrNull();
        try {
            user = application.appGraph().userSessionManager().requireUser();
        } catch (IllegalStateException ignored) {
            // Auth state is the safe fallback while session restoration completes.
        }
        String displayName = user == null || user.displayNameOrNull() == null
                || user.displayNameOrNull().trim().isEmpty()
                ? getString(R.string.today_member_name)
                : user.displayNameOrNull().trim();
        String firstName = displayName.split("\\s+")[0];
        int greeting = LocalTime.now().getHour() < 12
                ? R.string.today_greeting_morning
                : LocalTime.now().getHour() < 18
                ? R.string.today_greeting_afternoon
                : R.string.today_greeting_evening;
        binding.todayGreeting.setText(getString(greeting, firstName));
        binding.todayAvatar.setText(initials(displayName));
        binding.todayAvatar.setContentDescription(getString(
                R.string.today_avatar_description, displayName));
    }

    private String typeLabel(HabitModels.Type type) {
        switch (type) {
            case AVOID:
                return getString(R.string.today_habit_avoid);
            case AMOUNT:
                return getString(R.string.today_habit_amount);
            default:
                return getString(R.string.today_habit_build);
        }
    }

    private String scheduleLabel(HabitSchedule schedule) {
        switch (schedule.frequency) {
            case DAILY:
                return getString(R.string.today_habit_daily);
            case WEEKDAYS:
                return getString(R.string.today_habit_weekdays);
            default:
                return getString(R.string.today_habit_scheduled);
        }
    }

    private String habitStatus(HabitModels.HabitTodayItem item) {
        if (item.lapsed) return getString(R.string.today_habit_lapsed);
        if (item.completed) return getString(R.string.today_habit_completed);
        return getString(R.string.today_habit_due);
    }

    private static String initials(String displayName) {
        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length()))
                    .toUpperCase(Locale.getDefault());
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase(Locale.getDefault());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
