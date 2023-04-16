package com.example.campaignplus._data;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SpellData {
    public int level = 0;
    public String name = "";
    public int id = -1;

    public String castingTime = "";
    public String higherLevel = "";
    public String duration = "";
    public String components = "";
    public String range = "";
    public boolean concentration = false;
    public boolean ritual = false;
    public String school = "";
    public String material = "";
    public String description = "";

    public SpellData(JSONObject obj) throws JSONException {
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

    public SpellData() { };

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject response = new JSONObject();
        response.put("level", level);
        response.put("id", id);
        response.put("name", name);

        response.put("duration", duration);
        response.put("higher_level", higherLevel);
        response.put("casting_time", castingTime);

        response.put("concentration", concentration);
        response.put("ritual", ritual);

        response.put("material", material);

        response.put("components", components);
        response.put("spell_range", range);
        response.put("description", description);
        response.put("school", school);
        return response;
    }
}
