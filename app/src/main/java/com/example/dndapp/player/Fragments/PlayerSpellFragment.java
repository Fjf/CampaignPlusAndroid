package com.example.dndapp.player.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._data.SpellData;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import static com.example.dndapp.player.PlayerInfoActivity.psdDataSet;
import static com.example.dndapp.player.PlayerInfoActivity.selectedSpellId;

public class PlayerSpellFragment extends
        Fragment {
    private static final String TAG = "PlayerSpellFragment";

    private View view;

    public PlayerSpellFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_spell, container, false);

        registerSwipeRightExit(view.findViewById(R.id.fragment_player_spell));

        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        tb.setTitle("Player Spell Overview");
        registerExitFragmentButton(tb);

        setSpellData();

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
                            x1 - x2 > 100) {
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


    private void fillSpellData(SpellData current) {
        TextView ct = view.findViewById(R.id.spell_info_casting_time);
        TextView hl = view.findViewById(R.id.spell_info_higher_level);
        TextView de = view.findViewById(R.id.spell_info_description);
        TextView co = view.findViewById(R.id.spell_info_components);
        TextView du = view.findViewById(R.id.spell_info_duration);
        TextView le = view.findViewById(R.id.spell_info_level);

        String description = "null";
        try {
            description = new String(current.getDescription().getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ct.setText(current.getCastingTime());
        hl.setText(current.getHigherLevel());
        de.setText(description);
        co.setText(current.getComponents());
        du.setText(current.getDuration());
        le.setText(Integer.toString(current.getLevel()));
    }

    public void setSpellData() {
        // Default spell information is the first entry
        SpellData current = psdDataSet[selectedSpellId];

        createSpellDropdown();

        fillSpellData(current);
    }

    private void createSpellDropdown() {
        String[] users = new String[psdDataSet.length];
        for (int i = 0; i < psdDataSet.length; i++) {
            users[i] = psdDataSet[i].getName();
        }

        Spinner spin = view.findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_big_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(selectedSpellId);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillSpellData(psdDataSet[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
