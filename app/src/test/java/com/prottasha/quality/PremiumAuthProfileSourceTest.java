package com.prottasha.quality;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public final class PremiumAuthProfileSourceTest {

    private static final Path PROJECT = locateProject();

    @Test
    public void signInIsLogoLedAndUsesDirectCopy() throws Exception {
        String xml = read("app/src/main/res/layout/fragment_sign_in.xml");
        assertTrue(xml.contains("@+id/auth_brand_logo"));
        assertTrue(xml.contains("@drawable/prottasha_logo"));
        assertTrue(xml.contains("@style/TextAppearance.Prottasha.Display"));
        assertTrue(xml.contains("@style/Widget.Prottasha.Button.Primary"));
        assertTrue(xml.contains("@+id/auth_privacy_note"));
        assertTrue(xml.contains("android:autofillHints=\"emailAddress\""));
        assertTrue(xml.contains("app:endIconMode=\"password_toggle\""));
    }

    @Test
    public void createAccountShowsStepAndScannableRequirements() throws Exception {
        String xml = read("app/src/main/res/layout/fragment_create_account.xml");
        assertTrue(xml.contains("@+id/create_account_step"));
        assertTrue(xml.contains("@+id/password_requirement_length"));
        assertTrue(xml.contains("@+id/password_requirement_uppercase"));
        assertTrue(xml.contains("@+id/password_requirement_lowercase"));
        assertTrue(xml.contains("@+id/password_requirement_number"));
        assertTrue(xml.contains("@style/Widget.Prottasha.Button.Primary"));
        assertTrue(xml.contains("android:autofillHints=\"newPassword\""));
    }

    @Test
    public void profileSeparatesPrivateAndCircleIdentity() throws Exception {
        String xml = read("app/src/main/res/layout/activity_profile.xml");
        assertTrue(xml.contains("@+id/profile_private_section_label"));
        assertTrue(xml.contains("@+id/profile_circle_section_label"));
        assertTrue(xml.contains("@+id/profile_photo_edit_button"));
        assertTrue(xml.contains("@+id/profile_photo_privacy_text"));
        assertTrue(xml.contains("@style/Widget.Prottasha.Button.Primary"));
        assertFalse(xml.contains("style=\"@style/Widget.Material3.CardView.Filled\""));
    }

    @Test
    public void authCopyIsPlainAndSpecific() throws Exception {
        String strings = read("app/src/main/res/values/auth_strings.xml").toLowerCase();
        assertTrue(strings.contains("use your verified prottasha account"));
        assertFalse(strings.contains("journey"));
        assertFalse(strings.contains("unlock your potential"));
        assertFalse(strings.contains("empower"));
    }

    private static String read(String relative) throws Exception {
        return new String(Files.readAllBytes(PROJECT.resolve(relative)), StandardCharsets.UTF_8);
    }

    private static Path locateProject() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null && !Files.isRegularFile(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) throw new IllegalStateException("Project root not found");
        return current;
    }
}
