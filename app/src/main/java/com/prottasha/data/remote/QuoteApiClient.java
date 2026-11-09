package com.prottasha.data.remote;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class QuoteApiClient {

    public interface QuoteCallback {
        void onSuccess(String quote, String author);
        void onFailure(String error);
    }

    private static final String QUOTE_URL = "https://zenquotes.io/api/today";
    private static final OkHttpClient client = new OkHttpClient();

    public static void fetchDailyQuote(QuoteCallback callback) {
        Request request = new Request.Builder()
                .url(QUOTE_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    if (callback != null) {
                        callback.onFailure("HTTP error: " + response.code());
                    }
                    return;
                }

                try {
                    String json = response.body().string();
                    JSONArray array = new JSONArray(json);
                    if (array.length() > 0) {
                        JSONObject first = array.getJSONObject(0);
                        String q = first.optString("q", "We are what we repeatedly do.");
                        String a = first.optString("a", "Will Durant");
                        if (callback != null) {
                            callback.onSuccess(q, a);
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onFailure("Parse error: " + e.getMessage());
                    }
                }
            }
        });
    }
}
