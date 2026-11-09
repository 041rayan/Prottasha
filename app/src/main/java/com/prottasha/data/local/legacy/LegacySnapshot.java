package com.prottasha.data.local.legacy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class LegacySnapshot {

    public static final String PROTOTYPE_IDENTITY = "prototype_identity";
    public static final String PROTOTYPE_BUILD_HABIT = "prototype_build_habit";
    public static final String PROTOTYPE_AVOID_HABIT = "prototype_avoid_habit";
    public static final String PROTOTYPE_TRIGGER = "prototype_trigger";

    private final boolean sourcePresent;
    private final String sourceHash;
    private final long sourceBytes;
    private final Map<String, Integer> rowCounts;
    private final Set<String> sampleFlags;

    LegacySnapshot(
            boolean sourcePresent,
            String sourceHash,
            long sourceBytes,
            Map<String, Integer> rowCounts,
            Set<String> sampleFlags) {
        this.sourcePresent = sourcePresent;
        this.sourceHash = sourceHash;
        this.sourceBytes = sourceBytes;
        this.rowCounts = Collections.unmodifiableMap(new LinkedHashMap<>(rowCounts));
        this.sampleFlags = Collections.unmodifiableSet(new LinkedHashSet<>(sampleFlags));
    }

    static LegacySnapshot absent() {
        return new LegacySnapshot(
                false, "", 0L, Collections.emptyMap(), Collections.emptySet());
    }

    public boolean sourcePresent() {
        return sourcePresent;
    }

    public String sourceHash() {
        return sourceHash;
    }

    public long sourceBytes() {
        return sourceBytes;
    }

    public Map<String, Integer> rowCounts() {
        return rowCounts;
    }

    public Set<String> sampleFlags() {
        return sampleFlags;
    }
}
