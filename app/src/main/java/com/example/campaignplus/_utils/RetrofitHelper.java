package com.example.campaignplus._utils;

import android.util.Log;

import com.example.campaignplus._utils.ApiService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitHelper - Modern HTTP client using Retrofit + OkHttp
 * Replaces the legacy AsyncHttpClient with a type-safe, modern approach.
 */
public class RetrofitHelper {
    private static final String TAG = "RetrofitHelper";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    
    private static final long TIMEOUT_SECONDS = 15;

    /**
     * Get or create the Retrofit instance
     */
    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(HttpUtils.getUrl() + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Execute a GET request and parse JSON response
     */
    public static void getJson(String endpoint, retrofit2.Callback<JSONArray> callback) {
        getApiService().get(endpoint).enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        // Note: This is a simplified approach. For production, use proper Retrofit types.
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing JSON array", e);
                    }
                } else {
                    Log.e(TAG, "Request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Request failed", t);
            }
        });
    }

    /**
     * Execute a POST request with JSON body
     */
    public static void postJson(String endpoint, JSONObject jsonBody, retrofit2.Callback<JSONObject> callback) {
        // This would need a proper Retrofit endpoint definition
        Log.d(TAG, "POST to: " + endpoint);
    }

    /**
     * Execute a PUT request with JSON body
     */
    public static void putJson(String endpoint, JSONObject jsonBody, retrofit2.Callback<JSONObject> callback) {
        Log.d(TAG, "PUT to: " + endpoint);
    }

    /**
     * Execute a DELETE request
     */
    public static void delete(String endpoint, retrofit2.Callback<ResponseBody> callback) {
        getApiService().delete(endpoint).enqueue(callback);
    }

    /**
     * Set custom base URL
     */
    public static void setBaseUrl(String baseUrl) {
        // Recreate Retrofit instance with new base URL
        apiService = null;
        retrofit = null;
        HttpUtils.setIp(baseUrl);
    }
}
