package com.example.campaignplus.player.Fragments;

import static com.example.campaignplus._data.DataCache.selectedPlayer;
import static com.example.campaignplus.player.MainFragments.SpellViewFragment.selectedSpellId;

import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.campaignplus.R;
import com.example.campaignplus._data.SpellData;
import com.example.campaignplus._utils.eventlisteners.ShortHapticFeedback;

import java.util.Objects;

public class SpellInfoFragment extends
        Fragment {
    private static final String TAG = "PlayerSpellFragment";

    private View view;

    public SpellInfoFragment() {
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
        btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        btn.setOnClickListener(view -> {
            // Remove current fragment
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
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

//        TextView rit = view.findViewById(R.id.spell_info_ritual);
//        TextView con = view.findViewById(R.id.spell_info_concentration);
        TextView mat = view.findViewById(R.id.spell_info_material);
        TextView ran = view.findViewById(R.id.spell_info_range);
        TextView sch = view.findViewById(R.id.spell_info_school);

        ct.setText(current.castingTime);
        hl.setText(current.higherLevel);
        de.setText(current.description);
        co.setText(current.components);
        du.setText(current.duration);
        le.setText(String.valueOf(current.level));

        mat.setText(current.material);
//        rit.setText(current.ritual ? "Yes" : "No");
//        con.setText(current.concentration  ? "Yes" : "No");
        ran.setText(current.range);
        sch.setText(current.school);
    }

    public void setSpellData() {
        // Default spell information is the first entry
        SpellData current = selectedPlayer.getSpells().get(selectedSpellId);

        createSpellDropdown();

        fillSpellData(current);
    }

    private void createSpellDropdown() {
        String[] users = new String[selectedPlayer.getSpells().size()];
        for (int i = 0; i < selectedPlayer.getSpells().size(); i++) {
            users[i] = selectedPlayer.getSpells().get(i).name;
        }

        Spinner spin = view.findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_big_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(selectedSpellId);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillSpellData(selectedPlayer.getSpells().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


}
