package com.example.dndapp._data;

import org.json.JSONArray;
import org.json.JSONException;

public class PlayerCharacterList {
    public static PlayerData[] playerData;

    public PlayerCharacterList () { /* Nothing for static class */ }

    public static void setPlayerData(JSONArray array) throws JSONException {

        playerData = new PlayerData[array.length()];
        for (int i = 0; i < array.length(); i++) {
            playerData[i] = new PlayerData(array.getJSONObject(i));
        }
    }
}
