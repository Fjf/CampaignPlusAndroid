package com.example.dndapp._data.classinfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SubClassInfo {
    private final String mainClassName;
    private int id;
    private int mainClassId;
    private String name;
    private ArrayList<ClassAbility> abilities;

    public int getId() {
        return id;
    }

    public int getMainClassId() {
        return mainClassId;
    }

    public String getName() {
        return name;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public ArrayList<ClassAbility> getAbilities() {
        return abilities;
    }

    public SubClassInfo(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.mainClassName = obj.getString("main_class_name");
        this.mainClassId = obj.getInt("main_class_id");

        JSONArray jsonArray = obj.getJSONArray("abilities");
        abilities = new ArrayList<>();

        // Create ability objects for every element in the json array.
        for (int i = 0; i < jsonArray.length(); i++) {
            abilities.add(new ClassAbility(jsonArray.getJSONObject(i), this));
        }
    }
}
