package com.example.campaignplus._data.items;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AvailableItems {
    private static final String TAG = "AvailableItems";
    public static ArrayList<ItemData> items = new ArrayList<>();

    /**
     * Helper method to post callbacks to the main thread.
     */
    private static void postToMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public AvailableItems() {
        // Override default constructor to ignore initialization.
    }

    public static void setItems(JSONArray newItems) throws JSONException {
        for (int i = 0; i < newItems.length(); i++) {
            items.add(new ItemData(newItems.getJSONObject(i)));
        }
    }

    public static void updateItems(final CallBack fn) {
        HttpUtils.newGet("user/items", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Connection failed. Please check your server settings (IP: " + HttpUtils.getUrl() + ")";
                }
                fn.error(errorMessage);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Raw JSON response: " + jsonResponse);
                        JSONArray items = new JSONArray(jsonResponse);
                        Log.d(TAG, "Parsed " + items.length() + " items");
                        setItems(items);
                        fn.success();
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage(), e);
                        fn.error("Failed to parse items: " + e.getMessage());
                    }
                } else {
                    String errorMsg = "Server returned error: " + response.code();
                    if (response.code() == 404) {
                        errorMsg += " - Items endpoint not found. Check server configuration.";
                    } else if (response.code() >= 500) {
                        errorMsg += " - Server error. Please contact the server administrator.";
                    }
                    fn.error(errorMsg);
                }
            }
        });
    }

    public static ItemData getItem(int idx) {
        for (ItemData item : items) {
            if (item.getId() == idx) {
                return item;
            }
        }
        return null;
    }

    public static void updateItem(JSONObject rawNewItem) throws JSONException {
        ItemData newItem = new ItemData(rawNewItem);
        for (int i = 0; i < items.size(); i++) {
            ItemData item = items.get(i);
            // Overwrite existing item if we performed an update
            if (item.getId() == newItem.getId()) {
                items.set(i, newItem);
                return;
            }
        }
        // Add new item to list if it has a new ID.
        items.add(newItem);
    }
}
