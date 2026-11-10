package com.prottasha.habits.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.core.result.ResultCallback;
import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.core.security.StoredPrivateImage;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitDayImageEntity;
import com.prottasha.habits.data.HabitReminderEntity;
import com.prottasha.habits.data.HabitRepository;
import com.prottasha.habits.data.HabitSubstepEntity;
import com.prottasha.habits.data.HabitVacationEntity;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.reminders.ReminderScheduler;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public final class HabitDetailViewModel extends ViewModel {

    private final HabitRepository repository;
    private final long habitId;
    private final long today = LocalDate.now().toEpochDay();

    private final MutableLiveData<HabitModels.HabitSnapshot> habit = new MutableLiveData<>();
    private final MutableLiveData<HabitModels.HabitStats> stats = new MutableLiveData<>();
    private final MutableLiveData<List<HabitDayAggregateEntity>> history = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<HabitSubstepEntity>> substeps = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<HabitVacationEntity>> vacations = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<HabitReminderEntity>> reminders = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<HabitDayImageEntity>> dayImages = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Bitmap> currentImageBitmap = new MutableLiveData<>(null);
    private final MutableLiveData<String> note = new MutableLiveData<>("");

    public HabitDetailViewModel(HabitRepository repository, long habitId) {
        this.repository = repository;
        this.habitId = habitId;
        refresh();
    }

    public LiveData<HabitModels.HabitSnapshot> habit() {
        return habit;
    }

    public LiveData<HabitModels.HabitStats> stats() {
        return stats;
    }

    public LiveData<List<HabitDayAggregateEntity>> history() {
        return history;
    }

    public LiveData<List<HabitSubstepEntity>> substeps() {
        return substeps;
    }

    public LiveData<List<HabitVacationEntity>> vacations() {
        return vacations;
    }

    public LiveData<List<HabitReminderEntity>> reminders() {
        return reminders;
    }

    public LiveData<List<HabitDayImageEntity>> dayImages() {
        return dayImages;
    }

    public LiveData<Bitmap> currentImageBitmap() {
        return currentImageBitmap;
    }

    public LiveData<String> note() {
        return note;
    }

    public void refresh() {
        if (habitId <= 0) return;
        repository.getHabit(habitId, result -> {
            if (result.isSuccess()) habit.postValue(result.valueOrNull());
        });
        repository.computeStats(habitId, result -> {
            if (result.isSuccess()) stats.postValue(result.valueOrNull());
        });
        repository.getAggregates(habitId, 28, result -> {
            if (result.isSuccess()) history.postValue(result.valueOrNull());
        });
        repository.getSubsteps(habitId, result -> {
            if (result.isSuccess()) substeps.postValue(result.valueOrNull());
        });
        repository.getVacations(habitId, result -> {
            if (result.isSuccess()) vacations.postValue(result.valueOrNull());
        });
        repository.getReminders(habitId, result -> {
            if (result.isSuccess()) reminders.postValue(result.valueOrNull());
        });
        repository.listDayImages(habitId, today, result -> {
            if (result.isSuccess()) {
                dayImages.postValue(result.valueOrNull());
            }
        });
        repository.getNote(habitId, today, result -> {
            if (result.isSuccess()) note.postValue(result.valueOrNull());
        });
    }

    public void addReminder(int hour, int minute, int dayMask, String uidHash, ReminderScheduler scheduler) {
        HabitReminderEntity reminder = new HabitReminderEntity(0L, habitId, hour, minute, dayMask, true);
        repository.saveReminder(reminder, result -> {
            if (result.isSuccess() && result.valueOrNull() != null) {
                reminder.id = result.valueOrNull();
                if (scheduler != null && uidHash != null) {
                    scheduler.schedule(uidHash, reminder);
                }
                refresh();
            }
        });
    }

    public void deleteReminder(long reminderId, ReminderScheduler scheduler) {
        if (scheduler != null) {
            scheduler.cancel(reminderId);
        }
        repository.deleteReminder(reminderId, result -> refresh());
    }

    public void saveDayImage(InputStream stream, EncryptedPrivateFileStore fileStore, UserDatabaseIdentity identity) {
        if (stream == null || fileStore == null || identity == null) return;
        try {
            StoredPrivateImage stored = fileStore.storeImage(
                    identity, EncryptedPrivateFileStore.Category.HABIT, stream);
            repository.saveDayImage(habitId, today, stored.storageId(), result -> {
                loadDayImage(stored.storageId(), fileStore, identity);
                refresh();
            });
        } catch (Exception ignored) {}
    }

    public void loadDayImage(String storageId, EncryptedPrivateFileStore fileStore, UserDatabaseIdentity identity) {
        if (storageId == null || fileStore == null || identity == null) return;
        try {
            byte[] bytes = fileStore.readImage(
                    identity, EncryptedPrivateFileStore.Category.HABIT, storageId);
            if (bytes != null && bytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                currentImageBitmap.postValue(bitmap);
            }
        } catch (Exception ignored) {
            currentImageBitmap.postValue(null);
        }
    }

    public void deleteDayImage(String storageId, EncryptedPrivateFileStore fileStore, UserDatabaseIdentity identity) {
        if (storageId == null) return;
        if (fileStore != null && identity != null) {
            try {
                fileStore.delete(identity, EncryptedPrivateFileStore.Category.HABIT, storageId);
            } catch (Exception ignored) {}
        }
        repository.deleteDayImage(habitId, storageId, result -> {
            currentImageBitmap.postValue(null);
            refresh();
        });
    }

    public void saveNote(String text) {
        repository.saveNote(habitId, today, text, result -> {
            if (result.isSuccess()) note.postValue(text);
        });
    }

    public void backfill(long epochDay, HabitAction.Kind kind, int amount, ResultCallback<HabitModels.CompletionResult> callback) {
        repository.backfill(habitId, epochDay, kind, amount, result -> {
            refresh();
            callback.onResult(result);
        });
    }

    public void addVacation(long startDay, long endDay) {
        repository.addVacation(habitId, startDay, endDay, result -> refresh());
    }

    public void removeVacation(long startDay) {
        repository.removeVacation(habitId, startDay);
        refresh();
    }

    public void archive() {
        repository.archive(habitId);
        refresh();
    }

    public void restore() {
        repository.restore(habitId);
        refresh();
    }

    public void delete(ResultCallback<Boolean> callback) {
        repository.delete(habitId);
        callback.onResult(com.prottasha.core.result.AppResult.success(true));
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
            if (!modelClass.isAssignableFrom(HabitDetailViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new HabitDetailViewModel(repository, habitId));
        }
    }
}
