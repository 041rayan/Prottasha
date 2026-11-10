package com.prottasha.support.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class SupportDirectoryRepository {

    public enum SourceType {
        LIVE,
        CACHED,
        BUNDLED,
        OFFLINE
    }

    public static final class DirectoryResult {
        public final List<DirectoryEntryEntity> entries;
        public final SourceType sourceType;
        public final String lastUpdated;

        public DirectoryResult(List<DirectoryEntryEntity> entries, SourceType sourceType, String lastUpdated) {
            this.entries = Collections.unmodifiableList(entries);
            this.sourceType = sourceType;
            this.lastUpdated = lastUpdated;
        }
    }

    public interface DirectoryCallback {
        void onLoaded(DirectoryResult result);
    }

    private static final String CACHE_FILE_NAME = "support_directory_cache.json";
    private static final String DEFAULT_REMOTE_URL = "https://raw.githubusercontent.com/prottasha/directory/main/helplines.json";

    private final OkHttpClient httpClient;
    private Handler mainHandler;

    public SupportDirectoryRepository() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .callTimeout(8, TimeUnit.SECONDS)
                .build();
    }

    private void postToMain(Runnable r) {
        if (mainHandler == null) {
            try {
                mainHandler = new Handler(Looper.getMainLooper());
            } catch (RuntimeException ignored) {
                // In pure unit test environments without Android framework mocking
            }
        }
        if (mainHandler != null) {
            mainHandler.post(r);
        } else {
            r.run();
        }
    }

    public List<DirectoryEntryEntity> entries() {
        return bundledEntries();
    }

    public List<DirectoryEntryEntity> bundledEntries() {
        return Arrays.asList(
                new DirectoryEntryEntity(
                        "Bangladesh National Emergency Service",
                        "999",
                        "24 hours",
                        "Bangladesh Police",
                        "2026-11-10",
                        "VERIFIED",
                        "https://www.police.gov.bd/"
                ),
                new DirectoryEntryEntity(
                        "National Mental Health Helpline (Kaan Pete Roi)",
                        "01779554391",
                        "3:00 PM – 3:00 AM",
                        "Kaan Pete Roi Emotional Support Helpline",
                        "2026-11-10",
                        "VERIFIED",
                        "https://shuni.org/"
                ),
                new DirectoryEntryEntity(
                        "National Helpline for Violence Against Women and Children",
                        "109",
                        "24 hours",
                        "Ministry of Women and Children Affairs",
                        "2026-11-10",
                        "VERIFIED",
                        "https://www.mspvaw.gov.bd/"
                ),
                new DirectoryEntryEntity(
                        "National Health Call Centre (Shastho Batayan)",
                        "16263",
                        "24 hours",
                        "Directorate General of Health Services (DGHS)",
                        "2026-11-10",
                        "VERIFIED",
                        "https://dghs.gov.bd/"
                )
        );
    }

    public void loadDirectory(Context context, DirectoryCallback callback) {
        // Check cache first
        File cacheFile = new File(context.getFilesDir(), CACHE_FILE_NAME);
        if (cacheFile.exists()) {
            try {
                String cachedJson = readStringFromFile(cacheFile);
                List<DirectoryEntryEntity> entries = parseDirectoryJson(cachedJson);
                JSONObject obj = new JSONObject(cachedJson);
                String updated = obj.optString("updatedAt", "Cached");
                callback.onLoaded(new DirectoryResult(entries, SourceType.CACHED, updated));
                return;
            } catch (Exception ignored) {
                // Ignore corrupted cache and fall back
            }
        }
        callback.onLoaded(new DirectoryResult(bundledEntries(), SourceType.BUNDLED, "2026-11-10"));
    }

    public void refreshRemote(Context context, @Nullable String urlOverride, DirectoryCallback callback) {
        String targetUrl = urlOverride != null ? urlOverride : DEFAULT_REMOTE_URL;
        Request request = new Request.Builder()
                .url(targetUrl)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                postToMain(() -> loadDirectory(context, result -> {
                    // Return cached/bundled with OFFLINE marker
                    callback.onLoaded(new DirectoryResult(result.entries, SourceType.OFFLINE, result.lastUpdated));
                }));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    postToMain(() -> loadDirectory(context, result ->
                            callback.onLoaded(new DirectoryResult(result.entries, SourceType.OFFLINE, result.lastUpdated))));
                    return;
                }
                String body = response.body().string();
                try {
                    List<DirectoryEntryEntity> parsed = parseDirectoryJson(body);
                    JSONObject obj = new JSONObject(body);
                    String updated = obj.optString("updatedAt", "Recently");
                    // Save to cache
                    File cacheFile = new File(context.getFilesDir(), CACHE_FILE_NAME);
                    writeStringToFile(cacheFile, body);

                    postToMain(() -> callback.onLoaded(new DirectoryResult(parsed, SourceType.LIVE, updated)));
                } catch (Exception ex) {
                    postToMain(() -> loadDirectory(context, result ->
                            callback.onLoaded(new DirectoryResult(result.entries, SourceType.OFFLINE, result.lastUpdated))));
                }
            }
        });
    }

    public static List<DirectoryEntryEntity> parseDirectoryJson(String jsonString) {
        try {
            JSONObject root = new JSONObject(jsonString);
            if (!root.has("entries")) {
                throw new IllegalArgumentException("Missing 'entries' array in directory JSON");
            }
            JSONArray array = root.getJSONArray("entries");
            List<DirectoryEntryEntity> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.getJSONObject(i);
                String name = entry.getString("name").trim();
                String phone = entry.getString("phone").trim();
                String hours = entry.getString("hours").trim();
                String source = entry.getString("source").trim();
                String verifiedDate = entry.getString("verifiedDate").trim();
                String status = entry.getString("status").trim();
                String website = entry.optString("website", null);

                if (name.isEmpty() || phone.isEmpty() || hours.isEmpty() || source.isEmpty() || status.isEmpty()) {
                    throw new IllegalArgumentException("Required fields cannot be empty in entry: " + name);
                }
                if (!phone.matches("^[0-9+]+$")) {
                    throw new IllegalArgumentException("Invalid phone number format: " + phone);
                }
                if (website != null && !website.isEmpty() && !isSafeHttps(website)) {
                    throw new IllegalArgumentException("Insecure or invalid website URL: " + website);
                }
                list.add(new DirectoryEntryEntity(name, phone, hours, source, verifiedDate, status, website));
            }
            return Collections.unmodifiableList(list);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Malformed JSON in directory data", e);
        }
    }

    public static boolean isSafeHttps(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && !uri.getHost().isEmpty();
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    private static String readStringFromFile(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int read = in.read(bytes);
            if (read != bytes.length) throw new IOException("Incomplete read");
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static void writeStringToFile(File file, String text) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
