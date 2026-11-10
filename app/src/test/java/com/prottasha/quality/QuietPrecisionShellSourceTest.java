package com.prottasha.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public final class QuietPrecisionShellSourceTest {

    private static final Path PROJECT = locateProject();

    @Test
    public void mainHeaderUsesAccessibleIconActionsAndSectionTitle() throws Exception {
        String layout = read("app/src/main/res/layout/activity_main.xml");
        assertTrue(layout.contains("@+id/main_section_title"));
        assertTrue(layout.contains("@+id/main_lifeline_button"));
        assertTrue(layout.contains("@+id/main_settings_button"));
        assertTrue(layout.contains("@drawable/ic_lifeline"));
        assertTrue(layout.contains("@drawable/ic_settings"));
        assertTrue(layout.contains("android:contentDescription=\"@string/main_lifeline\""));
        assertTrue(layout.contains("android:contentDescription=\"@string/main_settings\""));
        assertFalse(buttonTag(layout, "main_lifeline_button").contains("android:text="));
        assertFalse(buttonTag(layout, "main_settings_button").contains("android:text="));
    }

    @Test
    public void bottomNavigationUsesFiveDistinctIconsAndMintActiveTint() throws Exception {
        String menu = read("app/src/main/res/menu/bottom_nav_menu.xml");
        Matcher matcher = Pattern.compile("android:icon=\"@drawable/([^\"]+)\"").matcher(menu);
        Set<String> icons = new HashSet<>();
        while (matcher.find()) icons.add(matcher.group(1));
        assertEquals(5, icons.size());
        assertTrue(read("app/src/main/res/color/nav_item_tint.xml")
                .contains("state_checked=\"true\" android:color=\"@color/color_hope\""));
    }

    @Test
    public void sharedQuietPrecisionStylesExist() throws Exception {
        String styles = read("app/src/main/res/values/quiet_precision_styles.xml");
        assertTrue(styles.contains("Widget.Prottasha.Button.Primary"));
        assertTrue(styles.contains("Widget.Prottasha.Button.Secondary"));
        assertTrue(styles.contains("Widget.Prottasha.Button.Icon"));
        assertTrue(styles.contains("Widget.Prottasha.Surface.Tonal"));
        assertTrue(styles.contains("Widget.Prottasha.Row"));
        assertTrue(styles.contains("Widget.Prottasha.Segmented"));
    }

    private static String buttonTag(String layout, String id) {
        Matcher matcher = Pattern.compile(
                "<com\\.google\\.android\\.material\\.button\\.MaterialButton[^>]+"
                        + Pattern.quote("@+id/" + id) + "[^>]*/>", Pattern.DOTALL)
                .matcher(layout);
        assertTrue("Missing button " + id, matcher.find());
        return matcher.group();
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
