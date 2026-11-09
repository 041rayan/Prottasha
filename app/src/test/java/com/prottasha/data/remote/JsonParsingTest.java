package com.prottasha.data.remote;

import com.prottasha.data.model.TherapistItem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class JsonParsingTest {

    @Test
    public void testZenQuotesJsonParsing() throws Exception {
        String mockResponse = "[\n" +
                "  {\n" +
                "    \"q\": \"We are what we repeatedly do. Excellence, then, is not an act, but a habit.\",\n" +
                "    \"a\": \"Will Durant\",\n" +
                "    \"h\": \"<blockquote>...</blockquote>\"\n" +
                "  }\n" +
                "]";

        JSONArray array = new JSONArray(mockResponse);
        assertEquals(1, array.length());
        JSONObject first = array.getJSONObject(0);
        assertEquals("We are what we repeatedly do. Excellence, then, is not an act, but a habit.", first.getString("q"));
        assertEquals("Will Durant", first.getString("a"));
    }

    @Test
    public void testTherapistDirectoryJsonParsing() throws Exception {
        String mockDirectoryJson = "{\n" +
                "  \"version\": \"2026.11\",\n" +
                "  \"clinics\": [\n" +
                "    {\n" +
                "      \"name\": \"Talk Hope Bangladesh\",\n" +
                "      \"specialization\": \"Suicide prevention and mental health support\",\n" +
                "      \"phone\": \"+8809638881888\",\n" +
                "      \"hours\": \"24/7 Daily\",\n" +
                "      \"website\": \"https://facebook.com/talkhope.bd\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Kaan Pete Roi\",\n" +
                "      \"specialization\": \"Emotional support helpline\",\n" +
                "      \"phone\": \"09612119911\",\n" +
                "      \"hours\": \"3:00 PM - 3:00 AM Daily\",\n" +
                "      \"website\": \"https://facebook.com/kaan.pete.roi/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        List<TherapistItem> clinics = TherapistApiClient.parseClinicsJson(mockDirectoryJson);
        assertNotNull(clinics);
        assertEquals(2, clinics.size());
        assertEquals("Talk Hope Bangladesh", clinics.get(0).getName());
        assertEquals("+8809638881888", clinics.get(0).getPhone());
        assertEquals("Kaan Pete Roi", clinics.get(1).getName());
        assertEquals("09612119911", clinics.get(1).getPhone());
    }
}
