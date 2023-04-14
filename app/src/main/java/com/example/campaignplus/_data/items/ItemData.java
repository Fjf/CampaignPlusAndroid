package com.example.campaignplus._data.items;

import org.json.JSONException;
import org.json.JSONObject;

public class ItemData {

    private int id;
    private int weight = 0;
    private int value = 0;
    private String name;
    private String category;

    private ItemType type = ItemType.ITEM;

    private String dice = "";
    private String damageType = "";
    private String description = "";
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

    private ItemType getItemType(String category) {
        if (category.equals("Weapon"))
            return ItemType.WEAPON;
        if (category.equals("Armor"))
            return ItemType.ARMOR;
        if (category.equals("Adventuring Gear"))
            return ItemType.GEAR;
        if (category.equals("Tools"))
            return ItemType.TOOLS;
        if (category.equals("Mounts and Vehicles"))
            return ItemType.MOUNT;

        // Fallback.
        return ItemType.ITEM;
    }

    public ItemData(JSONObject itemJson) throws JSONException {
        /*
         * General information about this type of item
         * All specific Weapon stats are loaded etc.
         */
        this.id = itemJson.getInt("id");
        this.name = itemJson.getString("name");
        this.weight = itemJson.getInt("weight");
        this.value = itemJson.getInt("raw_value");
        this.category = itemJson.getString("category");
        this.description = itemJson.getString("description");

        ItemType type = getItemType(this.category);

        this.type = type;


        JSONObject itemInfo = itemJson.getJSONObject("item_info");
        if (type == ItemType.WEAPON) {
            if (!itemInfo.isNull("dice")) {
                this.dice = itemInfo.getString("dice");
                this.damageType = itemInfo.getString("damage_type");
                this.damageBonus = itemInfo.getInt("damage_bonus");
            }

            // Make sure 2h damage exists.
            if (!itemInfo.isNull("2h_dice")) {
                this.twoDice = itemInfo.getString("2h_dice");
                this.twoDamageType = itemInfo.getString("2h_damage_type");
                this.twoDamageBonus = itemInfo.getInt("2h_damage_bonus");
            }

            this.rangeNormal = getIfNotNullString(itemInfo, "range_normal");
            this.rangeLong = getIfNotNullString(itemInfo, "range_long");

            this.throwRangeNormal = getIfNotNullString(itemInfo, "throw_range_normal");
            this.throwRangeLong = getIfNotNullString(itemInfo, "throw_range_long");
        }
    }

    private String getIfNotNullString(final JSONObject json, final String key) {
        return json.isNull(key) ? null : json.optString(key);
    }

    public int getPhb() {
        int phb = 145;
        return phb;
    }

    public String getDescription() {
        return description;
    }
}
