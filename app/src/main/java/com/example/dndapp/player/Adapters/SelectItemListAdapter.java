package com.example.dndapp.player.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.dndapp.R;
import com.example.dndapp._data.items.ItemData;

import java.util.ArrayList;
import java.util.Locale;

public class SelectItemListAdapter extends ArrayAdapter {
    private final ArrayList<ItemData> items;
    private final Activity context;

    public SelectItemListAdapter(@NonNull Activity context, @NonNull ArrayList<ItemData> items) {
        super(context, 0, items);

        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        if (rowView == null)
            rowView = inflater.inflate(R.layout.select_item_row, null, true);

        // Store index for later data retrieval

        TextView name = rowView.findViewById(R.id.select_item_name);
        TextView value = rowView.findViewById(R.id.select_item_value);
        TextView weight = rowView.findViewById(R.id.select_item_weight);

        ItemData data = items.get(position);
        assert data != null;

        rowView.setId(data.getId());
        name.setText(data.getName());
        value.setText(data.getNormalValue());
        weight.setText(String.format(Locale.ENGLISH, "%d", data.getWeight()));

        return rowView;
    }
}
