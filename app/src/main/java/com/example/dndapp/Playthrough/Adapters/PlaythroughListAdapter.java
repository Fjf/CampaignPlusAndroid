package com.example.dndapp.Playthrough.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;

public class PlaythroughListAdapter extends ArrayAdapter {
    private final Activity context;
    private final String[] names;

    public PlaythroughListAdapter(Activity context, String[] names) {
        super(context, R.layout.listview_row, names);

        this.context = context;
        this.names = names;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_row, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.playerName);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(names[position]);

        return rowView;

    };
}
