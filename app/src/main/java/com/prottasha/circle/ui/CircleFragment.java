package com.prottasha.circle.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.prottasha.databinding.FragmentCircleBinding;

public final class CircleFragment extends Fragment {
    private FragmentCircleBinding binding;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentCircleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        CircleViewModel viewModel = new ViewModelProvider(this).get(CircleViewModel.class);
        binding.circleStatus.setText(viewModel.statusText());
        binding.circleOpenPod.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), PodChatActivity.class)));
        binding.circleAccountability.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), AccountabilityActivity.class)));
    }

    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
