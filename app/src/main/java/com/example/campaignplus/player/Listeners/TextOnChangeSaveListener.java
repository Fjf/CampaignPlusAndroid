package com.example.campaignplus.player.Listeners;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class TextOnChangeSaveListener implements View.OnKeyListener {
    private final String entry;
    SharedPreferences preferences;

    public TextOnChangeSaveListener(SharedPreferences preferences, String entry) {
        this.preferences = preferences;
        this.entry = entry;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        EditText et = (EditText) v;
        String value = et.getText().toString();

        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(entry, value);
        edit.apply();
        return false;
    }
}
