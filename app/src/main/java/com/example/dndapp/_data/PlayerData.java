package com.example.dndapp._data;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.StringEntity;

public class PlayerData {
    private int id;
    private String userName;

    private String name;
    private String className;
    private String race;

    private String backstory = "";

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

    public PlayerData(int id, String name, String className, String race) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.race = race;
    }

    public PlayerData(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.userName = obj.getString("user_name");
        this.id = obj.getInt("id");
        this.className = obj.getString("class");
        this.backstory = getIfNotNullString(obj, "backstory");
        this.race = obj.getString("race");
    }

    private String getIfNotNullString(final JSONObject json, final String key) {
        return json.isNull(key) ? null : json.optString(key);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", this.name);
        obj.put("class", this.className);
        obj.put("id", this.id);
        obj.put("race", this.race);
        obj.put("backstory", this.getBackstory());

        return obj;
    }
}
