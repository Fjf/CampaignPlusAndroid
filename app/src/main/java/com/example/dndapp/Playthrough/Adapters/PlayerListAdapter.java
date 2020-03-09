package com.example.dndapp.Playthrough.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.dndapp.Player.PlayerInfoActivity;
import com.example.dndapp.R;
import com.example.dndapp._data.PlayerData;

import static android.content.Context.MODE_PRIVATE;

public class PlayerListAdapter extends ArrayAdapter {
    private final Activity context;
    private final PlayerData[] entries;
    private final String playerName;

    public PlayerListAdapter(Activity context, PlayerData[] entries, String playerName) {
        super(context, R.layout.playerview_row, entries);

        this.context = context;
        this.playerName = playerName;
        this.entries = entries;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.playerview_row, null,true);

        final PlayerData pae = entries[position];

        if (pae.getUserName().equals(playerName)) {
            Button sheet = rowView.findViewById(R.id.characterSheet);
            sheet.setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.userName).setVisibility(View.GONE);

            sheet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PlayerInfoActivity.class);
                    intent.putExtra("player_id", pae.getId());
                    context.startActivity(intent);
                }
            });
        } else {
            rowView.findViewById(R.id.characterSheet).setVisibility(View.GONE);
            rowView.findViewById(R.id.userName).setVisibility(View.VISIBLE);
        }

        //this code gets references to objects in the playthrough_listview_row.xmlview_row.xml file
        TextView playerText = rowView.findViewById(R.id.playerName);
        TextView classText =  rowView.findViewById(R.id.className);
        TextView userText =   rowView.findViewById(R.id.userName);

        //this code sets the values of the objects to values from the arrays
        userText.setText(pae.getUserName());
        playerText.setText(pae.getName());
        classText.setText(pae.getClassName());

        return rowView;

    };
}
