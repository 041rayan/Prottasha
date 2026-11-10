package com.prottasha.habits.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.core.result.ResultCallback;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HabitsViewModel extends ViewModel {

    public enum Filter { ALL, BUILD, AVOID, AMOUNT, ARCHIVED }

    private final HabitRepository repository;
    private final long today = LocalDate.now().toEpochDay();
    private final MutableLiveData<Filter> filter = new MutableLiveData<>(Filter.ALL);
    private final LiveData<List<HabitModels.HabitTodayItem>> items;
    private final LiveData<List<HabitModels.HabitSnapshot>> activeHabits;
    private final LiveData<List<HabitModels.HabitSnapshot>> archivedHabits;

    public HabitsViewModel(HabitRepository repository) {
        this.repository = repository;
        items = repository.observeDueOn(today);
        activeHabits = repository.observeActive();
        archivedHabits = repository.observeArchived();
    }

    public LiveData<Filter> filter() {
        return filter;
    }

    public void setFilter(Filter newFilter) {
        filter.setValue(newFilter);
    }

    public LiveData<List<HabitModels.HabitTodayItem>> items() {
        return items;
    }

    public LiveData<List<HabitModels.HabitSnapshot>> activeHabits() {
        return activeHabits;
    }

    public LiveData<List<HabitModels.HabitSnapshot>> archivedHabits() {
        return archivedHabits;
    }

    public void act(HabitModels.HabitTodayItem item) {
        act(item, null);
    }

    public void act(
            HabitModels.HabitTodayItem item,
            @Nullable ResultCallback<HabitModels.CompletionResult> callback) {
        HabitAction action;
        String nonce = UUID.randomUUID().toString();
        switch (item.habit.type) {
            case AVOID:
                action = HabitAction.lapse(nonce);
                break;
            case AMOUNT:
                action = HabitAction.increment(nonce, item.habit.incrementVal > 0 ? item.habit.incrementVal : 1);
                break;
            default:
                action = HabitAction.complete(nonce);
                break;
        }
        repository.record(action.forHabit(item.habit.id, today), callback != null ? callback : ignored -> {});
    }

    public void undo(long habitId, long eventId, ResultCallback<HabitModels.CompletionResult> callback) {
        repository.undo(habitId, eventId, callback);
    }

    public void archive(long habitId) {
        repository.archive(habitId);
    }

    public void restore(long habitId) {
        repository.restore(habitId);
    }

    public void delete(long habitId) {
        repository.delete(habitId);
    }

    public void moveUp(HabitModels.HabitSnapshot habit, List<HabitModels.HabitSnapshot> list) {
        int index = list.indexOf(habit);
        if (index > 0) {
            HabitModels.HabitSnapshot prev = list.get(index - 1);
            repository.reorder(habit.id, prev.sortOrder);
            repository.reorder(prev.id, habit.sortOrder);
        }
    }

    public void moveDown(HabitModels.HabitSnapshot habit, List<HabitModels.HabitSnapshot> list) {
        int index = list.indexOf(habit);
        if (index >= 0 && index < list.size() - 1) {
            HabitModels.HabitSnapshot next = list.get(index + 1);
            repository.reorder(habit.id, next.sortOrder);
            repository.reorder(next.id, habit.sortOrder);
        }
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final HabitRepository repository;

        public Factory(HabitRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(HabitsViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new HabitsViewModel(repository));
        }
    }
}
