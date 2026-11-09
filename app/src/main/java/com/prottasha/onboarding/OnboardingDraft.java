package com.prottasha.onboarding;

public final class OnboardingDraft {
    public final String purpose;
    public final String identityStatement;

    public OnboardingDraft(String purpose, String identityStatement) {
        this.purpose = purpose == null ? "" : purpose;
        this.identityStatement = identityStatement == null ? "" : identityStatement;
    }
}
