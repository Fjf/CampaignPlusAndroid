package com.example.campaignplus._data;

import static com.example.campaignplus._data.DataCache.availableSubClasses;
import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.campaignplus._data.classinfo.ClassAbility;
import com.example.campaignplus._data.classinfo.MainClassInfo;
import com.example.campaignplus._data.classinfo.SubClassInfo;
import com.example.campaignplus._data.items.EquipmentItem;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PlayerData {
    private static final String TAG = "PlayerData";
    private int id = -1;
    private String userName = "";
    public int campaignId;

    public ArrayList<SpellData> getSpells() {
        return spells;
    }

    public void addSpell(SpellData spell) {
        this.spells.add(spell);
    }

    public void updateSpells(final CallBack callback) {
        String url = String.format(Locale.ENGLISH, "player/%s/spells", selectedPlayer.getId());
        HttpUtils.get(url, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray newSpells = new JSONArray(responseBody);
                        setSpells(newSpells);
                        callback.success();
                    } catch (JSONException e) {
                        callback.error(e.getMessage());
                    }
                } else {
                    callback.error(response.message());
                }
            }
        });

    }

    public void setSpells(JSONArray newSpells) throws JSONException {
        spells.clear();
        for (int i = 0; i < newSpells.length(); i++) {
            JSONObject obj = newSpells.getJSONObject(i);
            spells.add(new SpellData(obj));
        }
    }

    private ArrayList<SpellData> spells = new ArrayList<>();
    private String name;

    public int gold;
    public int silver;
    public int copper;

    private String race;

    private String backstory = "";
    public PlayerStatsData statsData = new PlayerStatsData();

    public PlayerProficiencyData proficiencies = new PlayerProficiencyData();
    public final ArrayList<Integer> mainClassIds = new ArrayList<>();
    public final ArrayList<Integer> subClassIds = new ArrayList<>();

    public final ArrayList<EquipmentItem> equipment = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return "";
    }

    public String getRace() {
        return race;
    }

    public String getUserName() {
        return userName;
    }

    public String getBackstory() {
        return backstory == null ? "" : backstory;
    }

    public void setBackstory(String backstory) {
        this.backstory = backstory;
    }

    public SubClassInfo getSubclassForMainclass(MainClassInfo info) {
        for (int id : subClassIds) {
            SubClassInfo subclass = availableSubClasses.get(id);
            assert subclass != null;
            if (Objects.equals(subclass.mainClassName, info.getName()))
                return availableSubClasses.get(id);
        }
        return null;
    }

    public ArrayList<ClassAbility> getAllAbilities() {
        ArrayList<ClassAbility> arrayList = new ArrayList<>();
        for (Integer id : mainClassIds) {
            // Locate class from list and add to this PlayerData.
            MainClassInfo mci = DataCache.getClass(id);
            if (mci == null) {
                Log.d(TAG, "Unable to find class with id; " + id);
                continue;
            }
            arrayList.addAll(mci.getAbilities());
        }
        for (Integer id : subClassIds) {
            SubClassInfo sci = DataCache.availableSubClasses.get(id);
            if (sci == null) {
                Log.d(TAG, "Unable to find subclass with id; " + id);
                continue;
            }
            arrayList.addAll(sci.getAbilities());
        }

        return arrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<ClassAbility> getSortedAbilities() {
        ArrayList<ClassAbility> abilities = getAllAbilities();

        abilities.sort(Comparator.comparingInt(ClassAbility::getLevel));
        return abilities;
    }

    public PlayerData() {
    }

    public PlayerData(JSONObject obj) throws JSONException {
        this.setData(obj);
    }

    public void setData(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.userName = obj.getString("owner");
        this.race = obj.getString("race");
        this.campaignId = obj.optInt("campaign_id", -1);

        this.gold = obj.getInt("gold");
        this.silver = obj.getInt("silver");
        this.copper = obj.getInt("copper");

            this.backstory = obj.optString("backstory", "");

        JSONObject info = obj.getJSONObject("info");
        this.statsData = new PlayerStatsData(info.getJSONObject("stats"));
        this.proficiencies = new PlayerProficiencyData(info.getJSONObject("proficiencies"));
        JSONArray ids = info.getJSONArray("class_ids");
        for (int i = 0; i < ids.length(); i++) {
            int id = ids.getInt(i);
            this.mainClassIds.add(id);
        }
        ids = info.getJSONArray("subclass_ids");
        for (int i = 0; i < ids.length(); i++) {
            int id = ids.getInt(i);
            this.subClassIds.add(id);
        }
    }

    public void setMainClassIds(List<Integer> mcis) {
        mainClassIds.clear();
        mainClassIds.addAll(mcis);
    }

    public void setSubClassIds(List<Integer> scis) {
        subClassIds.clear();
        subClassIds.addAll(scis);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", this.name);

        // Fill array with all ids for this user.
        obj.put("id", this.id);
        obj.put("race", this.race);
        obj.put("backstory", this.getBackstory());

        JSONObject infoObj = new JSONObject();
        infoObj.put("stats", this.statsData.toJSON());
        infoObj.put("proficiencies", this.proficiencies.toJSON());
        infoObj.put("class_ids", new JSONArray(mainClassIds));
        infoObj.put("subclass_ids", new JSONArray(subClassIds));

        obj.put("info", infoObj);

        JSONObject money = new JSONObject();
        money.put("gold", this.gold);
        money.put("silver", this.silver);
        money.put("copper", this.copper);
        money.put("electron", 0);
        money.put("platinum", 0);
        obj.put("money", money);

        return obj;
    }

    /**
     * This function will try to update the PlayerData object with data retrieved from the server.
     * On success, it will call the supplied FunctionCall.success() callback.
     * On failure, the FunctionCall.error(errorMessage);
     * If the player has an invalid ID (non existent), it will instantly return with success().
     *
     * @param func The callback function
     */
    public void updatePlayerData(final CallBack func) {
        // Cannot update a player with an invalid id.
        if (this.getId() == -1)
            func.success();

        String url = String.format(Locale.ENGLISH, "player/%d", this.getId());
        HttpUtils.get(url, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                func.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject info = new JSONObject(responseBody).getJSONObject("info");
                        statsData = new PlayerStatsData(info.getJSONObject("stats"));
                        proficiencies = new PlayerProficiencyData(info.getJSONObject("proficiencies"));
                        func.success();
                    } catch (JSONException e) {
                        func.error(e.getMessage());
                    }
                } else {
                    func.error(response.message());
                }
            }
        });
    }

    public void getEquipment(final CallBack func) {
        String url = String.format(Locale.ENGLISH, "player/%s/items", selectedPlayer.getId());
        HttpUtils.get(url, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                func.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray items = new JSONArray(responseBody);
                        selectedPlayer.equipment.clear();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject obj = items.getJSONObject(i);
                            selectedPlayer.equipment.add(new EquipmentItem(obj));
                        }
                        func.success();
                    } catch (JSONException e) {
                        func.error(e.getMessage());
                    }
                } else {
                    func.error(response.message());
                }
            }
        });
    }

    public void upload(final CallBack func) throws JSONException {
        String url = String.format("player/%s", selectedPlayer.getId());
        String json = selectedPlayer.toJSON().toString();
        HttpUtils.put(url, json, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                func.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    func.success();
                } else {
                    func.error(response.message());
                }
            }
        });

    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " / " + getClassName() + " / " + getRace();
    }

}



