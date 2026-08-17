package com.example.campaignplus._data.items;

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
    public static ArrayList<ItemData> items = new ArrayList<>();

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
                fn.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray items = new JSONArray(jsonResponse);
                        setItems(items);
                        fn.success();
                    } catch (Exception e) {
                        fn.error("Fetching available items failed: " + e.getMessage());
                    }
                } else {
                    fn.error("Failed to fetch items: " + response.code());
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
