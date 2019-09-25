package com.example.dndapp._data;

import org.json.JSONException;
import org.json.JSONObject;

public class StatsData {
    private int dexterity;
    private int strength;
    private int constitution;
    private int wisdom;
    private int intelligence;
    private int charisma;

    public String getDexterity() {
        return String.valueOf(dexterity);
    }

    public String getStrength() {
        return String.valueOf(strength);
    }

    public String getConstitution() {
        return String.valueOf(constitution);
    }

    public String getWisdom() {
        return String.valueOf(wisdom);
    }

    public String getIntelligence() {
        return String.valueOf(intelligence);
    }

    public String getCharisma() {
        return String.valueOf(charisma);
    }

    public StatsData(JSONObject obj) throws JSONException {
        this.dexterity = obj.getInt("dexterity");
        this.constitution = obj.getInt("constitution");
        this.wisdom = obj.getInt("wisdom");
        this.strength = obj.getInt("strength");
        this.intelligence = obj.getInt("intelligence");
        this.charisma = obj.getInt("charisma");
    }
}
