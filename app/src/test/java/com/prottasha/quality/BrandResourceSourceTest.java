package com.prottasha.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public final class BrandResourceSourceTest {

    private static final Path PROJECT = locateProject();

    @Test
    public void quietPrecisionFontsAndBrandAssetsAreBundled() {
        assertFile("app/src/main/res/font/newsreader_variable.ttf");
        assertFile("app/src/main/res/font/newsreader_medium.xml");
        assertFile("app/src/main/res/font/newsreader_semibold.xml");
        assertFile("app/src/main/res/font/ibm_plex_sans_variable.ttf");
        assertFile("app/src/main/res/font/ibm_plex_sans_regular.xml");
        assertFile("app/src/main/res/font/ibm_plex_sans_medium.xml");
        assertFile("app/src/main/res/font/ibm_plex_sans_semibold.xml");
        assertFile("app/src/main/res/font/ibm_plex_sans_bold.xml");
        assertFile("app/src/main/res/font/ibm_plex_mono_regular.ttf");
        assertFile("app/src/main/res/font/ibm_plex_mono_medium.ttf");
        assertFile("app/src/main/res/drawable-nodpi/prottasha_logo.webp");
        assertFile("app/src/main/res/drawable/ic_launcher_monochrome.xml");
        assertFile("app/src/main/res/drawable/ic_notification_prottasha.xml");
        assertFile("docs/licenses/NEWSREADER-OFL.txt");
        assertFile("docs/licenses/IBM-PLEX-OFL.txt");
    }

    @Test
    public void quietPrecisionPaletteMatchesApprovedValues() throws Exception {
        String colors = new String(Files.readAllBytes(
                PROJECT.resolve("app/src/main/res/values/colors.xml")), StandardCharsets.UTF_8);
        assertEquals("#07090D", color(colors, "color_background"));
        assertEquals("#0D1117", color(colors, "color_surface"));
        assertEquals("#151A22", color(colors, "color_surface_raised"));
        assertEquals("#252D39", color(colors, "color_border"));
        assertEquals("#43E0AB", color(colors, "color_hope"));
        assertEquals("#06110D", color(colors, "color_on_hope"));
        assertEquals("#8B91FF", color(colors, "color_identity"));
        assertEquals("#F5F7FA", color(colors, "color_text_primary"));
    }

    private static String color(String xml, String name) {
        Matcher matcher = Pattern.compile(
                "<color\\s+name=\"" + Pattern.quote(name) + "\">([^<]+)</color>")
                .matcher(xml);
        assertTrue("Missing color " + name, matcher.find());
        return matcher.group(1).trim();
    }

    private static void assertFile(String relative) {
        assertTrue("Missing " + relative, Files.isRegularFile(PROJECT.resolve(relative)));
    }

    private static Path locateProject() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (current != null && !Files.isRegularFile(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Project root not found");
        }
        return current;
    }
}
