package com.prottasha.habits.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class HabitsViewModel extends ViewModel {

    private final HabitRepository repository;
    private final long today = LocalDate.now().toEpochDay();
    private final LiveData<List<HabitModels.HabitTodayItem>> items;

    HabitsViewModel(HabitRepository repository) {
        this.repository = repository;
        items = repository.observeDueOn(today);
    }

    public LiveData<List<HabitModels.HabitTodayItem>> items() {
        return items;
    }

    public void act(HabitModels.HabitTodayItem item) {
        HabitAction action;
        String nonce = UUID.randomUUID().toString();
        switch (item.habit.type) {
            case AVOID:
                action = HabitAction.lapse(nonce);
                break;
            case AMOUNT:
                action = HabitAction.increment(nonce, 1);
                break;
            default:
                action = HabitAction.complete(nonce);
                break;
        }
        repository.record(action.forHabit(item.habit.id, today), ignored -> { });
    }

    public void archive(long habitId) {
        repository.archive(habitId);
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
