package com.example.dndapp._data;

import static com.example.dndapp._data.DataCache.selectedPlayer;

import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.dndapp.R;
import com.example.dndapp._data.classinfo.ClassAbility;
import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._data.classinfo.SubClassInfo;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.player.Adapters.SpellListAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class PlayerData {
    private static final String TAG = "PlayerData";
    private int id = -1;
    private String userName = "";

    public ArrayList<SpellData> getSpells() {
        return spells;
    }

    public void addSpell(SpellData spell) {
        this.spells.add(spell);
    }

    public void updateSpells(final FunctionCall callback) {
        String url = String.format(Locale.ENGLISH, "player/%s/spells", selectedPlayer.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray newSpells) {
                try {
                    setSpells(newSpells);
                    callback.success();
                } catch (JSONException e) {
                    callback.error(e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                callback.error(response);
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

    private ArrayList<SpellData> spells = new ArrayList<SpellData>();
    private String name;
    private String className;

    private String race;

    private String backstory = "";
    public PlayerStatsData statsData = null;

    public PlayerProficiencyData proficiencies = new PlayerProficiencyData();
    private final ArrayList<Integer> mainClassIds = new ArrayList<>();

    private final SubClassInfo[] subClassInfos = null;
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
        return className;
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

    public List<Integer> getMainClassIds() {
        return mainClassIds;
    }

    public ArrayList<ClassAbility> getAllAbilities() {
        ArrayList<ClassAbility> arrayList = new ArrayList<>();
        Log.d("--------------------", "Getting alla biltiies");
        for (Integer id : mainClassIds) {
            Log.d("--------------------", String.valueOf(id));
            // Locate class from list and add to this PlayerData.
            MainClassInfo mci = DataCache.getClass(id);
            if (mci == null) {
                Log.d(TAG, "Unable to find class with id; " + id);
                continue;
            }
            arrayList.addAll(mci.getAbilities());
        }

        return arrayList;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<ClassAbility> getSortedAbilities() {
        ArrayList<ClassAbility> abilities = getAllAbilities();

        abilities.sort(Comparator.comparingInt(ClassAbility::getLevel));
        return abilities;
    }

    public SubClassInfo[] getSubClassInfos() {
        return subClassInfos;
    }
    public PlayerData() { }
    public PlayerData(JSONObject obj) throws JSONException {
        this.setData(obj);
    }

    public void setData(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.userName = obj.getString("owner");
        this.race = obj.getString("race");


        if (!obj.isNull("backstory")) {
            this.backstory = obj.getString("backstory");
        }

        JSONObject info = obj.getJSONObject("info");
        this.statsData = new PlayerStatsData(info.getJSONObject("stats"));
        this.proficiencies = new PlayerProficiencyData(info.getJSONObject("proficiencies"));
        JSONArray ids = info.getJSONArray("class_ids");
        for (int i = 0; i < ids.length(); i++) {
            int id = ids.getInt(i);
            this.mainClassIds.add(id);
        }
    }

    public void addMainClassIds(List<Integer> mcis) {
        mainClassIds.addAll(mcis);
    }

    public void setMainClassIds(List<Integer> mcis) {
        mainClassIds.clear();
        addMainClassIds(mcis);
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

        obj.put("info", infoObj);

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
    public void updatePlayerData(final FunctionCall func) {
        // Cannot update a player with an invalid id.
        if (this.getId() == -1)
            func.success();

        Log.d("---------------------Fetching", String.valueOf(this.getId()));

        String url = String.format(Locale.ENGLISH, "player/%d", this.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject info = response.getJSONObject("info");
                    statsData = new PlayerStatsData(info.getJSONObject("stats"));
                    proficiencies = new PlayerProficiencyData(info.getJSONObject("proficiencies"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    func.error(e.getMessage());
                    return;
                }
                func.success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("updatePlayerData::HttpUtils::get()", "Response failure.");
                func.error(response);
            }
        });
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " / " + getClassName() + " / " + getRace();
    }

}



