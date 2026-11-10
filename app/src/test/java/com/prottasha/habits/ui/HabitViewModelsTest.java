package com.prottasha.habits.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.data.HabitSubstepEntity;
import com.prottasha.habits.data.HabitVacationEntity;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;

import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class HabitViewModelsTest {

    @Rule
    public final InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void habitEditViewModelTracksSubstepsAndDirtyFlag() {
        HabitRepository fakeRepo = new HabitRepository(null, Runnable::run);
        HabitEditViewModel vm = new HabitEditViewModel(fakeRepo, 0L);

        assertFalse(Boolean.TRUE.equals(vm.isDirty().getValue()));
        assertEquals(0, vm.substeps().getValue().size());

        vm.addSubstep("Step 1");
        vm.addSubstep("Step 2");
        assertEquals(2, vm.substeps().getValue().size());
        assertEquals("Step 1", vm.substeps().getValue().get(0));
        assertEquals("Step 2", vm.substeps().getValue().get(1));
        assertTrue(Boolean.TRUE.equals(vm.isDirty().getValue()));

        vm.removeSubstep(0);
        assertEquals(1, vm.substeps().getValue().size());
        assertEquals("Step 2", vm.substeps().getValue().get(0));
    }

    @Test
    public void habitEditViewModelSavesSnapshotAndSubsteps() {
        AtomicReference<HabitModels.HabitSnapshot> savedSnapshot = new AtomicReference<>();
        List<String> savedSubsteps = new ArrayList<>();

        HabitRepository fakeRepo = new HabitRepository(null, Runnable::run) {
            @Override
            public void save(HabitModels.HabitSnapshot snapshot, ResultCallback<Long> callback) {
                savedSnapshot.set(snapshot);
                callback.onResult(AppResult.success(101L));
            }

            @Override
            public void saveSubstep(long habitId, String title, int sortOrder, ResultCallback<Long> callback) {
                savedSubsteps.add(title);
                callback.onResult(AppResult.success((long) sortOrder));
            }
        };

        HabitEditViewModel vm = new HabitEditViewModel(fakeRepo, 0L);
        vm.addSubstep("Prepare running shoes");

        vm.save(
                "Morning Run",
                HabitModels.Type.BUILD,
                1,
                HabitSchedule.daily(LocalDate.now().toEpochDay()),
                "Running shoes near door",
                "Put on shoes",
                "Coffee",
                "If raining, run on treadmill",
                "Restart next morning",
                "",
                1,
                1,
                20,
                "Pod member",
                result -> assertTrue(result.isSuccess())
        );

        assertTrue(Boolean.TRUE.equals(vm.saved().getValue()));
        assertEquals("Morning Run", savedSnapshot.get().title);
        assertEquals("Running shoes near door", savedSnapshot.get().cue);
        assertEquals(1, savedSubsteps.size());
        assertEquals("Prepare running shoes", savedSubsteps.get(0));
    }

    @Test
    public void habitsViewModelFiltersAndDispatchesCorrectActions() {
        AtomicReference<HabitAction> recordedAction = new AtomicReference<>();
        HabitRepository fakeRepo = new HabitRepository(null, Runnable::run) {
            @Override
            public void record(HabitAction action, ResultCallback<HabitModels.CompletionResult> callback) {
                recordedAction.set(action);
                callback.onResult(AppResult.success(new HabitModels.CompletionResult(1, true, false)));
            }
        };

        HabitsViewModel vm = new HabitsViewModel(fakeRepo);
        assertEquals(HabitsViewModel.Filter.ALL, vm.filter().getValue());

        vm.setFilter(HabitsViewModel.Filter.BUILD);
        assertEquals(HabitsViewModel.Filter.BUILD, vm.filter().getValue());

        HabitModels.HabitSnapshot buildHabit = new HabitModels.HabitSnapshot(
                1L, "Meditation", HabitModels.Type.BUILD, 1, HabitSchedule.daily(0L),
                "", "", "", "", "", "", 1, 1, 10, "", false, 0);
        vm.act(new HabitModels.HabitTodayItem(buildHabit, 0, false, false));
        assertEquals(HabitAction.Kind.COMPLETE, recordedAction.get().kind);

        HabitModels.HabitSnapshot avoidHabit = new HabitModels.HabitSnapshot(
                2L, "Sugar", HabitModels.Type.AVOID, 1, HabitSchedule.daily(0L),
                "", "", "", "", "", "", 1, 1, 0, "", false, 0);
        vm.act(new HabitModels.HabitTodayItem(avoidHabit, 0, false, false));
        assertEquals(HabitAction.Kind.LAPSE, recordedAction.get().kind);

        HabitModels.HabitSnapshot amountHabit = new HabitModels.HabitSnapshot(
                3L, "Water", HabitModels.Type.AMOUNT, 8, HabitSchedule.daily(0L),
                "", "", "", "", "", "glasses", 2, 8, 0, "", false, 0);
        vm.act(new HabitModels.HabitTodayItem(amountHabit, 0, false, false));
        assertEquals(HabitAction.Kind.INCREMENT, recordedAction.get().kind);
        assertEquals(2, recordedAction.get().amount);
    }

    @Test
    public void habitsViewModelHandlesReorderingAndUndo() {
        AtomicLong undoEventId = new AtomicLong();
        List<Long> reorderedIds = new ArrayList<>();

        HabitRepository fakeRepo = new HabitRepository(null, Runnable::run) {
            @Override
            public void undo(long habitId, long eventId, ResultCallback<HabitModels.CompletionResult> callback) {
                undoEventId.set(eventId);
                callback.onResult(AppResult.success(new HabitModels.CompletionResult(0, false, false)));
            }

            @Override
            public void reorder(long habitId, int newSortOrder) {
                reorderedIds.add(habitId);
            }
        };

        HabitsViewModel vm = new HabitsViewModel(fakeRepo);
        vm.undo(10L, 555L, r -> assertTrue(r.isSuccess()));
        assertEquals(555L, undoEventId.get());

        HabitModels.HabitSnapshot h1 = new HabitModels.HabitSnapshot(
                101L, "One", HabitModels.Type.BUILD, 1, HabitSchedule.daily(0),
                "", "", "", "", "", "", 1, 1, 0, "", false, 0);
        HabitModels.HabitSnapshot h2 = new HabitModels.HabitSnapshot(
                102L, "Two", HabitModels.Type.BUILD, 1, HabitSchedule.daily(0),
                "", "", "", "", "", "", 1, 1, 0, "", false, 1);
        List<HabitModels.HabitSnapshot> list = Arrays.asList(h1, h2);

        vm.moveDown(h1, list);
        assertEquals(Arrays.asList(101L, 102L), reorderedIds);

        reorderedIds.clear();
        vm.moveUp(h2, list);
        assertEquals(Arrays.asList(102L, 101L), reorderedIds);
    }

    @Test
    public void habitDetailViewModelManagesVacationsBackfillAndLifecycle() {
        AtomicBoolean vacationAdded = new AtomicBoolean();
        AtomicBoolean vacationRemoved = new AtomicBoolean();
        AtomicBoolean habitArchived = new AtomicBoolean();
        AtomicBoolean habitRestored = new AtomicBoolean();
        AtomicBoolean habitDeleted = new AtomicBoolean();
        AtomicLong backfillDay = new AtomicLong();

        HabitRepository fakeRepo = new HabitRepository(null, Runnable::run) {
            @Override
            public void addVacation(long habitId, long startDay, long endDay, ResultCallback<Boolean> callback) {
                vacationAdded.set(true);
                callback.onResult(AppResult.success(true));
            }

            @Override
            public void removeVacation(long habitId, long startDay) {
                vacationRemoved.set(true);
            }

            @Override
            public void archive(long habitId) {
                habitArchived.set(true);
            }

            @Override
            public void restore(long habitId) {
                habitRestored.set(true);
            }

            @Override
            public void delete(long habitId) {
                habitDeleted.set(true);
            }

            @Override
            public void backfill(long habitId, long epochDay, HabitAction.Kind kind, int amount,
                                 ResultCallback<HabitModels.CompletionResult> callback) {
                backfillDay.set(epochDay);
                callback.onResult(AppResult.success(new HabitModels.CompletionResult(amount, true, false)));
            }
        };

        HabitDetailViewModel vm = new HabitDetailViewModel(fakeRepo, 42L);

        vm.addVacation(100L, 105L);
        assertTrue(vacationAdded.get());

        vm.removeVacation(100L);
        assertTrue(vacationRemoved.get());

        vm.archive();
        assertTrue(habitArchived.get());

        vm.restore();
        assertTrue(habitRestored.get());

        vm.delete(r -> assertTrue(r.isSuccess()));
        assertTrue(habitDeleted.get());

        vm.backfill(99L, HabitAction.Kind.COMPLETE, 1, r -> assertTrue(r.isSuccess()));
        assertEquals(99L, backfillDay.get());
    }
}
