package com.example.dndapp._utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;

public class HttpUtils {
//    private static final String BASE_URL = "http://84.107.84.180:5000/api/";
    private static final String TAG = "HttpUtils";
    private static final String PORT = "5000";
    private static String IP_ADDRESS = "0.0.0.0";

    private static AsyncHttpClient client = new AsyncHttpClient();

    static {
        client.setMaxRetriesAndTimeout(0, 1500);
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, StringEntity params, AsyncHttpResponseHandler responseHandler) {
        client.post(null, getAbsoluteUrl(url), null, params, RequestParams.APPLICATION_JSON, responseHandler);
    }

    public static void put(String url, StringEntity params, AsyncHttpResponseHandler responseHandler) {
        client.put(null, getAbsoluteUrl(url), null, params, RequestParams.APPLICATION_JSON, responseHandler);
    }

    public static void delete(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.delete(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.d(TAG, String.format("http://%s:%s/api/", IP_ADDRESS, PORT) + relativeUrl);
        return String.format("http://%s:%s/api/", IP_ADDRESS, PORT) + relativeUrl;
    }

    public static void setIp(String ip) {
        IP_ADDRESS = ip;
    }
}
