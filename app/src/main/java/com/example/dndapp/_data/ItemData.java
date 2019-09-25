package com.example.dndapp._data;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemData {
    private int id;

    private int amount = 0;
    private int weight = 0;
    private int value = 0;
    private String name;

    private ItemType type = ItemType.ITEM;

    private String diceType = "";
    private String diceAmount = "";
    private String damageType = "";
    private String rangeNormal = "";
    private String rangeLong = "";
    private String throwRangeNormal = "";
    private String throwRangeLong = "";

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public String getNormalValue() {
        if (value > 10000) {
            return (value / 10000) + " gp";
        } else if (value > 100) {
            return (value / 100) + " sp";
        } else {
            return value + " cp";
        }
    }

    public int getWeight() {
        return weight;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getDiceType() {
        return diceType;
    }

    public String getDiceAmount() {
        return diceAmount;
    }

    public String getDamageType() {
        return damageType;
    }

    public String getRangeNormal() {
        return rangeNormal;
    }

    public String getRangeLong() {
        return rangeLong;
    }

    public String getThrowRangeNormal() {
        return throwRangeNormal;
    }

    public String getThrowRangeLong() {
        return throwRangeLong;
    }

    public ItemType getType() {
        return type;
    }

    public String getNormalDamage() {
        return diceAmount + "d" + diceType;
    }

    public String getNormalRange() {
        if (rangeLong == null)
            return rangeNormal;
        else
            return rangeNormal + " - " + rangeLong;
    }

    public String getNormalThrowRange() {
        if (throwRangeNormal == null)
            return "Unthrowable";
        return throwRangeNormal + " - " + throwRangeLong;
    }

    public ItemData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public ItemData(JSONObject obj, ItemType type) throws JSONException {
        this.id = obj.getInt("id");

        this.name = obj.getString("name");
        this.amount = obj.getInt("amount");
        this.weight = obj.getInt("weight");
        this.value = obj.getInt("value");

        this.type = type;

        if (type == ItemType.WEAPON) {
            this.diceAmount = obj.getString("dice_amount");
            this.diceType = obj.getString("dice_type");
            this.damageType = obj.getString("damage_type");

            this.rangeNormal = obj.getString("range_normal");
            this.rangeLong = getIfNotNullString(obj, "range_long");

            this.throwRangeNormal = getIfNotNullString(obj,"throw_range_normal");
            this.throwRangeLong = getIfNotNullString(obj,"throw_range_long");
        }
    }

    private String getIfNotNullString(final JSONObject json, final String key) {
        return json.isNull(key) ? null : json.optString(key);
    }

    public int getPhb() {
        int phb = 145;
        return phb;
    }
}
