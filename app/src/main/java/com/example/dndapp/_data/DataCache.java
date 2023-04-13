package com.example.dndapp._data;

import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._data.classinfo.SubClassInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class DataCache {
    public static PlayerData selectedPlayer = new PlayerData();
    public static ArrayList<PlayerData> playerData = new ArrayList<>();
    public static HashMap<Integer, MainClassInfo> availableClasses = new HashMap<>();
    public static HashMap<Integer, SubClassInfo> availableSubClasses = new HashMap<>();

    public static void setPlayerData(JSONArray array) throws JSONException {
        playerData.clear();
        for (int i = 0; i < array.length(); i++) {
            playerData.add(new PlayerData(array.getJSONObject(i)));
        }
    }

    /**
     * Returns either the player from the list, or a new player if this player id does not exist in the list.
     */
    public static PlayerData getPlayer(int id) {
        for (PlayerData player : playerData) {
            if (player.getId() == id) return player;
        }

        if (playerData.size() > 0)
            return playerData.get(0);

        return new PlayerData();
    }

    public static void updatePlayer(PlayerData newPlayer) {
        for (int i = 0; i < playerData.size(); i++) {
            PlayerData player = playerData.get(i);
            if (player.getId() == newPlayer.getId()) {
                playerData.set(i, newPlayer);
                return;
            }
        }

        // This player didnt exist yet
        playerData.add(newPlayer);
    }


    public static ArrayList<PlayerData> getPlayers(int campaignId) {
        ArrayList<PlayerData> data = new ArrayList<>();

        for (PlayerData player : playerData) {
            if (player.campaignId == campaignId)
                data.add(player);
        }

        return data;
    }

    public static MainClassInfo getClass(int id) {
        for (MainClassInfo availableClass : DataCache.availableClasses.values()) {
            if (availableClass.getId() == id) return availableClass;
        }
        return null;
    }
}
