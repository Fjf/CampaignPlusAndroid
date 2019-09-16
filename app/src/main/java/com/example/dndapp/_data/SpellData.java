package com.example.dndapp._data;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SpellData {
    private final int level;
    private final String name;
    private final int phb;
    private final int id;

    public SpellData(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.level = obj.getInt("level");
        this.phb = obj.getInt("phb_page");
        this.id = obj.getInt("id");
    }


    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public int getPhb() {
        return phb;
    }

    public int getId() {
        return id;
    }

    public SpellData(String name, int level, int phb, int id) {
        this.name = name;
        this.level = level;
        this.phb = phb;
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
