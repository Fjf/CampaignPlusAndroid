package com.example.dndapp.player.Fragments;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.items.ItemData;
import com.example.dndapp._data.items.ItemType;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;

import java.util.Objects;

import static com.example.dndapp.player.PlayerInfoActivity.pidDataSet;
import static com.example.dndapp.player.PlayerInfoActivity.selectedItemId;

public class PlayerItemFragment extends Fragment {
    private String TAG = "PlayerItemActivity";

    private View view;

    public PlayerItemFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_item, container, false);

        registerSwipeRightExit(view.findViewById(R.id.fragment_player_item));

        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        tb.setTitle("Player Item Overview");
        registerExitFragmentButton(tb);

        getItems();

        return view;
    }

    private void registerSwipeRightExit(View viewById) {
        viewById.setOnTouchListener(new View.OnTouchListener() {
            private float x1;
            private float y1;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    x1 = motionEvent.getX();
                    y1 = motionEvent.getY();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    float x2 = motionEvent.getX();
                    float y2 = motionEvent.getY();

                    // Only activate when not moving down much, but moving a lot horizontally.
                    if (Math.abs(y1 - y2) < 100 &&
                            x2 - x1 > 100) {
                        // Close fragment
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                    }
                }

                return false;
            }
        });
    }

    private void registerExitFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.close_fragment_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove current fragment
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
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
