package com.example.dndapp._data;

import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MyPlayerCharacterList {
    public static ArrayList<PlayerData> playerData = new ArrayList<>();

    public MyPlayerCharacterList () { /* Nothing for static class */ }

    public static void cleanArray() {
        playerData = new ArrayList<>();
    }

    public static void setPlayerData(JSONArray array) throws JSONException {
        playerData = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            playerData.add(new PlayerData(array.getJSONObject(i)));
        }
    }

    public static void updatePlayerData(final FunctionCall fn) {
        String url = "user/players";
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        setPlayerData(response.getJSONArray("players"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (fn != null)
                    fn.run();
            }
        });
    }
}
