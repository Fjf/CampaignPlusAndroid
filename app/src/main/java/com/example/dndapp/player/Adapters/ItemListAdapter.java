package com.example.dndapp.player.Adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.items.ItemData;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {
    private ItemData[] pidDataSet;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView itemName;
        public TextView itemAmount;
        public ItemViewHolder(View v) {
            super(v);
            itemName = v.findViewById(R.id.playerlist_item_name);
            itemAmount = v.findViewById(R.id.playerlist_item_amount);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ItemListAdapter(ItemData[] pidDataSet) {
        this.pidDataSet = pidDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_item, parent, false);

        ItemViewHolder vh = new ItemViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.itemName.setText(pidDataSet[position].getName());
        holder.itemAmount.setText(String.valueOf(pidDataSet[position].getAmount()));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return pidDataSet.length;
    }

}