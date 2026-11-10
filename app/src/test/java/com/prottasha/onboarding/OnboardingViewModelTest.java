package com.prottasha.onboarding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.SavedStateHandle;

import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class OnboardingViewModelTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void versionIsTwoAndRoutesV1UsersToTargetSelection() {
        assertEquals(2, OnboardingViewModel.CURRENT_VERSION);

        SavedStateHandle handle = new SavedStateHandle();
        OnboardingViewModel vm0 = new OnboardingViewModel(handle, null, null, null, null, 0);
        assertEquals(OnboardingViewModel.Stage.PURPOSE, vm0.initialStage());

        OnboardingViewModel vm1 = new OnboardingViewModel(handle, null, null, null, null, 1);
        assertEquals(OnboardingViewModel.Stage.TARGETS, vm1.initialStage());
    }

    @Test
    public void draftPreservesAllSevenStagesState() {
        SavedStateHandle handle = new SavedStateHandle();
        OnboardingViewModel viewModel = new OnboardingViewModel(
                handle, null, null, null, null, 0);

        viewModel.setPurpose("Build strong focus");
        viewModel.setIdentityStatement("I am a thoughtful engineer");
        viewModel.setSuccessMemory("Finished thesis on time");
        List<String> pkgs = Arrays.asList("com.example.distraction1", "com.example.distraction2");
        viewModel.setSelectedPackages(pkgs);
        viewModel.setTargetMode("PAUSE");

        OnboardingDraft draft = viewModel.draft();
        assertEquals("Build strong focus", draft.purpose);
        assertEquals("I am a thoughtful engineer", draft.identityStatement);
        assertEquals("Finished thesis on time", draft.successMemory);
        assertEquals(pkgs, draft.selectedPackages);
        assertEquals("PAUSE", draft.targetMode);
    }
}
