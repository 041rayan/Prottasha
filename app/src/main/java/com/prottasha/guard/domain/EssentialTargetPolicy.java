package com.prottasha.guard.domain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class EssentialTargetPolicy {
    private final String ownPackage;
    private final Set<String> essential = new HashSet<>(Arrays.asList(
            "com.android.settings",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.permissioncontroller",
            "com.google.android.permissioncontroller"));

    public EssentialTargetPolicy(String ownPackage) {
        this.ownPackage = ownPackage;
    }

    public boolean isEssential(String packageName) {
        return packageName != null
                && (packageName.equals(ownPackage)
                || essential.contains(packageName)
                || packageName.contains("launcher"));
    }
}
