package com.prottasha.circle.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.prottasha.circle.domain.PodMessage;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PodRepositoryTest {
    @Test
    public void showcasePodIdIsFixedAndNotUserControlled() {
        assertEquals("academic-showcase", PodRepository.SHOWCASE_POD_ID);
    }

    @Test
    public void messageValidationMatchesSecurityRules() {
        assertTrue(PodRepository.isValidMessageText("Hello world"));
        assertFalse(PodRepository.isValidMessageText(""));
        assertFalse(PodRepository.isValidMessageText("   "));
        assertFalse(PodRepository.isValidMessageText(null));

        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1001; i++) longMessage.append("a");
        assertFalse(PodRepository.isValidMessageText(longMessage.toString()));
    }

    @Test
    public void reportReasonValidationMatchesSecurityRules() {
        assertTrue(PodRepository.isValidReportReason("Inappropriate language"));
        assertFalse(PodRepository.isValidReportReason(""));
        assertFalse(PodRepository.isValidReportReason(null));

        StringBuilder longReason = new StringBuilder();
        for (int i = 0; i < 501; i++) longReason.append("x");
        assertFalse(PodRepository.isValidReportReason(longReason.toString()));
    }

    @Test
    public void blockFilterExcludesBlockedSenders() {
        PodMessage m1 = new PodMessage("1", "alice", "Alice", "Hi", 1000L);
        PodMessage m2 = new PodMessage("2", "bob", "Bob", "Hello", 2000L);
        PodMessage m3 = new PodMessage("3", "mallory", "Mallory", "Spam", 3000L);

        List<PodMessage> all = Arrays.asList(m1, m2, m3);
        Set<String> blocked = new HashSet<>(Collections.singletonList("mallory"));

        List<PodMessage> filtered = PodRepository.filterBlockedMessages(all, blocked);
        assertEquals(2, filtered.size());
        assertEquals("alice", filtered.get(0).senderUid);
        assertEquals("bob", filtered.get(1).senderUid);
    }
}
