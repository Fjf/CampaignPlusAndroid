package com.example.dndapp.Player.Adapters;

import android.app.Activity;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dndapp.Player.DrawerListData;
import com.example.dndapp.R;
import com.example.dndapp._data.ItemData;
import com.example.dndapp._data.SpellData;

import java.util.ArrayList;

public class DrawerListAdapter extends ArrayAdapter {

    private final Activity context;
    private final int resource;

    public DrawerListAdapter(@NonNull Activity context, int resource, @NonNull ArrayList<DrawerListData> objects) {
        super(context, resource, objects);

        this.resource = resource;
        this.context = context;
    }



    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        rowView = inflater.inflate(resource, null,true);

        TextView textView = rowView.findViewById(R.id.title);
        ImageView imageView = rowView.findViewById(R.id.icon);

        DrawerListData obj = (DrawerListData) getItem(position);
        assert obj != null;

        textView.setText(obj.getTitle());
        imageView.setImageDrawable(obj.getIcon());

        return rowView;
    }
}
