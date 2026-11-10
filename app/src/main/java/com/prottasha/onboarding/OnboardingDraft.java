package com.prottasha.onboarding;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OnboardingDraft {
    public final String purpose;
    public final String identityStatement;
    public final String successMemory;
    public final List<String> selectedPackages;
    public final String targetMode;

    public OnboardingDraft(
            @Nullable String purpose,
            @Nullable String identityStatement,
            @Nullable String successMemory,
            @Nullable List<String> selectedPackages,
            @Nullable String targetMode) {
        this.purpose = purpose == null ? "" : purpose;
        this.identityStatement = identityStatement == null ? "" : identityStatement;
        this.successMemory = successMemory == null ? "" : successMemory;
        this.selectedPackages = selectedPackages == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(selectedPackages));
        this.targetMode = targetMode == null ? "PAUSE" : targetMode;
    }

    public OnboardingDraft(String purpose, String identityStatement) {
        this(purpose, identityStatement, "", null, "PAUSE");
    }
}
