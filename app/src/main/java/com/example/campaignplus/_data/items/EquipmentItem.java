package com.example.campaignplus._data.items;

import org.json.JSONException;
import org.json.JSONObject;

public class EquipmentItem {
    private int instanceId;

    private int amount = 0;

    private String description;
    private ItemData item;

    public void setAmount(int amount) {
        this.amount = amount;
    }

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
        this.description = obj.getString("description");
        this.item = new ItemData(obj.getJSONObject("info"));
    }

    public EquipmentItem(ItemData item) {
        this.item = item;
        this.description = "";
        this.amount = 1;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject response = new JSONObject();
        response.put("item_id", this.instanceId);
        response.put("amount", this.amount);
        response.put("description", this.description);
        return response;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
