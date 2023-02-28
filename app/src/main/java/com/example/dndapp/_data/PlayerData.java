package com.example.dndapp._data;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.util.Log;
import android.widget.Toast;

import com.example.dndapp._data.classinfo.ClassAbility;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._data.classinfo.SubClassInfo;
import com.example.dndapp._utils.HttpUtils;
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
    private int id;
    private String userName;

    private String name;
    private String className;
    private String race;

    private String backstory = "";

    public PlayerStatsData statsData = null;
    public PlayerProficiencyData proficiencies = null;

    private ArrayList<MainClassInfo> mainClassInfos = new ArrayList<>();
    private SubClassInfo[] subClassInfos = null;

    public int getId() {
        return id;
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

    public List<MainClassInfo> getMainClassInfos() {
        return mainClassInfos;
    }

    public ArrayList<ClassAbility> getAllAbilities() {
        ArrayList<ClassAbility> arrayList = new ArrayList<>();

        for (MainClassInfo mainClassInfo : mainClassInfos) {
            arrayList.addAll(mainClassInfo.getAbilities());
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

    public PlayerData(@NonNull int id, String name, String race) {
        this.id = id;
        this.name = name;
        this.race = race;
        this.statsData = new PlayerStatsData();
    }

    public PlayerData(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.userName = obj.getString("owner");
        this.race = obj.getString("race");

        // Find the class with the correct id from the available classes.
        if (!obj.isNull("class_ids")) {
            JSONArray ids = obj.getJSONArray("class_ids");
            for (int i = 0; i < ids.length(); i++) {
                int id = ids.getInt(i);

                // Locate class from list and add to this PlayerData.
                MainClassInfo mci = MyPlayerCharacterList.findClass(id);
                if (mci == null) {
                    System.err.println("Unable to find class with id; " + id);
                    continue;
                }
                this.mainClassInfos.add(mci);
            }
        }

        if (!obj.isNull("backstory")) {
            this.backstory = obj.getString("backstory");
        }

        if (!obj.isNull("stats")) {
            this.statsData = new PlayerStatsData(obj.getJSONObject("stats"));
        }

        if (!obj.isNull("proficiencies")) {
            this.proficiencies = new PlayerProficiencyData(obj.getJSONObject("proficiencies"));
        }
    }

    public void addMainClasses(List<MainClassInfo> mcis) {
        mainClassInfos.addAll(mcis);
    }

    public void setMainClasses(List<MainClassInfo> mcis) {
        mainClassInfos.clear();
        addMainClasses(mcis);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", this.name);

        // Fill array with all ids for this user.
        int[] classIds = new int[mainClassInfos.size()];
        for (int i = 0; i < mainClassInfos.size(); i++) {
            classIds[i] = mainClassInfos.get(i).getId();
        }

        obj.put("id", this.id);
        obj.put("race", this.race);
        obj.put("backstory", this.getBackstory());

        JSONObject infoObj = new JSONObject();
        infoObj.put("stats", this.statsData.toJSON());
        infoObj.put("proficiencies", this.proficiencies.toJSON());
        infoObj.put("class_ids", new JSONArray(classIds));

        obj.put("info", infoObj);

        return obj;
    }

    private void setMainClassInfos(JSONArray response) throws JSONException {
        for (int i = 0; i < response.length(); i++) {
            mainClassInfos.add(new MainClassInfo(response.getJSONObject(i)));
        }
    }

    public void updateMainClassInfos(final Context context, final FunctionCall call) {
        String url = String.format(Locale.ENGLISH, "player/%d/classes", this.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    setMainClassInfos(response);
                    call.success();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, "Something went wrong retrieving main class information from the server.", Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void setSubClassInfos(JSONObject response) throws JSONException {
        JSONArray jsonArray = response.getJSONArray("class");
        for (int i = 0; i < jsonArray.length(); i++) {
            subClassInfos[i] = new SubClassInfo(jsonArray.getJSONObject(i));
        }
    }

    public void updateSubClassInfos(final Context context, final FunctionCall call) {
        String url = String.format(Locale.ENGLISH, "/player/%d/subclasses", this.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    setSubClassInfos(response);
                    call.success();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, "Something went wrong retrieving subclass information from the server.", Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
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



