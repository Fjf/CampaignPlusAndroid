package com.example.campaignplus._data;

import org.json.JSONException;
import org.json.JSONObject;

public class Counter {
    private String name;
    private int maxValue;
    private int currentValue;
    private boolean isCustom;

    public Counter() {
        this.name = "";
        this.maxValue = 0;
        this.currentValue = 0;
        this.isCustom = false;
    }

    public Counter(String name, int maxValue, int currentValue) {
        this.name = name;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.isCustom = false;
    }

    public Counter(String name, int maxValue, int currentValue, boolean isCustom) {
        this.name = name;
        this.maxValue = maxValue;
        this.currentValue = currentValue;
        this.isCustom = isCustom;
    }

    public Counter(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.maxValue = obj.optInt("maxValue", 0);
        this.currentValue = obj.optInt("currentValue", 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public void increment() {
        if (currentValue < maxValue) {
            currentValue++;
        }
    }

    public void decrement() {
        if (currentValue > 0) {
            currentValue--;
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("maxValue", maxValue);
        obj.put("currentValue", currentValue);
        return obj;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    public static Counter fromJSON(JSONObject obj) throws JSONException {
        Counter counter = new Counter(obj);
        counter.isCustom = obj.optBoolean("isCustom", false);
        return counter;
    }
}
