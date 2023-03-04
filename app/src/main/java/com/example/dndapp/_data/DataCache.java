package com.example.dndapp._data;

import com.example.dndapp._data.classinfo.MainClassInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class DataCache {
    public static PlayerData selectedPlayer = new PlayerData();
    public static ArrayList<PlayerData> playerData = new ArrayList<>();
    public static HashMap<Integer, MainClassInfo> availableClasses = new HashMap<>();

    public static void setPlayerData(JSONArray array) throws JSONException {
        playerData.clear();
        for (int i = 0; i < array.length(); i++) {
            playerData.add(new PlayerData(array.getJSONObject(i)));
        }
    }

    public static PlayerData getPlayer(int id) {
        for (PlayerData player : playerData) {
            if (player.getId() == id) return player;
        }
        return null;
    }

    public static MainClassInfo getClass(int id) {
        for (MainClassInfo availableClass : DataCache.availableClasses.values()) {
            if (availableClass.getId() == id) return availableClass;
        }
        return null;
    }
}
