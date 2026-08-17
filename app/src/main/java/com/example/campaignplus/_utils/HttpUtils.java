package com.example.campaignplus._utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * HttpUtils - Modern HTTP client using OkHttp.
 * Provides backward-compatible methods for migration from AsyncHttpClient.
 * 
 * Note: This class uses OkHttp under the hood while maintaining
 * method signatures similar to the legacy AsyncHttpClient for easier migration.
 */
public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final String PORT = "5000";

    private static String IP_ADDRESS = "0.0.0.0";
    public static String OFFLINE_ERROR = "Could not fetch data from cache.";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<>();
                }
            })
            .build();
    
    static boolean offlineMode = false;
    static private SharedPreferences preferences;
    static private final String cacheName = "httpCache";
    static boolean cacheEnabled = false;

    public static void setCacheEnabled(boolean state) {
        cacheEnabled = state;
    }

    public static void setOfflineMode(boolean state) {
        offlineMode = state;
    }

    public static void setPreferences(Context ctx) {
        preferences = ctx.getSharedPreferences(cacheName, MODE_PRIVATE);
    }

    public static void clearCache() {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
        }
    }

    public static String getUrl() {
        return "http://" + IP_ADDRESS + ":" + PORT;
    }

    /**
     * GET request with OkHttp Callback (modern approach)
     */
    public static void newGet(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .build();

        client.newCall(request).enqueue(callback);
    }

    /**
     * GET request - Legacy compatibility wrapper
     * Note: This method signature matches legacy AsyncHttpClient for gradual migration
     */
    public static void get(String url, Callback responseHandler) {
        if (offlineMode) {
            // Cannot make offline request
            return;
        }
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .build();
        client.newCall(request).enqueue(responseHandler);
    }

    /**
     * POST request with JSON body - Legacy compatibility
     * @param url The endpoint URL
     * @param params JSON string to send
     * @param responseHandler Callback for response
     */
    public static void post(String url, String params, Callback responseHandler) {
        if (offlineMode) {
            return;
        }
        
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                params
        );
        
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .post(body)
                .build();
        
        client.newCall(request).enqueue(responseHandler);
    }

    /**
     * PUT request with JSON body - Legacy compatibility
     */
    public static void put(String url, String params, Callback responseHandler) {
        if (offlineMode) {
            return;
        }
        
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                params
        );
        
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .put(body)
                .build();
        
        client.newCall(request).enqueue(responseHandler);
    }

    /**
     * DELETE request - Legacy compatibility
     */
    public static void delete(String url, Callback responseHandler) {
        if (offlineMode) {
            return;
        }
        
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .delete()
                .build();
        
        client.newCall(request).enqueue(responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.d(TAG, String.format(Locale.ENGLISH, "http://%s:%s/api/", IP_ADDRESS, PORT) + relativeUrl);
        return String.format(Locale.ENGLISH, "http://%s:%s/api/", IP_ADDRESS, PORT) + relativeUrl;
    }

    public static void setIp(String ip) {
        IP_ADDRESS = ip;
    }

    /**
     * Shows a Toast message on the main UI thread.
     * Use this method when showing Toast from background threads (e.g., OkHttp callbacks).
     *
     * @param context The context to use for the Toast
     * @param message The message to display
     * @param duration The duration of the Toast (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     */
    public static void showToast(Context context, String message, int duration) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, duration).show()
        );
    }
}
