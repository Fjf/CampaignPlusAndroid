package com.example.campaignplus._data.classinfo;

import org.json.JSONException;
import org.json.JSONObject;

public class ClassAbility {
    private int level;

    private SubClassInfo subClass = null;
    private MainClassInfo mainClass = null;
    private String description;

    public SubClassInfo getSubClass() {
        return subClass;
    }

    public MainClassInfo getMainClass() {
        return mainClass;
    }


    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }

    public ClassAbility(JSONObject obj, MainClassInfo mainClass) throws JSONException {
        this.description = obj.getString("description").trim();
        this.level = obj.getInt("level");
        this.mainClass = mainClass;
    }

    public ClassAbility(JSONObject obj, SubClassInfo subClass) throws JSONException {
        this.level = obj.getInt("level");
        this.description = obj.getString("description").trim();
        this.subClass = subClass;
    }
}
