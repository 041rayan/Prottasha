package com.prottasha.data.remote;

import android.content.Context;
import com.prottasha.R;
import com.prottasha.data.model.TherapistItem;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TherapistApiClient {

    public interface TherapistCallback {
        void onLoaded(List<TherapistItem> clinics, boolean isRemote);
    }

    private static final String REMOTE_DIRECTORY_URL = "https://raw.githubusercontent.com/prottasha/directory/main/clinics.json";
    private static final OkHttpClient client = new OkHttpClient();

    public static void fetchClinics(Context context, TherapistCallback callback) {
        Request request = new Request.Builder()
                .url(REMOTE_DIRECTORY_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Fallback to local raw JSON resource
                loadLocalFallback(context, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        List<TherapistItem> items = parseClinicsJson(json);
                        if (callback != null) {
                            callback.onLoaded(items, true);
                        }
                        return;
                    } catch (Exception ignored) {}
                }
                loadLocalFallback(context, callback);
            }
        });
    }

    private static void loadLocalFallback(Context context, TherapistCallback callback) {
        try {
            InputStream is = context.getResources().openRawResource(R.raw.fallback_directory);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            List<TherapistItem> items = parseClinicsJson(sb.toString());
            if (callback != null) {
                callback.onLoaded(items, false);
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onLoaded(new ArrayList<>(), false);
            }
        }
    }

    public static List<TherapistItem> parseClinicsJson(String jsonString) throws Exception {
        List<TherapistItem> list = new ArrayList<>();
        JSONObject root = new JSONObject(jsonString);
        JSONArray clinics = root.getJSONArray("clinics");
        for (int i = 0; i < clinics.length(); i++) {
            JSONObject obj = clinics.getJSONObject(i);
            list.add(new TherapistItem(
                    obj.optString("name", "Support Center"),
                    obj.optString("specialization", "Counseling"),
                    obj.optString("phone", "999"),
                    obj.optString("hours", "24/7"),
                    obj.optString("website", "https://findahelpline.com")
            ));
        }
        return list;
    }
}
