package com.example.campaignplus._utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * NetworkClient - Modern HTTP client using OkHttp directly.
 * Provides backward-compatible methods for migration from AsyncHttpClient.
 * 
 * TODO: Gradually migrate all callers to use Retrofit's type-safe API
 */
public class NetworkClient {
    private static final String TAG = "NetworkClient";
    private static final String DEFAULT_PORT = "5000";
    
    private static OkHttpClient client;
    private static String ipAddress = "0.0.0.0";
    private static String port = DEFAULT_PORT;
    private static boolean offlineMode = false;
    
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    
    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Initialize network client with context
     */
    public static void init(Context context) {
        // Initialize any context-dependent resources if needed
    }
    
    /**
     * Set offline mode
     */
    public static void setOfflineMode(boolean offline) {
        offlineMode = offline;
    }
    
    /**
     * Set IP address
     */
    public static void setIp(String ip) {
        ipAddress = ip;
    }
    
    /**
     * Get base URL
     */
    public static String getBaseUrl() {
        return String.format("http://%s:%s/api/", ipAddress, port);
    }
    
    /**
     * GET request with callback
     */
    public static void get(String endpoint, Callback callback) {
        if (offlineMode) {
            // Return offline error
            return;
        }
        
        String url = getBaseUrl() + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        client.newCall(request).enqueue(callback);
    }
    
    /**
     * POST request with JSON body
     */
    public static void post(String endpoint, JSONObject jsonBody, Callback callback) {
        if (offlineMode) {
            return;
        }
        
        String url = getBaseUrl() + endpoint;
        RequestBody body = RequestBody.create(
                JSON_MEDIA_TYPE,
                jsonBody.toString()
        );
        
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(request).enqueue(callback);
    }
    
    /**
     * POST request with String body
     */
    public static void post(String endpoint, String bodyString, Callback callback) {
        if (offlineMode) {
            return;
        }
        
        String url = getBaseUrl() + endpoint;
        RequestBody body = RequestBody.create(
                JSON_MEDIA_TYPE,
                bodyString
        );
        
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        
        client.newCall(request).enqueue(callback);
    }
    
    /**
     * PUT request with JSON body
     */
    public static void put(String endpoint, JSONObject jsonBody, Callback callback) {
        if (offlineMode) {
            return;
        }
        
        String url = getBaseUrl() + endpoint;
        RequestBody body = RequestBody.create(
                JSON_MEDIA_TYPE,
                jsonBody.toString()
        );
        
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        
        client.newCall(request).enqueue(callback);
    }
    
    /**
     * PUT request with String body
     */
    public static void put(String endpoint, String bodyString, Callback callback) {
        if (offlineMode) {
            return;
        }
        
        String url = getBaseUrl() + endpoint;
        RequestBody body = RequestBody.create(
                JSON_MEDIA_TYPE,
                bodyString
        );
        
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        
        client.newCall(request).enqueue(callback);
    }
    
    /**
     * DELETE request
     */
    public static void delete(String endpoint, Callback callback) {
        if (offlineMode) {
            return;
        }
        
        String url = getBaseUrl() + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        
        client.newCall(request).enqueue(callback);
    }
    
    /**
     * Helper method to create JSON RequestBody
     */
    public static RequestBody createJsonBody(JSONObject jsonObject) {
        return RequestBody.create(JSON_MEDIA_TYPE, jsonObject.toString());
    }
    
    /**
     * Helper method to create JSON RequestBody from string
     */
    public static RequestBody createJsonBody(String jsonString) {
        return RequestBody.create(JSON_MEDIA_TYPE, jsonString);
    }
}
