package com.example.campaignplus._utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

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
