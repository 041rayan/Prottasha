package com.prottasha.today;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.IdentityRepository;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.intervention.platform.AccessibilityServiceHealth;

import org.junit.Rule;
import org.junit.Test;

public final class TodayViewModelTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void hopeHorizonCalculatesPercentagesAndTruthfulEmptyState() {
        HopeHorizonState empty = new HopeHorizonState(0, 0, 0);
        assertFalse(empty.hasData());
        assertEquals(0, empty.mindfulExitPercentage());

        HopeHorizonState populated = new HopeHorizonState(5, 4, 1);
        assertTrue(populated.hasData());
        assertEquals(80, populated.mindfulExitPercentage());
        assertEquals(4, populated.mindfulExits);
        assertEquals(1, populated.intentionalContinues);
        assertEquals(5, populated.totalPrompts);
    }

    @Test
    public void viewModelObservesIdentityAndSuccessMemory() {
        MutableLiveData<IdentityEntity> identityLive = new MutableLiveData<>();
        MutableLiveData<SuccessMemoryEntity> memoryLive = new MutableLiveData<>();

        IdentityRepository fakeIdentityRepo = new IdentityRepository(null) {
            @Override
            public LiveData<IdentityEntity> observeActive() {
                return identityLive;
            }

            @Override
            public LiveData<SuccessMemoryEntity> observeLatestSuccessMemory() {
                return memoryLive;
            }
        };

        TodayViewModel vm = new TodayViewModel(
                fakeIdentityRepo, null, null, null, null, null);

        assertNotNull(vm.activeIdentity());
        assertNotNull(vm.latestSuccessMemory());
        assertNotNull(vm.hopeHorizon());
        assertNotNull(vm.serviceHealth());
        assertEquals(AccessibilityServiceHealth.State.HEALTHY, vm.serviceHealth().getValue());

        IdentityEntity identity = new IdentityEntity(1L, "Focused", "Purpose", 100L, true);
        identityLive.setValue(identity);
        assertEquals(identity, vm.activeIdentity().getValue());

        SuccessMemoryEntity memory = new SuccessMemoryEntity(1L, 1L, "Kept my word", 100L);
        memoryLive.setValue(memory);
        assertEquals(memory, vm.latestSuccessMemory().getValue());
    }

    @Test
    public void healthCheckUpdatesServiceHealthState() {
        IdentityRepository fakeIdentityRepo = new IdentityRepository(null) {
            @Override
            public LiveData<IdentityEntity> observeActive() {
                return new MutableLiveData<>();
            }

            @Override
            public LiveData<SuccessMemoryEntity> observeLatestSuccessMemory() {
                return new MutableLiveData<>();
            }
        };

        AccessibilityServiceHealth fakeHealthChecker = new AccessibilityServiceHealth(null) {
            @Override
            public State state(long lastEventAt, boolean batteryRisk) {
                return State.REVOKED;
            }
        };

        TodayViewModel vm = new TodayViewModel(
                fakeIdentityRepo, null, null, null, fakeHealthChecker, null);

        assertEquals(AccessibilityServiceHealth.State.REVOKED, vm.serviceHealth().getValue());
    }
}
