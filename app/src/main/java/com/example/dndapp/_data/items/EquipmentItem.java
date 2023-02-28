package com.example.dndapp._data.items;

import org.json.JSONException;
import org.json.JSONObject;

public class EquipmentItem {
    private int instanceId;

    private int amount = 0;

    private ItemData item;

    public int getAmount() {
        return amount;
    }

    public ItemData getItem() {
        return item;
    }

    public int getInstanceId() {
        return instanceId;
    }
    public EquipmentItem(JSONObject obj) throws JSONException {
        /*
         * Information about the specific item this user holds
         */
        this.instanceId = obj.getInt("id");

        if (!obj.isNull("amount")) {
            this.amount = obj.getInt("amount");
        } else {
            this.amount = 0;
        }
        this.item = new ItemData(obj.getJSONObject("info"));
    }

}
