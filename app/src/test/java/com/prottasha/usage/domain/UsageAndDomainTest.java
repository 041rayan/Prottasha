package com.prottasha.usage.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.prottasha.usage.browser.DomainNormalizer;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public final class UsageAndDomainTest {

    @Test
    public void aggregatorClampsOverlapAndRejectsMalformedSessions() {
        UsageAggregator.Summary summary = UsageAggregator.aggregate(Arrays.asList(
                new UsageAggregator.Session("a", 1000, 4000),
                new UsageAggregator.Session("a", 3000, 6000),
                new UsageAggregator.Session("a", 7000, 6000)));
        assertEquals(5000L, summary.totalMillis);
        assertEquals(UsageAggregator.Quality.PARTIAL, summary.quality);
    }

    @Test
    public void missingUsageAccessIsUnavailableNotZero() {
        UsageAggregator.Summary summary = UsageAggregator.unavailable();
        assertEquals(UsageAggregator.Quality.UNAVAILABLE, summary.quality);
        assertFalse(summary.hasMeasuredValue);
    }

    @Test
    public void emptyMeasuredSessionsAreCompleteZero() {
        UsageAggregator.Summary summary = UsageAggregator.aggregate(Collections.emptyList());
        assertEquals(UsageAggregator.Quality.COMPLETE, summary.quality);
        assertTrue(summary.hasMeasuredValue);
        assertEquals(0L, summary.totalMillis);
    }

    @Test
    public void domainNormalizerStoresOnlyCanonicalHost() {
        assertEquals("example.com", DomainNormalizer.normalize(
                "https://user:pass@Example.COM:443/path?q=secret#fragment"));
        assertEquals("xn--bcher-kva.example", DomainNormalizer.normalize(
                "https://bücher.example/path"));
    }

    @Test
    public void domainNormalizerRejectsSearchTextPrivateAndUnsupportedValues() {
        assertNull(DomainNormalizer.normalize("how do I stop scrolling"));
        assertNull(DomainNormalizer.normalize("chrome://settings"));
        assertNull(DomainNormalizer.normalize("file:///private/note"));
        assertNull(DomainNormalizer.normalize("https://localhost/private"));
    }
}
