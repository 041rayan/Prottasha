package com.prottasha.habits.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.core.result.ResultCallback;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.data.HabitSubstepEntity;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class HabitEditViewModel extends ViewModel {

    private final HabitRepository repository;
    private final long habitId;
    private final MutableLiveData<HabitModels.HabitSnapshot> habit = new MutableLiveData<>();
    private final MutableLiveData<List<String>> substeps = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isDirty = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

    public HabitEditViewModel(HabitRepository repository, long habitId) {
        this.repository = repository;
        this.habitId = habitId;
        if (habitId > 0) {
            repository.getHabit(habitId, result -> {
                if (result.isSuccess() && result.valueOrNull() != null) {
                    habit.postValue(result.valueOrNull());
                }
            });
            repository.getSubsteps(habitId, result -> {
                if (result.isSuccess() && result.valueOrNull() != null) {
                    List<String> list = new ArrayList<>();
                    for (HabitSubstepEntity s : result.valueOrNull()) {
                        list.add(s.title);
                    }
                    substeps.postValue(list);
                }
            });
        }
    }

    public LiveData<HabitModels.HabitSnapshot> habit() {
        return habit;
    }

    public LiveData<List<String>> substeps() {
        return substeps;
    }

    public LiveData<Boolean> isDirty() {
        return isDirty;
    }

    public LiveData<Boolean> saved() {
        return saved;
    }

    public void markDirty() {
        isDirty.setValue(true);
    }

    public void addSubstep(String title) {
        if (title == null || title.trim().isEmpty()) return;
        List<String> current = new ArrayList<>(substeps.getValue() != null ? substeps.getValue() : new ArrayList<>());
        current.add(title.trim());
        substeps.setValue(current);
        markDirty();
    }

    public void removeSubstep(int index) {
        List<String> current = new ArrayList<>(substeps.getValue() != null ? substeps.getValue() : new ArrayList<>());
        if (index >= 0 && index < current.size()) {
            current.remove(index);
            substeps.setValue(current);
            markDirty();
        }
    }

    public void save(
            String title,
            HabitModels.Type type,
            int target,
            HabitSchedule schedule,
            String cue,
            String twoMinute,
            String reward,
            String ifThen,
            String rebound,
            String unit,
            int increment,
            int max,
            int focusMinutes,
            String accountability,
            ResultCallback<Long> callback) {

        HabitModels.HabitSnapshot snapshot = new HabitModels.HabitSnapshot(
                habitId,
                title,
                type,
                target,
                schedule,
                cue,
                twoMinute,
                reward,
                ifThen,
                rebound,
                unit,
                increment,
                max,
                focusMinutes,
                accountability,
                false,
                0);

        repository.save(snapshot, result -> {
            if (result.isSuccess()) {
                long savedId = result.valueOrNull() != null ? result.valueOrNull() : habitId;
                // Save substeps if build
                if (type == HabitModels.Type.BUILD && substeps.getValue() != null) {
                    List<String> list = substeps.getValue();
                    for (int i = 0; i < list.size(); i++) {
                        repository.saveSubstep(savedId, list.get(i), i, r -> {});
                    }
                }
                saved.postValue(true);
            }
            callback.onResult(result);
        });
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final HabitRepository repository;
        private final long habitId;

        public Factory(HabitRepository repository, long habitId) {
            this.repository = repository;
            this.habitId = habitId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(HabitEditViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new HabitEditViewModel(repository, habitId));
        }
    }
}
