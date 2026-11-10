package com.prottasha.guard.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.data.GuardRuleRepository;

import java.util.List;

public final class GuardViewModel extends ViewModel {
    private final GuardRuleRepository repository;

    GuardViewModel(GuardRuleRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<GuardRuleEntity>> rules() {
        return repository.observeRules();
    }

    public void setEnabled(long id, boolean enabled) {
        repository.setEnabled(id, enabled, null);
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final GuardRuleRepository repository;

        public Factory(GuardRuleRepository repository) {
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(GuardViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel");
            }
            return modelClass.cast(new GuardViewModel(repository));
        }
    }
}
