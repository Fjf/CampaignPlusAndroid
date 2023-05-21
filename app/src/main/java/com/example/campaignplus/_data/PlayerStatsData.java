package com.example.campaignplus._data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PlayerStatsData {
    private int speed = 0;
    private int armorClass = 0;
    private int maxHP = 0;
    private int level = 0;

    public void setDexterity(int dexterity) {
        this.dexterity = fixStatInputRange(dexterity);
    }


    public void setStrength(int strength) {
        this.strength = fixStatInputRange(strength);
    }

    public void setConstitution(int constitution) {
        this.constitution = fixStatInputRange(constitution);
    }

    public void setWisdom(int wisdom) {
        this.wisdom = fixStatInputRange(wisdom);
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = fixStatInputRange(intelligence);
    }

    public void setCharisma(int charisma) {
        this.charisma = fixStatInputRange(charisma);
    }

    private int fixStatInputRange(int stat) {
        return Math.max(Math.min(stat, 30), 1);
    }

    private int dexterity = 0;
    private int strength = 0;
    private int constitution = 0;
    private int wisdom = 0;
    private int intelligence = 0;
    private int charisma = 0;

    public boolean dexSave = false;
    public boolean conSave = false;
    public boolean wisSave = false;
    public boolean strSave = false;
    public boolean intSave = false;
    public boolean chaSave = false;

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public void setLevel(int level) {
        this.level = Math.max(level, 1);
    }

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

    public String getIntelligenceModifier() {
        return getModifier(intelligence);
    }

    public String getDexterityModifier() {
        return getModifier(dexterity);
    }

    public String getStrengthModifier() {
        return getModifier(strength);
    }

    public String getConstitutionModifier() {
        return getModifier(constitution);
    }

    public String getCharismaModifier() {
        return getModifier(charisma);
    }

    public String getWisdomModifier() {
        return getModifier(wisdom);
    }

    public PlayerStatsData(JSONObject obj) throws JSONException {
        this.dexterity = obj.getInt("dexterity");
        this.constitution = obj.getInt("constitution");
        this.wisdom = obj.getInt("wisdom");
        this.strength = obj.getInt("strength");
        this.intelligence = obj.getInt("intelligence");
        this.charisma = obj.getInt("charisma");

        this.dexSave = obj.getBoolean("saving_throws_dex");
        this.conSave = obj.getBoolean("saving_throws_con");
        this.wisSave = obj.getBoolean("saving_throws_wis");
        this.strSave = obj.getBoolean("saving_throws_str");
        this.intSave = obj.getBoolean("saving_throws_int");
        this.chaSave = obj.getBoolean("saving_throws_cha");

        this.speed = obj.getInt("speed");
        this.armorClass = obj.getInt("armor_class");
        this.maxHP = obj.getInt("max_hp");
        this.level = obj.getInt("level");

        if (this.level < 1) {
            this.level = 1;
            Log.w("PlayerStatsData", "Invalid level passed, autocorrecting to 1. This should be handled server side.");
        }
    }

    /**
     * Default constructor
     */
    public PlayerStatsData() { }

    private String getModifier(int value) {
        int val = getIntModifier(value);
        return formatBonus(val);
    }

    private String formatBonus(int val) {
        if (val >= 0)
            return String.format(Locale.ENGLISH, "+%d", val);
        else
            return String.format(Locale.ENGLISH, "%d", val);
    }

    private int getIntModifier(int value) {
        return value / 2 - 5;
    }

    public String getLevel() {
        return String.valueOf(level);
    }

    public String getMaxHP() {
        return String.valueOf(maxHP);
    }

    public String getArmorClass() {
        return String.valueOf(armorClass);
    }

    public String getSpeed() {
        return String.valueOf(speed);
    }

    public int getProficiencyModifier() {
        return (this.level - 1) / 4 + 2;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("dexterity", this.dexterity);
        obj.put("constitution", this.constitution);
        obj.put("wisdom", this.wisdom);
        obj.put("strength", this.strength);
        obj.put("intelligence", this.intelligence);
        obj.put("charisma", this.charisma);

        obj.put("saving_throws_dex", this.dexSave);
        obj.put("saving_throws_con", this.conSave);
        obj.put("saving_throws_wis", this.wisSave);
        obj.put("saving_throws_str", this.strSave);
        obj.put("saving_throws_int", this.intSave);
        obj.put("saving_throws_cha", this.chaSave);

        obj.put("speed", this.speed);
        obj.put("armor_class", this.armorClass);
        obj.put("max_hp", this.maxHP);
        obj.put("level", this.level);

        return obj;
    }

    public String getSavingThrowsCha() {
        int val = (chaSave ? getProficiencyModifier() : 0) + getIntModifier(charisma);
        return formatBonus(val);
    }

    public String getSavingThrowsInt() {
        int val = (intSave ? getProficiencyModifier() : 0) + getIntModifier(intelligence);
        return formatBonus(val);
    }

    public String getSavingThrowsDex() {
        int val = (dexSave ? getProficiencyModifier() : 0) + getIntModifier(dexterity);
        return formatBonus(val);
    }

    public String getSavingThrowsStr() {
        int val = (strSave ? getProficiencyModifier() : 0) + getIntModifier(strength);
        return formatBonus(val);
    }

    public String getSavingThrowsCon() {
        int val = (conSave ? getProficiencyModifier() : 0) + getIntModifier(constitution);
        return formatBonus(val);
    }

    public String getSavingThrowsWis() {
        int val = (wisSave ? getProficiencyModifier() : 0) + getIntModifier(wisdom);
        return formatBonus(val);
    }
}
