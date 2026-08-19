package com.example.campaignplus._data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.campaignplus._data.classinfo.MainClassInfo;
import com.example.campaignplus._data.classinfo.SubClassInfo;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyPlayerCharacterList {
    private static final String TAG = "MyPlayerCharacterList";

    // This variable tracks whether or not the initial request has gone through yet.
    public static boolean hasInitialized = false;

    public MyPlayerCharacterList() { /* Nothing for static class */ }

    /**
     * Helper method to post callbacks to the main thread.
     */
    private static void postToMainThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    /**
     * Helper method to get error message from response.
     */
    private static String getErrorMessage(Response response) {
        try {
            if (response.body() != null) {
                return "HTTP error: " + response.code() + " - " + response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "HTTP error: " + response.code();
    }

    public static void initialize(final CallBack fn) {
        if (hasInitialized) {
            fn.success();
            return;
        }

        // Load iteratively
        updateClassData(new CallBack() {
            @Override
            public void success() {
                updatePlayerData(fn);
            }

            @Override
            public void error(String errorMessage) {
                postToMainThread(() -> fn.error(errorMessage));
            }
        });
        hasInitialized = true;
    }

    public static void updatePlayerData(final CallBack fn) {
        HttpUtils.newGet("user/players", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed: " + e.getMessage());
                if (fn != null)
                    postToMainThread(() -> fn.error(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "Response received: " + response.isSuccessful() + " Body: " + (response.body() != null) + " Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "JSON Response: " + jsonResponse);
                        JSONArray array = new JSONArray(jsonResponse);
                        DataCache.setPlayerData(array);
                        if (fn != null)
                            postToMainThread(fn::success);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing player data: " + e.getMessage());
                        if (fn != null)
                            postToMainThread(() -> fn.error("Error parsing player data: " + e.getMessage()));
                    }
                } else {
                    final String errorMsg = getErrorMessage(response);
                    Log.e(TAG, errorMsg);
                    if (fn != null)
                        postToMainThread(() -> fn.error(errorMsg));
                }
            }
        });
    }

    public static void updateClassData(final CallBack fn) {
        HttpUtils.newGet("user/classes", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postToMainThread(() -> fn.error(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray classes = new JSONArray(jsonResponse);
                        // Create class object for all incoming classes.
                        DataCache.availableClasses.clear();
                        for (int i = 0; i < classes.length(); i++) {
                            MainClassInfo obj = new MainClassInfo(classes.getJSONObject(i));
                            DataCache.availableClasses.put(obj.getId(), obj);
                        }

                        updateSubClassData(fn);
                    } catch (Exception e) {
                        e.printStackTrace();
                        postToMainThread(() -> fn.error(e.getMessage()));
                    }
                } else {
                    try {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        JSONObject errorObj = new JSONObject(errorBody);
                        postToMainThread(() -> fn.error(errorObj.optString("error", errorBody)));
                    } catch (Exception e) {
                        postToMainThread(() -> fn.error("HTTP error: " + response.code()));
                    }
                }
            }
        });
    }

    public static void updateSubClassData(final CallBack fn) {
        HttpUtils.newGet("user/subclasses", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postToMainThread(() -> fn.error(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray classes = new JSONArray(jsonResponse);
                        // Create class object for all incoming classes.
                        DataCache.availableSubClasses.clear();
                        for (int i = 0; i < classes.length(); i++) {
                            SubClassInfo obj = new SubClassInfo(classes.getJSONObject(i));
                            DataCache.availableSubClasses.put(obj.getId(), obj);
                        }
                        postToMainThread(fn::success);
                    } catch (Exception e) {
                        e.printStackTrace();
                        postToMainThread(() -> fn.error(e.getMessage()));
                    }
                } else {
                    try {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        JSONObject errorObj = new JSONObject(errorBody);
                        postToMainThread(() -> fn.error(errorObj.optString("error", errorBody)));
                    } catch (Exception e) {
                        postToMainThread(() -> fn.error("HTTP error: " + response.code()));
                    }
                }
            }
        });
    }


    public static PlayerData emptyPlayer() {
        return new PlayerData();
    }
}
