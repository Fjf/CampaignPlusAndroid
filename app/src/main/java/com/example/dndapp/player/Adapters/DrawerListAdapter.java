package com.example.dndapp.player.Adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.DrawerListData;

import java.util.ArrayList;

public class DrawerListAdapter extends ArrayAdapter {

    private final Activity context;
    private final int resource;
    private final ArrayList<DrawerListData> objects;

    public DrawerListAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<DrawerListData> objects) {
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
        ImageView imageView = rowView.findViewById(R.id.icon);

        Object obj = getItem(position);
        assert obj != null;

        DrawerListData data = (DrawerListData) obj;
        textView.setText(data.getTitle());
        imageView.setImageDrawable(data.getIcon());

        return rowView;
    }
}
