package com.prottasha.guard.platform;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.prottasha.guard.domain.EssentialTargetPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LaunchableAppSource {
    public static final class AppInfo {
        public final String packageName;
        public final String label;

        AppInfo(String packageName, String label) {
            this.packageName = packageName;
            this.label = label;
        }
    }

    private final Context context;

    public LaunchableAppSource(Context context) {
        this.context = context.getApplicationContext();
    }

    public List<AppInfo> listNonEssential() {
        PackageManager manager = context.getPackageManager();
        Intent launcher = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        EssentialTargetPolicy policy = new EssentialTargetPolicy(context.getPackageName());
        List<AppInfo> result = new ArrayList<>();
        for (ResolveInfo info : manager.queryIntentActivities(launcher, PackageManager.MATCH_ALL)) {
            String packageName = info.activityInfo.packageName;
            if (!policy.isEssential(packageName)) {
                result.add(new AppInfo(packageName, info.loadLabel(manager).toString()));
            }
        }
        result.sort(Comparator.comparing(value -> value.label.toLowerCase(java.util.Locale.ROOT)));
        return result;
    }
}
