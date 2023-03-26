package com.example.dndapp._data;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SpellData {
    public final int level;
    public final String name;
    public final int id;

    public final String castingTime;
    public final String higherLevel;
    public final String duration;
    public final String components;
    public final String range;
    public final boolean concentration;
    public final boolean ritual;
    public final String school;
    public final String material;
    public final String description;

    public SpellData(JSONObject obj, boolean simple) throws JSONException {
        this.level = obj.getInt("level");
        this.id = obj.getInt("id");
        this.name = obj.getString("name");

        this.duration = obj.getString("duration");
        this.higherLevel = obj.getString("higher_level");
        this.castingTime = obj.getString("casting_time");

        this.concentration = obj.optBoolean("concentration", false);
        this.ritual = obj.optBoolean("ritual", false);

        this.material = obj.optString("material", "None");

        this.components = obj.getString("components");
        this.range = obj.getString("spell_range");
        this.description = obj.getString("description");
        this.school = obj.optString("school", "None");
    }

    public SpellData(JSONObject obj) throws JSONException {
        this(obj, false);
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
