package com.example.dndapp.Player.Listeners;

import android.content.SharedPreferences;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class TextOnChangeSaveListener implements View.OnKeyListener {
    private final String entry;
    SharedPreferences.Editor editor;

    public TextOnChangeSaveListener(SharedPreferences.Editor editor, String entry) {
        this.editor = editor;
        this.entry = entry;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        EditText et = (EditText) v;
        String value = et.getText().toString();

        editor.putString(entry, value);
        editor.apply();
        return false;
    }
}
