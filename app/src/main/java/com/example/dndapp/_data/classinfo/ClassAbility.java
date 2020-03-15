package com.example.dndapp._data.classinfo;

import org.json.JSONException;
import org.json.JSONObject;

public class ClassAbility {
    private int id;
    private int level;

    private SubClassInfo subClass = null;
    private MainClassInfo mainClass = null;
    private String name;
    private String info;

    public SubClassInfo getSubClass() {
        return subClass;
    }

    public MainClassInfo getMainClass() {
        return mainClass;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public ClassAbility(JSONObject obj, MainClassInfo mainClass) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.info = obj.getString("info").trim();
        this.level = obj.getInt("level");

        this.mainClass = mainClass;
    }

    public ClassAbility(JSONObject obj, SubClassInfo subClass) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.info = obj.getString("info").trim();
        this.level = obj.getInt("level");

        this.subClass = subClass;
    }
}
