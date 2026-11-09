package com.prottasha.usage.browser;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

@SuppressWarnings("deprecation")
public final class ChromeDomainAdapter implements BrowserDomainAdapter {
    private static final String[] ADDRESS_IDS = {
            "com.android.chrome:id/url_bar",
            "com.brave.browser:id/url_bar"
    };

    @Override
    public String readSelectedDomain(AccessibilityNodeInfo root) {
        if (root == null) return null;
        for (String id : ADDRESS_IDS) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes == null || nodes.isEmpty()) continue;
            CharSequence text = nodes.get(0).getText();
            String domain = DomainNormalizer.normalize(text == null ? null : text.toString());
            for (AccessibilityNodeInfo node : nodes) node.recycle();
            if (domain != null) return domain;
        }
        return null;
    }
}
