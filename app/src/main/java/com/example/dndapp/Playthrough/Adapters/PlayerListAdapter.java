package com.example.dndapp.Playthrough.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.Playthrough.PlayerArrayElement;
import com.example.dndapp.R;

import java.util.List;

public class PlayerListAdapter extends ArrayAdapter {
    private final Activity context;
    private final List<PlayerArrayElement> entries;

    public PlayerListAdapter(Activity context, List<PlayerArrayElement> entries) {
        super(context, R.layout.playerview_row, entries);

        this.context = context;
        this.entries = entries;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.playerview_row, null,true);

        PlayerArrayElement pae = entries.get(position);

        //this code gets references to objects in the listview_row.xml file
        TextView playerText = (TextView) rowView.findViewById(R.id.playerName);
        TextView classText = (TextView) rowView.findViewById(R.id.className);
        TextView userText = (TextView) rowView.findViewById(R.id.userName);

        //this code sets the values of the objects to values from the arrays
        userText.setText(pae.getUserName());
        playerText.setText(pae.getPlayerName());
        classText.setText(pae.getClassName());

        return rowView;

    };
}
