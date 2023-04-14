package com.example.campaignplus._data.items;

import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class AvailableItems {
    public static ArrayList<ItemData> items = new ArrayList<>();

    private AvailableItems() {
        // Override default constructor to ignore initialization.
    }

    public static void setItems(JSONArray items) throws JSONException {
        for (int i = 0; i < items.length(); i++) {
            // Available items are never weapons, additional weapon data has to be retrieved separately.
            AvailableItems.items.add(new ItemData(items.getJSONObject(i)));
        }
    }

    public static void initialize(final CallBack fn) {
       updateItems(fn);
    }

    public static void updateItems(final CallBack fn) {
        HttpUtils.get("user/items", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray items) {
                try {
                    setItems(items);
                    fn.success();
                } catch (JSONException e) {
                    fn.error("Fetching available items failed: invalid JSON format.");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                fn.error(response);
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
