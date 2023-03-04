package com.example.dndapp.campaign.Adapters;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.dndapp.player.PlayerInfoActivity;
import com.example.dndapp.R;
import com.example.dndapp._data.PlayerData;

import java.util.ArrayList;

public class PlayerListAdapter extends ArrayAdapter {
    private final Activity context;
    private final ArrayList<PlayerData> entries;
    private final String playerName;

    public PlayerListAdapter(Activity context, ArrayList<PlayerData> entries, String playerName) {
        super(context, R.layout.playerview_row, entries);

        this.context = context;
        this.playerName = playerName;
        this.entries = entries;
    }

    @Override
    public View getView(final int position, View rowView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (rowView == null) {
            rowView = inflater.inflate(R.layout.playerview_row, null,true);
        }

        final PlayerData pae = entries.get(position);

        if (pae.getUserName().equals(playerName)) {
            Button sheet = rowView.findViewById(R.id.characterSheet);
            sheet.setVisibility(View.VISIBLE);
            rowView.findViewById(R.id.userName).setVisibility(View.GONE);

            sheet.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlayerInfoActivity.class);
                intent.putExtra("player_id", pae.getId());
                context.startActivity(intent);
                context.finish();
            });
        } else {
            rowView.findViewById(R.id.characterSheet).setVisibility(View.GONE);
            rowView.findViewById(R.id.userName).setVisibility(View.VISIBLE);
        }

        //this code gets references to objects in the campaign_listview_row.xmlview_row.xml file
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
