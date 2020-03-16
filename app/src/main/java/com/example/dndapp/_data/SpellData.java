package com.example.dndapp._data;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SpellData {
    private final int level;
    private final String name;
    private final int phb;
    private final int id;

    private final String castingTime;
    private final String higherLevel;
    private final String duration;
    private final String components;
    private final String description;

    public String getCastingTime() {
        return castingTime;
    }

    public String getHigherLevel() {
        return higherLevel;
    }

    public String getDuration() {
        return duration;
    }

    public String getComponents() {
        return components;
    }

    public String getDescription() {
        return description;
    }

    public SpellData(JSONObject obj, boolean simple) throws JSONException {
        this.level = obj.getInt("level");
        this.phb = obj.getInt("phb_page");
        this.id = obj.getInt("id");
        this.name = obj.getString("name");

        if (simple) {
            this.castingTime = "null";
            this.higherLevel = "null";
            this.description = "null";
            this.components = "null";
            this.duration = "null";
        } else {
            this.castingTime = obj.getString("casting_time");
            this.higherLevel = obj.getString("higher_level");
            this.description = obj.getString("description");
            this.components = obj.getString("components");
            this.duration = obj.getString("duration");
        }
    }

    public SpellData(JSONObject obj) throws JSONException {
        this(obj, false);
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

        this.castingTime = "null";
        this.higherLevel = "null";
        this.description = "null";
        this.components = "null";
        this.duration = "null";
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
