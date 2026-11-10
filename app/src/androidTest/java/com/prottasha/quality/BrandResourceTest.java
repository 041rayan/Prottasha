package com.prottasha.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class BrandResourceTest {

    private final Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void quietPrecisionFontsAreBundled() {
        assertFont("newsreader_medium");
        assertFont("newsreader_semibold");
        assertFont("ibm_plex_sans_regular");
        assertFont("ibm_plex_sans_medium");
        assertFont("ibm_plex_sans_semibold");
        assertFont("ibm_plex_sans_bold");
        assertFont("ibm_plex_mono_regular");
        assertFont("ibm_plex_mono_medium");
    }

    @Test
    public void approvedBrandDrawablesResolve() {
        assertDrawable("prottasha_logo");
        assertDrawable("ic_launcher_monochrome");
        assertDrawable("ic_notification_prottasha");
        assertTrue(context.getResources().getIdentifier(
                "ic_launcher", "mipmap", context.getPackageName()) != 0);
    }

    @Test
    public void quietPrecisionPaletteMatchesApprovedValues() {
        assertEquals(Color.rgb(0x07, 0x09, 0x0D), color(R.color.color_background));
        assertEquals(Color.rgb(0x0D, 0x11, 0x17), color(R.color.color_surface));
        assertEquals(Color.rgb(0x15, 0x1A, 0x22), color(R.color.color_surface_raised));
        assertEquals(Color.rgb(0x25, 0x2D, 0x39), color(R.color.color_border));
        assertEquals(Color.rgb(0x43, 0xE0, 0xAB), color(R.color.color_hope));
        assertEquals(Color.rgb(0x06, 0x11, 0x0D), color(R.color.color_on_hope));
        assertEquals(Color.rgb(0x8B, 0x91, 0xFF), color(R.color.color_identity));
        assertEquals(Color.rgb(0xF5, 0xF7, 0xFA), color(R.color.color_text_primary));
    }

    @Test
    public void normalTextContrastMeetsWcag() {
        int background = color(R.color.color_background);
        assertTrue(contrast(color(R.color.color_text_primary), background) >= 4.5);
        assertTrue(contrast(color(R.color.color_text_secondary), background) >= 4.5);
    }

    private void assertFont(String name) {
        int id = context.getResources().getIdentifier(name, "font", context.getPackageName());
        assertTrue(name + " is missing", id != 0);
        assertNotNull(name + " could not be loaded", ResourcesCompat.getFont(context, id));
    }

    private void assertDrawable(String name) {
        int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        assertTrue(name + " is missing", id != 0);
        assertNotNull(name + " could not be loaded", ContextCompat.getDrawable(context, id));
    }

    private int color(int id) {
        return ContextCompat.getColor(context, id);
    }

    private static double contrast(int foreground, int background) {
        double lighter = Math.max(luminance(foreground), luminance(background));
        double darker = Math.min(luminance(foreground), luminance(background));
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double luminance(int color) {
        return 0.2126 * channel(Color.red(color))
                + 0.7152 * channel(Color.green(color))
                + 0.0722 * channel(Color.blue(color));
    }

    private static double channel(int value) {
        double normalized = value / 255.0;
        return normalized <= 0.03928
                ? normalized / 12.92
                : Math.pow((normalized + 0.055) / 1.055, 2.4);
    }
}
