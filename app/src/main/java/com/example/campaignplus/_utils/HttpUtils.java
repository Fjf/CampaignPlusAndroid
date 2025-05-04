package com.example.campaignplus._utils;

import android.content.Context;
import android.util.Log;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class PersistentCookieJar implements CookieJar {
    private final SharedPreferences sharedPreferences;
    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    public PersistentCookieJar(Context context) {
        sharedPreferences = context.getSharedPreferences("cookies", Context.MODE_PRIVATE);
        loadAll();
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cookieStore.put(url.host(), cookies);
        saveToPrefs(url.host(), cookies);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
    }

    private void saveToPrefs(String host, List<Cookie> cookies) {
        JSONArray jsonArray = new JSONArray();
        for (Cookie cookie : cookies) {
            jsonArray.put(encodeCookie(cookie));
        }
        sharedPreferences.edit().putString(host, jsonArray.toString()).apply();
    }

    private void loadAll() {
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            List<Cookie> cookies = new ArrayList<>();
            try {
                JSONArray array = new JSONArray((String) entry.getValue());
                for (int i = 0; i < array.length(); i++) {
                    Cookie cookie = decodeCookie(array.getString(i));
                    if (cookie != null) cookies.add(cookie);
                }
                cookieStore.put(entry.getKey(), cookies);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeCookie(Cookie cookie) {
        JSONObject json = new JSONObject();
        try {
            json.put("name", cookie.name());
            json.put("value", cookie.value());
            json.put("expiresAt", cookie.expiresAt());
            json.put("domain", cookie.domain());
            json.put("path", cookie.path());
            json.put("secure", cookie.secure());
            json.put("httpOnly", cookie.httpOnly());
            json.put("hostOnly", cookie.hostOnly());
            json.put("persistent", cookie.persistent());
            return Base64.encodeToString(json.toString().getBytes(), Base64.NO_WRAP);
        } catch (JSONException e) {
            return null;
        }
    }

    private Cookie decodeCookie(String encoded) {
        try {
            String decoded = new String(Base64.decode(encoded, Base64.NO_WRAP));
            JSONObject json = new JSONObject(decoded);
            Cookie.Builder builder = new Cookie.Builder()
                    .name(json.getString("name"))
                    .value(json.getString("value"))
                    .expiresAt(json.getLong("expiresAt"))
                    .path(json.getString("path"));

            if (json.getBoolean("secure")) builder.secure();
            if (json.getBoolean("httpOnly")) builder.httpOnly();
            if (json.getBoolean("hostOnly")) {
                builder.hostOnlyDomain(json.getString("domain"));
            } else {
                builder.domain(json.getString("domain"));
            }

            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
        cookieStore.clear();
    }
}
class SessionCookieJar implements CookieJar {
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
}

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final String PORT = "5000";
    private static String IP_ADDRESS = "192.168.1.1";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .cookieJar(new SessionCookieJar())
                .connectTimeout(1, TimeUnit.DAYS)
                .readTimeout(1, TimeUnit.DAYS)
                .writeTimeout(1, TimeUnit.DAYS)
                .build();
    }

    public static String getUrl() {
        return "http://" + IP_ADDRESS + ":" + PORT;
    }

    public static void get(String url, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void post(String url, String json, okhttp3.Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void put(String url, String json, okhttp3.Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .put(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void delete(String url, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(getAbsoluteUrl(url))
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getByUrl(String url, okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void postByUrl(String url, String json, okhttp3.Callback callback) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.d(TAG, String.format("http://%s:%s/api/", IP_ADDRESS, PORT) + relativeUrl);
        return String.format("http://%s:%s/api/", IP_ADDRESS, PORT) + relativeUrl;
    }

    public static void setIp(String ip) {
        IP_ADDRESS = ip;
    }
}
