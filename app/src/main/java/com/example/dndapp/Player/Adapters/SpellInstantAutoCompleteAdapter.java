package com.example.dndapp.Player.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.Item;
import com.example.dndapp._utils.PlayerSpellData;

public class SpellInstantAutoCompleteAdapter extends ArrayAdapter {
    private final Activity context;
    private final PlayerSpellData[] objects;

    public SpellInstantAutoCompleteAdapter(@NonNull Activity context, @NonNull PlayerSpellData[] objects) {
        super(context, R.layout.spell_selection_row, objects);

        this.context = context;
        this.objects = objects;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.spell_selection_row, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.instant_spell_name);
        TextView levelTextField = (TextView) rowView.findViewById(R.id.instant_spell_level);
        TextView idTextFields = (TextView) rowView.findViewById(R.id.instant_spell_id);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(objects[position].getName());
        levelTextField.setText(Integer.toString(objects[position].getLevel()));
        idTextFields.setText(objects[position].getId());

        return rowView;
    };

}
