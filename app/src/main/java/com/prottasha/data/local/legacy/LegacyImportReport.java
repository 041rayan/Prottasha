package com.prottasha.data.local.legacy;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class LegacyImportReport {

    public enum Status {
        NO_SOURCE,
        STAGED,
        VERIFIED,
        ALREADY_STAGED,
        ALREADY_VERIFIED,
        ROLLED_BACK,
        NOTHING_TO_ROLL_BACK
    }

    private final Status status;
    private final String sourceHash;
    private final Map<String, Integer> rowCounts;
    private final Set<String> sampleFlags;

    LegacyImportReport(Status status, String sourceHash) {
        this.status = status;
        this.sourceHash = sourceHash;
        rowCounts = Collections.emptyMap();
        sampleFlags = Collections.emptySet();
    }

    LegacyImportReport(Status status, LegacySnapshot snapshot) {
        this.status = status;
        sourceHash = snapshot.sourceHash();
        rowCounts = snapshot.rowCounts();
        sampleFlags = snapshot.sampleFlags();
    }

    public Status status() {
        return status;
    }

    public String sourceHash() {
        return sourceHash;
    }

    public Map<String, Integer> rowCounts() {
        return rowCounts;
    }

    public Set<String> sampleFlags() {
        return sampleFlags;
    }
}
