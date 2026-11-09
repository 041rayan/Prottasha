package com.prottasha.auth.domain;

public final class SessionAccessPolicy {

    public enum Action {
        RENDER_NEUTRAL_WIDGET,
        COMPLETE_BUILD,
        INCREMENT_AMOUNT,
        CHECK_IN_AVOID,
        OPEN_SENSITIVE_CONTENT
    }

    private SessionAccessPolicy() {
    }

    public static boolean canRun(boolean verifiedOwnerMatches, boolean uiLocked, Action action) {
        if (!verifiedOwnerMatches || action == null) {
            return false;
        }
        if (!uiLocked) {
            return true;
        }
        return action == Action.RENDER_NEUTRAL_WIDGET
                || action == Action.COMPLETE_BUILD
                || action == Action.INCREMENT_AMOUNT;
    }
}
