package com.example.campaignplus._data;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.campaignplus._data.classinfo.ClassAbility;
import com.example.campaignplus._data.classinfo.MainClassInfo;
import com.example.campaignplus._data.classinfo.SubClassInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class DataCache {
    public static PlayerData selectedPlayer = new PlayerData();
    public static CampaignData selectedCampaign = new CampaignData();
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


    public static ArrayList<ClassAbility> getAllAbilities(ArrayList<Integer> mainClassIds, ArrayList<Integer> subClassIds) {
        ArrayList<ClassAbility> arrayList = new ArrayList<>();
        for (Integer id : mainClassIds) {
            // Locate class from list and add to this PlayerData.
            MainClassInfo mci = DataCache.getClass(id);
            if (mci == null) {
                continue;
            }
            arrayList.addAll(mci.getAbilities());
        }
        for (Integer id : subClassIds) {
            SubClassInfo sci = DataCache.availableSubClasses.get(id);
            if (sci == null) {
                continue;
            }
            arrayList.addAll(sci.getAbilities());
        }

        return arrayList;
    }
    /**
     * Gets the abilities sorted by level for a given mainclass and subclass id.
     * If either of those are irrelevant, they can be set to -1.
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static ArrayList<ClassAbility> getSortedAbilities(ArrayList<Integer> mainClassIds, ArrayList<Integer> subClassIds) {
        ArrayList<ClassAbility> abilities = getAllAbilities(mainClassIds, subClassIds);

        abilities.sort(Comparator.comparingInt(ClassAbility::getLevel));
        return abilities;
    }
}
