package com.example.campaignplus._utils;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayerItemData {
    private String name;
    private String extraInfo;
    private int amount;

    private String damageType = null;
    private int diceAmount = 0;
    private int diceType = 0;
    private int flatDamage = 0;

    public PlayerItemData(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.extraInfo = "";
        this.amount = obj.getInt("amount");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDamageType() {
        return damageType;
    }

    public void setDamageType(String damageType) {
        this.damageType = damageType;
    }

    public int getDiceAmount() {
        return diceAmount;
    }

    public void setDiceAmount(int diceAmount) {
        this.diceAmount = diceAmount;
    }

    public int getDiceType() {
        return diceType;
    }

    public void setDiceType(int diceType) {
        this.diceType = diceType;
    }

    public int getFlatDamage() {
        return flatDamage;
    }

    public void setFlatDamage(int flatDamage) {
        this.flatDamage = flatDamage;
    }

    public PlayerItemData(String name, String extraInfo, int amount) {
        this.name = name;
        this.extraInfo = extraInfo;
        this.amount = amount;
    }

    public PlayerItemData(String name, String extraInfo, int amount, String damageType, int diceAmount, int diceType, int flatDamage) {
        this.name = name;
        this.extraInfo = extraInfo;
        this.amount = amount;

        this.damageType = damageType;
        this.diceAmount = diceAmount;
        this.diceType = diceType;
        this.flatDamage = flatDamage;
    }

    @Override
    public String toString() {
        return name + " : " + amount;
    }
}
