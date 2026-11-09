package com.prottasha.usage.browser;

import android.view.accessibility.AccessibilityNodeInfo;

public interface BrowserDomainAdapter {
    String readSelectedDomain(AccessibilityNodeInfo root);
}
