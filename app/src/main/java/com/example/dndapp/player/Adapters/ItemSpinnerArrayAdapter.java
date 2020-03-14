package com.example.dndapp.player.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ItemSpinnerArrayAdapter extends ArrayAdapter {

    @Override
    public boolean isEnabled(int position){
        // Disable the first item from Spinner
        // First item will be use for hint
        return position != 0;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if (position == 0) {
            // Set the hint text color gray
            tv.setTextColor(Color.GRAY);
        } else {
            tv.setTextColor(Color.BLACK);
        }
        return view;
    }


    public ItemSpinnerArrayAdapter(@NonNull Context context, int resource, String[] dice) {
        super(context, resource, dice);
    }
}
