package com.example.campaignplus._utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AllItemData {
    private static final String url = "http://dnd5eapi.co/api/equipment";
    private static final String TAG = "AllItemData";

    private String[] items;

    private static final OkHttpClient client = new OkHttpClient();

    public String[] getItems() {
        return items;
    }

    public void updateItems(Callback responseHandler) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(responseHandler);
    }
}
