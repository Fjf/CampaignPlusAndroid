package com.example.dndapp._data.items;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemData {
    private int id;

    private int amount = 0;
    private int weight = 0;
    private int value = 0;
    private String name;
    private String category;

    private ItemType type = ItemType.ITEM;

    private String dice = "";
    private String damageType = "";
    private int damageBonus = 0;

    private String twoDice = "";
    private String twoDamageType = "";
    private int twoDamageBonus = 0;

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
        return dice;
    }

    public String getNormalTwoDamage() {
        return twoDice;
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

        if (!obj.isNull("amount")) {
            this.amount = obj.getInt("amount");
        } else {
            this.amount = 0;
        }

        this.weight = obj.getInt("weight");
        this.value = obj.getInt("value");
        this.category = obj.getString("category");

        this.type = type;

        if (type == ItemType.WEAPON) {
            if (!obj.isNull("dice")) {
                this.dice = obj.getString("dice");
                this.damageType = obj.getString("damage_type");
                this.damageBonus = obj.getInt("damage_bonus");
            }

            // Make sure 2h damage exists.
            if (!obj.isNull("2h_dice")) {
                this.twoDice = obj.getString("2h_dice");
                this.twoDamageType = obj.getString("2h_damage_type");
                this.twoDamageBonus = obj.getInt("2h_damage_bonus");
            }

            this.rangeNormal = getIfNotNullString(obj, "range_normal");
            this.rangeLong = getIfNotNullString(obj, "range_long");

            this.throwRangeNormal = getIfNotNullString(obj, "throw_range_normal");
            this.throwRangeLong = getIfNotNullString(obj, "throw_range_long");
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
