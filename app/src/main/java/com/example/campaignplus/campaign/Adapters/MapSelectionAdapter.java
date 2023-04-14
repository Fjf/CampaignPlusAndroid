package com.example.campaignplus.campaign.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.campaignplus.R;
import com.example.campaignplus.campaign.Listeners.OnItemClickListener;

import java.util.ArrayList;

public class MapSelectionAdapter extends RecyclerView.Adapter<MapSelectionAdapter.MapViewHolder> {
    private final ArrayList<String> mapNames;
    private final OnItemClickListener<String> listener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MapViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView name;

        public MapViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
        }

        public void bind(final String item, final OnItemClickListener<String> listener) {
            name.setText(item);
            name.setOnClickListener((View.OnClickListener) v -> listener.onItemClick(item));
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MapSelectionAdapter(ArrayList<String> mapNames, OnItemClickListener<String> listener) {
        this.listener = listener;
        this.mapNames = mapNames;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MapSelectionAdapter.MapViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.spinner_big_item, parent, false);
        return new MapSelectionAdapter.MapViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MapViewHolder holder, int position) {
        holder.bind(mapNames.get(position), listener);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mapNames.size();
    }

}
