package com.prottasha.onboarding;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.prottasha.R;
import com.prottasha.databinding.FragmentOnboardingPermissionsBinding;
import com.prottasha.service.ProttashaAccessibilityService;

public final class PermissionSetupFragment extends Fragment {

    private FragmentOnboardingPermissionsBinding binding;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOnboardingPermissionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        OnboardingViewModel viewModel = ((OnboardingActivity) requireActivity())
                .onboardingViewModel();

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> updatePermissionStatuses()
        );

        binding.onboardingPermissionAccessibilityButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception ignored) {}
        });

        binding.onboardingPermissionUsageButton.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } catch (Exception ignored) {}
        });

        binding.onboardingPermissionNotificationsButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        });

        binding.onboardingPermissionsBackButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.onboardingPermissionsContinueButton.setOnClickListener(ignored ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_permissionSetupFragment_to_interventionPreviewFragment));

        binding.onboardingPermissionsSkipButton.setOnClickListener(ignored ->
                viewModel.complete(true));
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissionStatuses();
    }

    private void updatePermissionStatuses() {
        if (binding == null) return;
        Context context = requireContext();

        boolean accessibilityEnabled = isAccessibilityServiceEnabled(context);
        binding.onboardingPermissionAccessibilityButton.setText(accessibilityEnabled
                ? R.string.onboarding_granted : R.string.onboarding_grant);
        binding.onboardingPermissionAccessibilityButton.setEnabled(!accessibilityEnabled);

        boolean usageEnabled = isUsageAccessGranted(context);
        binding.onboardingPermissionUsageButton.setText(usageEnabled
                ? R.string.onboarding_granted : R.string.onboarding_grant);
        binding.onboardingPermissionUsageButton.setEnabled(!usageEnabled);

        boolean notificationsEnabled = isNotificationPermissionGranted(context);
        binding.onboardingPermissionNotificationsButton.setText(notificationsEnabled
                ? R.string.onboarding_granted : R.string.onboarding_grant);
        binding.onboardingPermissionNotificationsButton.setEnabled(!notificationsEnabled);
    }

    private boolean isAccessibilityServiceEnabled(Context context) {
        String enabled = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String component = new ComponentName(context, ProttashaAccessibilityService.class)
                .flattenToString();
        return enabled != null && enabled.contains(component);
    }

    @SuppressWarnings("deprecation")
    private boolean isUsageAccessGranted(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mode = appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName());
        } else {
            mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.getPackageName());
        }
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
