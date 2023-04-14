package com.example.campaignplus._utils;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayerStatsData {
    private int strength;
    private int dexterity;
    private int constitution;
    private int wisdom;
    private int intelligence;
    private int charisma;

    private boolean stStrength;
    private boolean stDexterity;
    private boolean stConstitution;
    private boolean stWisdom;
    private boolean stIntelligence;
    private boolean stCharisma;

    public boolean isStStrength() {
        return stStrength;
    }

    public void setStStrength(boolean stStrength) {
        this.stStrength = stStrength;
    }

    public boolean isStDexterity() {
        return stDexterity;
    }

    public void setStDexterity(boolean stDexterity) {
        this.stDexterity = stDexterity;
    }

    public boolean isStConstitution() {
        return stConstitution;
    }

    public void setStConstitution(boolean stConstitution) {
        this.stConstitution = stConstitution;
    }

    public boolean isStWisdom() {
        return stWisdom;
    }

    public void setStWisdom(boolean stWisdom) {
        this.stWisdom = stWisdom;
    }

    public boolean isStIntelligence() {
        return stIntelligence;
    }

    public void setStIntelligence(boolean stIntelligence) {
        this.stIntelligence = stIntelligence;
    }

    public boolean isStCharisma() {
        return stCharisma;
    }

    public void setStCharisma(boolean stCharisma) {
        this.stCharisma = stCharisma;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    public int getConstitution() {
        return constitution;
    }

    public void setConstitution(int constitution) {
        this.constitution = constitution;
    }

    public int getWisdom() {
        return wisdom;
    }

    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getCharisma() {
        return charisma;
    }

    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    public PlayerStatsData(int strength, int dexterity, int constitution, int wisdom, int intelligence, int charisma) {
        this.charisma = charisma;
        this.strength = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.wisdom = wisdom;
        this.intelligence = intelligence;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();

        if (this.strength >= 0)
            object.put("strength", this.strength);
        if (this.dexterity >= 0)
            object.put("dexterity", this.dexterity);
        if (this.constitution >= 0)
            object.put("constitution", this.constitution);
        if (this.wisdom >= 0)
            object.put("wisdom", this.wisdom);
        if (this.intelligence >= 0)
            object.put("intelligence", this.intelligence);
        if (this.charisma >= 0)
            object.put("charisma", this.charisma);

        return object;
    }
}
