package com.example.dndapp._data.items;

import android.widget.Toast;

import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
            AvailableItems.items.add(new ItemData(items.getJSONObject(i), ItemType.ITEM));
        }
    }

    public static void initialize(final FunctionCall fn) {
        HttpUtils.get("items", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        fn.error(response.getString("error"));
                        return;
                    }

                    System.out.println("\n\n\n\n\\\\\n\n\n\n\n");
                    System.out.println(response.get("items").toString());

                    setItems(response.getJSONArray("items"));

                    fn.success();
                } catch (JSONException e) {
                    fn.error("Invalidly formatted JSON retrieved from server.");
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
}
