package com.prottasha.circle.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public final class AccountabilityPartnerStoreTest {

    @Test
    public void generateShareNoteCreatesTruthfulSummary() {
        String note = AccountabilityPartner.generateShareNote(
                "Rahim",
                Arrays.asList("Morning Mindfulness", "Evening Screen Boundary"));
        assertTrue(note.contains("Rahim"));
        assertTrue(note.contains("Morning Mindfulness"));
        assertTrue(note.contains("Evening Screen Boundary"));
        assertFalse(note.contains("100%")); // no fake statistics
    }

    @Test
    public void generateShareNoteWithNoHabitsRemainsTruthful() {
        String note = AccountabilityPartner.generateShareNote("Karim", Collections.emptyList());
        assertTrue(note.contains("Karim"));
        assertTrue(note.contains("setting up my recovery habits"));
    }

    @Test
    public void partnerValidationEnforcesValidNameAndPhone() {
        assertTrue(AccountabilityPartner.isValidPhoneNumber("01712345678"));
        assertTrue(AccountabilityPartner.isValidPhoneNumber("+8801712345678"));
        assertFalse(AccountabilityPartner.isValidPhoneNumber("abc"));
        assertFalse(AccountabilityPartner.isValidPhoneNumber("12")); // too short
        assertFalse(AccountabilityPartner.isValidPhoneNumber(null));
    }
}
