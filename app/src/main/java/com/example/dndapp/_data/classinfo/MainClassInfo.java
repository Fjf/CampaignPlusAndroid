package com.example.dndapp._data.classinfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainClassInfo {
    private int id;

    private String name;
    private final String info;
    private ArrayList<ClassAbility> abilities;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public ArrayList<ClassAbility> getAbilities() {
        return abilities;
    }

    public MainClassInfo(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.info = obj.getString("info");

        JSONArray jsonArray = obj.getJSONArray("abilities");
        abilities = new ArrayList<>();

        // Create ability objects for every element in the json array.
        for (int i = 0; i < jsonArray.length(); i++) {
            abilities.add(new ClassAbility(jsonArray.getJSONObject(i), this));
        }
    }
}
