package com.example.campaignplus.player.Adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.SpellData;

import java.util.ArrayList;

public class SpellListAdapter extends RecyclerView.Adapter<SpellListAdapter.ItemViewHolder> {
    private final ArrayList<SpellData> psdDataSet;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView spellName;
        public TextView spellLevel;
        public TextView spellPhb;
        public ItemViewHolder(View v) {
            super(v);
            spellName = v.findViewById(R.id.playerlist_spell_name);
            spellLevel = v.findViewById(R.id.playerlist_spell_level);
            spellPhb = v.findViewById(R.id.playerlist_spell_phb);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SpellListAdapter(ArrayList<SpellData> psdDataSet) {
        this.psdDataSet = psdDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_spell, parent, false);

        ItemViewHolder vh = new ItemViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.spellName.setText(psdDataSet.get(position).name);
        holder.spellLevel.setText(String.valueOf(psdDataSet.get(position).level));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return psdDataSet.size();
    }

}
