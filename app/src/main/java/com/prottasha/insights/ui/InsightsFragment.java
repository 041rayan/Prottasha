package com.prottasha.insights.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prottasha.ProttashaApp;
import com.prottasha.R;
import com.prottasha.databinding.FragmentInsightsBinding;
import com.prottasha.insights.data.InsightsRepository;
import com.prottasha.insights.domain.InsightsCalculator;
import com.prottasha.methodology.MethodologyActivity;

public final class InsightsFragment extends Fragment {
    private FragmentInsightsBinding binding;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle state) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        binding.insightsReflect.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), ReflectionActivity.class)));
        binding.insightsMethodology.setOnClickListener(ignored ->
                startActivity(new Intent(requireContext(), MethodologyActivity.class)));
        ProttashaApp app = (ProttashaApp) requireActivity().getApplication();
        app.appGraph().executors().disk().execute(() -> {
            InsightsCalculator.Snapshot snapshot = new InsightsRepository(
                    app.appGraph().userSessionDatabaseProvider()).snapshot();
            requireActivity().runOnUiThread(() -> render(snapshot));
        });
    }

    private void render(InsightsCalculator.Snapshot snapshot) {
        binding.insightsSummary.setText(snapshot.hasData
                ? getString(R.string.insights_rate, Math.round(snapshot.followThroughRate * 100),
                        snapshot.decisions)
                : getString(R.string.insights_no_data));
        binding.insightsTrend.setRate(snapshot.followThroughRate, snapshot.decisions);
        binding.insightsSample.setText(snapshot.adequateSample
                ? R.string.insights_sample_adequate : R.string.insights_sample_limited);
    }

    @Override public void onDestroyView() { binding = null; super.onDestroyView(); }
}
