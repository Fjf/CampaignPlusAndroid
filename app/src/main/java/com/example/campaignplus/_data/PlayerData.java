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
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

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

    public void upload(final CallBack func) throws JSONException, UnsupportedEncodingException {
        String url = String.format("player/%s", selectedPlayer.getId());
        StringEntity entity = new StringEntity(selectedPlayer.toJSON().toString(), Charset.defaultCharset());
        // Upload changed data to the server.
        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                func.success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                func.error(response.toString());
            }
        });
    }

    @NonNull
    @Override
    public String toString() {
        return getName() + " / " + getClassName() + " / " + getRace();
    }

}



