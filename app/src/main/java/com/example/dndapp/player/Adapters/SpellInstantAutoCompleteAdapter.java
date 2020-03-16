package com.example.dndapp.player.Adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.SpellData;

import java.util.ArrayList;

public class SpellInstantAutoCompleteAdapter extends ArrayAdapter {
    private final Activity context;
    private final ArrayList<SpellData> objects;
    private final int resource;

    public SpellInstantAutoCompleteAdapter(@NonNull Activity context, @NonNull ArrayList<SpellData> objects) {
        super(context, R.layout.spell_selection_row, objects);

        this.resource = R.layout.spell_selection_row;
        this.context = context;
        this.objects = objects;
    }


    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        rowView = inflater.inflate(resource, null,true);

        TextView nameTextField = (TextView) rowView.findViewById(R.id.instant_spell_name);
        TextView levelTextField = (TextView) rowView.findViewById(R.id.instant_spell_level);
        TextView idTextField = (TextView) rowView.findViewById(R.id.instant_spell_id);

        SpellData obj = (SpellData) getItem(position);
        assert obj != null;

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(obj.getName());
        levelTextField.setText(Integer.toString(obj.getLevel()));
        idTextField.setText(Integer.toString(obj.getId()));

        return rowView;
    }

}
