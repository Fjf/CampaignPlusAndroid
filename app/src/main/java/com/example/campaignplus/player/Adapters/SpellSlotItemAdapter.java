package com.example.campaignplus.player.Adapters;

import static android.content.Context.MODE_PRIVATE;
import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.classinfo.MainClassInfo;

import java.util.ArrayList;
import java.util.Objects;

public class SpellSlotItemAdapter extends ArrayAdapter {

    private final int resource;
    private final Activity context;
    private final ArrayList<ArrayList<String>> aggregation;
    private final ArrayList<String> keyList;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public SpellSlotItemAdapter(Activity context, int resource, ArrayList<ArrayList<String>> aggregation, ArrayList<String> keyList) {
        super(context, resource, keyList);
        this.context = context;
        this.resource = resource;
        this.aggregation = aggregation;
        this.keyList = keyList;

        preferences = context.getSharedPreferences("PlayerData", MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static SpellSlotItemAdapter newInstance(Activity context, int resource) {

        // Count the amount of available items.
        ArrayList<ArrayList<String>> aggregation = new ArrayList<>();
        ArrayList<String> keyList = new ArrayList<>();
        for (int i : selectedPlayer.mainClassIds) {
            MainClassInfo info = DataCache.availableClasses.get(i);

            keyList.addAll(info.getTableKeys());
            aggregation.addAll(info.getTableValues());
        }

        return new SpellSlotItemAdapter(context, resource, aggregation, keyList);
    }

    public void resetCounters() {
        final int level = Integer.parseInt(selectedPlayer.statsData.getLevel()) - 1;
        for (int i = 0; i < keyList.size(); i++) {
            editor.putString(keyList.get(i), String.valueOf(aggregation.get(i).get(level)));
            editor.apply();
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        rowView = inflater.inflate(resource, null, true);

        TextView textView = rowView.findViewById(R.id.table_info_title);
        LinearLayout itemWrapper = rowView.findViewById(R.id.table_item_group);
        TextView itemTextView = itemWrapper.findViewById(R.id.table_item);
        Button counterButton = rowView.findViewById(R.id.table_counter);

        Object obj = getItem(position);
        assert obj != null;

        final String itemName = this.keyList.get(position);
        final int level = Integer.parseInt(selectedPlayer.statsData.getLevel());
        final int index = Math.min(level, this.aggregation.get(position).size()) - 1;
        final String strValue = this.aggregation.get(position).get(index);

        textView.setText(itemName);
        itemTextView.setText(strValue);

        // Get stored value from previous sessions
        String storedValueStr = preferences.getString(itemName, strValue);
        counterButton.setText(storedValueStr);

        try {
            // Try to parseint, if it doesnt work, this cannot become a button
            Integer.parseInt(strValue);

            counterButton.setOnClickListener(v -> {
                // Load the preferences storage to save player's current exhaustion.

                int storedValue = Integer.parseInt(Objects.requireNonNull(preferences.getString(itemName, strValue)));
                int newValue = Math.max(storedValue - 1, 0);
                counterButton.setText(String.valueOf(newValue));

                editor.putString(itemName, String.valueOf(newValue));
                editor.apply();
            });
        } catch (NumberFormatException e) {
            // If this row doesnt contain a number, we can skip putting the button here.
            counterButton.setVisibility(View.GONE);
        }
        return rowView;
    }
}
