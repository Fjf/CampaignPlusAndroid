package com.example.dndapp._data;

import android.content.Context;
import android.support.annotation.NonNull;
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

    private MainClassInfo[] mainClassInfos = null;
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

    public MainClassInfo[] getMainClassInfos() {
        return mainClassInfos;
    }

    public ArrayList<ClassAbility> getAllAbilities() {
        ArrayList<ClassAbility> arrayList = new ArrayList<>();

        for (MainClassInfo mainClassInfo : mainClassInfos) {
            arrayList.addAll(mainClassInfo.getAbilities());
        }

        return arrayList;
    }

    public SubClassInfo[] getSubClassInfos() {
        return subClassInfos;
    }

    public PlayerData(@NonNull int id, String name, String className, String race) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.race = race;
    }

    public PlayerData(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.userName = obj.getString("user_name");
        this.className = obj.getString("class");
        this.race = obj.getString("race");

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

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", this.name);
        obj.put("class", this.className);
        obj.put("id", this.id);
        obj.put("race", this.race);
        obj.put("backstory", this.getBackstory());

        obj.put("stats", this.statsData.toJSON());
        obj.put("proficiencies", this.proficiencies.toJSON());

        return obj;
    }

    private void setMainClassInfos(JSONObject response) throws JSONException {
        JSONArray jsonArray = response.getJSONArray("classes");
        mainClassInfos = new MainClassInfo[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            mainClassInfos[i] = new MainClassInfo(jsonArray.getJSONObject(i));
        }
    }

    public void updateMainClassInfos(final Context context, final FunctionCall call) {
        String url = String.format(Locale.ENGLISH, "player/%d/classes", this.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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

    public void updatePlayerData(int playerId, final FunctionCall func) {
        String url = String.format(Locale.ENGLISH, "player/%s/data", playerId);
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    statsData = new PlayerStatsData(response.getJSONObject("info"));
                    proficiencies = new PlayerProficiencyData(response.getJSONObject("proficiencies"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                func.success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
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



