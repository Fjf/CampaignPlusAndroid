package com.example.dndapp._data;

import android.graphics.drawable.Drawable;

public class DrawerListData {
    private Drawable icon;
    private String title;

    public Drawable getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public DrawerListData(String title, Drawable icon) {
        this.icon = icon;
        this.title = title;
    }
}
