package com.example.dndapp.Player.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.ItemData;
import com.example.dndapp._data.ItemType;

import static com.example.dndapp.Player.PlayerInfoActivity.pidDataSet;
import static com.example.dndapp.Player.PlayerInfoActivity.selectedItemId;

public class PlayerItemFragment extends android.app.Fragment {
    private String TAG = "PlayerItemActivity";

    private View view;

    public PlayerItemFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_item, container, false);

        getItems();

        return view;
    }

    private void getItems() {
        // Default spell information is the first entry
        System.out.println(selectedItemId);

        ItemData current = pidDataSet[selectedItemId];

        createItemDropdown();

        fillItemData(current);
    }

    private void createItemDropdown() {
        String[] itemNames = new String[pidDataSet.length];
        for (int i = 0; i < pidDataSet.length; i++) {
            itemNames[i] = pidDataSet[i].getName();
        }

        Spinner spin = view.findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_big_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(selectedItemId);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillItemData(pidDataSet[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void fillItemData(ItemData itemData) {
        TextView na = view.findViewById(R.id.item_info_value);
        TextView am = view.findViewById(R.id.item_info_amount);

        na.setText(itemData.getNormalValue());
        am.setText(String.valueOf(itemData.getAmount()));

        if (itemData.getType() == ItemType.WEAPON) {
            hideAllBut(R.id.item_weapon_info);

            TextView da = view.findViewById(R.id.weapon_info_damage);
            TextView dt = view.findViewById(R.id.weapon_info_damage_type);
            TextView ra = view.findViewById(R.id.weapon_info_range);
            TextView tr = view.findViewById(R.id.weapon_info_throw_range);

            da.setText(itemData.getNormalDamage());
            dt.setText(itemData.getDamageType());
            ra.setText(itemData.getNormalRange());
            tr.setText(itemData.getNormalThrowRange());
        } else {
            hideAllBut(0);
        }
    }

    private void hideAllBut(int showId) {
        int[] ids = new int[]{R.id.item_weapon_info};

        for (int id : ids) {
            if (id == showId)
                view.findViewById(id).setVisibility(View.VISIBLE);
            else
                view.findViewById(id).setVisibility(View.GONE);
        }
    }

}
