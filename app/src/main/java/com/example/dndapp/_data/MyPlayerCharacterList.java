package com.example.dndapp._data;

import android.util.Log;

import androidx.arch.core.util.Function;

import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MyPlayerCharacterList {
    private static final String TAG = "MyPlayerCharacterList";
    public static ArrayList<PlayerData> playerData = new ArrayList<>();
    public static ArrayList<MainClassInfo> availableClasses;

    // This variable tracks whether or not the initial request has gone through yet.
    public static boolean hasInitialized = false;

    public MyPlayerCharacterList () { /* Nothing for static class */ }

    public static void cleanArray() {
        playerData = new ArrayList<>();
    }

    public static void initialize(final FunctionCall fn, boolean force) {
        if (hasInitialized && !force) {
            fn.success();
            return;
        }

        // Load iteratively
        updateClassData(new FunctionCall() {
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

    public static void initialize(final FunctionCall fn) {
        initialize(fn, false);
    }

    public static void setPlayerData(JSONArray array) throws JSONException {
        playerData = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            playerData.add(new PlayerData(array.getJSONObject(i)));
        }
    }

    public static void updatePlayerData(final FunctionCall fn) {
        Log.d(TAG, "Loading player classes");
        HttpUtils.get("user/players", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    setPlayerData(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (fn != null)
                    fn.success();
            }
        });
    }

    public static void updateClassData(final FunctionCall fn) {
        HttpUtils.get("user/classes", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray classes) {
                try {
                    // Create class object for all incoming classes.
                    availableClasses = new ArrayList<>();
                    for (int i = 0; i < classes.length(); i++) {
                        availableClasses.add(new MainClassInfo(classes.getJSONObject(i)));
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

    public static PlayerData getPlayer(int playerId) {
        for (PlayerData pd : playerData) {
            if (pd.getId() == playerId)
                return pd;
        }
        return null;
    }

    public static MainClassInfo findClass(int id) {
        for (MainClassInfo availableClass : availableClasses) {
            if (availableClass.getId() == id)
                return availableClass;
        }
        return null;
    }

    public static PlayerData emptyPlayer() {
        return new PlayerData(-1, "", "");
    }
}
