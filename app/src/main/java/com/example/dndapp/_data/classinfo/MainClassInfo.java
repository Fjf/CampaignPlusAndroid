package com.example.dndapp._data.classinfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainClassInfo {
    private int id = -1;

    private String name = "";
    private final ArrayList<ClassAbility> abilities;

    private final ArrayList<String> tableKeys = new ArrayList<>();
    private final ArrayList<ArrayList<String>> tableValues = new ArrayList<>();

    public ArrayList<ArrayList<String>> getTableValues() {
        return tableValues;
    }
    public ArrayList<String> getTableKeys() {
        return tableKeys;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ClassAbility> getAbilities() {
        return abilities;
    }

    public MainClassInfo(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");

        tableValues.clear();

        // Iterate table values.
        JSONArray table_array = obj.getJSONArray("table_values");

        for (int i = 0; i < table_array.length(); i++) {
            JSONObject item = table_array.getJSONObject(i);
            String key = item.keys().next();
            JSONArray jData = (JSONArray) item.get(key);

            // Create arraylist from json array.
            ArrayList<String> data = new ArrayList<>();
            for (int j = 0; j < jData.length(); j++) {
                data.add(jData.getString(j));
            }
            tableKeys.add(key);
            tableValues.add(data);
        }

        JSONArray jsonArray = obj.getJSONArray("abilities");
        abilities = new ArrayList<>();

        // Create ability objects for every element in the json array.
        for (int i = 0; i < jsonArray.length(); i++) {
            abilities.add(new ClassAbility(jsonArray.getJSONObject(i), this));
        }
    }
}
