package com.prottasha.today;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.IdentityRepository;

public final class TodayViewModel extends ViewModel {

    private final IdentityRepository identityRepository;

    TodayViewModel(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public LiveData<IdentityEntity> activeIdentity() {
        return identityRepository.observeActive();
    }

    public static final class Factory implements ViewModelProvider.Factory {
        private final IdentityRepository identityRepository;

        public Factory(IdentityRepository identityRepository) {
            this.identityRepository = identityRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (!modelClass.isAssignableFrom(TodayViewModel.class)) {
                throw new IllegalArgumentException("Unsupported ViewModel " + modelClass.getName());
            }
            return modelClass.cast(new TodayViewModel(identityRepository));
        }
    }
}
