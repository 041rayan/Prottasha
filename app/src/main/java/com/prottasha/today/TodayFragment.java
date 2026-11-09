package com.prottasha.today;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.ProttashaApp;
import com.prottasha.databinding.FragmentTodayBinding;

public final class TodayFragment extends Fragment {

    private FragmentTodayBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTodayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProttashaApp application = (ProttashaApp) requireActivity().getApplication();
        TodayViewModel viewModel = new ViewModelProvider(this, new TodayViewModel.Factory(
                application.appGraph().identityRepository())).get(TodayViewModel.class);
        viewModel.activeIdentity().observe(getViewLifecycleOwner(), identity -> {
            binding.todayIdentityCard.setVisibility(identity == null ? View.GONE : View.VISIBLE);
            if (identity != null) {
                binding.todayIdentityStatement.setText(identity.statement);
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
