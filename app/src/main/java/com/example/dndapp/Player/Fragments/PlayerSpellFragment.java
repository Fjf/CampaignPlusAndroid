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
import com.example.dndapp._data.SpellData;

import java.io.UnsupportedEncodingException;

import static com.example.dndapp.Player.PlayerInfoActivity.psdDataSet;
import static com.example.dndapp.Player.PlayerInfoActivity.selectedSpellId;

public class PlayerSpellFragment extends android.app.Fragment {
    private static final String TAG = "PlayerSpellFragment";

    private View view;

    public PlayerSpellFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_spell, container, false);

        setSpellData();

        return view;
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
