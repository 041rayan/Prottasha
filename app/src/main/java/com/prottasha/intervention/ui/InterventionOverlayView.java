package com.prottasha.intervention.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.prottasha.R;
import com.prottasha.databinding.ViewInterventionOverlayBinding;

public final class InterventionOverlayView extends FrameLayout {
    public interface Listener {
        void onStepBack();
        void onContinue(String intention);
    }

    private final ViewInterventionOverlayBinding binding;

    public InterventionOverlayView(Context context, String target, boolean block, Listener listener) {
        super(context);
        binding = ViewInterventionOverlayBinding.inflate(LayoutInflater.from(context), this, true);
        binding.interventionTarget.setText(target);
        binding.interventionContinue.setVisibility(block ? GONE : VISIBLE);
        binding.interventionIntentionLayout.setVisibility(block ? GONE : VISIBLE);
        binding.interventionStepBack.setOnClickListener(ignored -> listener.onStepBack());
        binding.interventionContinue.setOnClickListener(ignored -> {
            String intention = binding.interventionIntention.getText() == null
                    ? "" : binding.interventionIntention.getText().toString();
            if (intention.trim().isEmpty()) {
                binding.interventionIntentionLayout.setError(
                        getContext().getString(R.string.intervention_intention_required));
            } else {
                listener.onContinue(intention);
            }
        });
    }
}
