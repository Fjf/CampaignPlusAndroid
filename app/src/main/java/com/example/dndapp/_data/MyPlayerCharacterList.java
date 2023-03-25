package com.example.dndapp._data;

import android.util.Log;

import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._data.classinfo.SubClassInfo;
import com.example.dndapp._utils.CallBack;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MyPlayerCharacterList {
    private static final String TAG = "MyPlayerCharacterList";

    // This variable tracks whether or not the initial request has gone through yet.
    public static boolean hasInitialized = false;

    public MyPlayerCharacterList() { /* Nothing for static class */ }

    public static void initialize(final CallBack fn, boolean force) {
        if (hasInitialized && !force) {
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
                fn.error(errorMessage);
            }
        });
        hasInitialized = true;
    }

    public static void initialize(final CallBack fn) {
        initialize(fn, false);
    }

    public static void updatePlayerData(final CallBack fn) {
        Log.d(TAG, "Loading player classes");
        HttpUtils.get("user/players", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    DataCache.setPlayerData(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (fn != null)
                    fn.success();
            }
        });
    }

    public static void updateClassData(final CallBack fn) {
        HttpUtils.get("user/classes", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray classes) {
                try {
                    // Create class object for all incoming classes.
                    DataCache.availableClasses.clear();
                    for (int i = 0; i < classes.length(); i++) {
                        MainClassInfo obj = new MainClassInfo(classes.getJSONObject(i));
                        DataCache.availableClasses.put(obj.getId(), obj);
                    }

                    updateSubClassData(fn);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    fn.error(errorResponse.getString("error"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void updateSubClassData(final CallBack fn) {
        HttpUtils.get("user/subclasses", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray classes) {
                try {
                    // Create class object for all incoming classes.
                    DataCache.availableSubClasses.clear();
                    for (int i = 0; i < classes.length(); i++) {
                        SubClassInfo obj = new SubClassInfo(classes.getJSONObject(i));
                        DataCache.availableSubClasses.put(obj.getId(), obj);
                    }
                    fn.success();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    fn.error(errorResponse.getString("error"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static PlayerData emptyPlayer() {
        return new PlayerData();
    }
}
