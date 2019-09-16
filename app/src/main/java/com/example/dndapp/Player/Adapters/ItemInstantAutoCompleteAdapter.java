package com.example.dndapp.Player.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.Item;

public class ItemInstantAutoCompleteAdapter extends ArrayAdapter {
    private final Activity context;
    private final Item[] objects;

    public ItemInstantAutoCompleteAdapter(@NonNull Activity context, @NonNull Item[] objects) {
        super(context, R.layout.item_selection_row, objects);

        this.context = context;
        this.objects = objects;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.item_selection_row, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.instant_item_name);
        TextView categoryTextField = (TextView) rowView.findViewById(R.id.instant_item_category);
        TextView idTextFields = (TextView) rowView.findViewById(R.id.instant_item_id);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(objects[position].getName());
        idTextFields.setText(Integer.toString(objects[position].getId()));
        categoryTextField.setText("Categorie");

        return rowView;
    };

}
