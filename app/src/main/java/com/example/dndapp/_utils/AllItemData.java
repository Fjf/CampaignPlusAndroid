package com.example.dndapp._utils;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AllItemData {
    private static final String url = "http://dnd5eapi.co/api/equipment";
    private static final String TAG = "AllItemData";

    private String[] items;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public String[] getItems() {
        return items;
    }

    public void updateItems(AsyncHttpResponseHandler responseHandler) {
        client.get(url, null, responseHandler);
    }
}
