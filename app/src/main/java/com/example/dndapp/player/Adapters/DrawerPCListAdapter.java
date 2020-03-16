package com.example.dndapp.player.Adapters;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.PlayerData;

import java.util.ArrayList;

public class DrawerPCListAdapter extends ArrayAdapter {

    private final ArrayList<PlayerData> objects;
    private final int resource;
    private final Activity context;

    public DrawerPCListAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<PlayerData> objects) {
        super(context, resource, objects);

        this.objects = objects;
        this.resource = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        rowView = inflater.inflate(resource, null,true);

        TextView textView = rowView.findViewById(R.id.title);

        Object obj = getItem(position);
        assert obj != null;

        PlayerData data = (PlayerData) obj;
        textView.setText(data.getName());

        return rowView;
    }
}
