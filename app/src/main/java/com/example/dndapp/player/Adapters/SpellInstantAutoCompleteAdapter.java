package com.example.dndapp.player.Adapters;

import android.app.Activity;

import androidx.annotation.NonNull;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.SpellData;

import java.util.ArrayList;

public class SpellInstantAutoCompleteAdapter extends ArrayAdapter<SpellData> {
    private final String TAG = "SpellInstantAutoCompleteAdapter";
    private final Context context;
    private final ArrayList<SpellData> filteredSpells;
    private final ArrayList<SpellData> originalSpells;
    private final int resource;

    public SpellInstantAutoCompleteAdapter(@NonNull Context context, @NonNull ArrayList<SpellData> spells) {
        super(context, R.layout.spell_selection_row, spells);

        this.resource = R.layout.spell_selection_row;
        this.filteredSpells = new ArrayList<>(spells);
        this.originalSpells = spells;
        this.context = context;
    }



    public int getCount() {
        return filteredSpells.size();
    }

    public SpellData getItem(int position) {
        return filteredSpells.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rowView = inflater.inflate(resource, null, true);

        TextView nameTextField = rowView.findViewById(R.id.instant_spell_name);
        TextView levelTextField = rowView.findViewById(R.id.instant_spell_level);
        TextView idTextField = rowView.findViewById(R.id.instant_spell_id);

        SpellData obj = getItem(position);
        assert obj != null;

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(obj.name);
        levelTextField.setText(Integer.toString(obj.level));
        idTextField.setText(Integer.toString(obj.id));

        return rowView;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                return ((SpellData) resultValue).name;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filteredSpells.clear();
                if (constraint == null) {
                    constraint = "";
                }
                for (SpellData spell : originalSpells) {
                    if (spell.name.toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        filteredSpells.add(spell);
                    }
                }
                FilterResults filterResults = new FilterResults();

                filterResults.values = filteredSpells;
                filterResults.count = filteredSpells.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
