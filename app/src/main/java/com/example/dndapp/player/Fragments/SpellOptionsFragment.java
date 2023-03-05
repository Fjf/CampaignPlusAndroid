package com.example.dndapp.player.Fragments;

import static com.example.dndapp._data.DataCache.selectedPlayer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.dndapp.R;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;

import java.util.Objects;

public class SpellOptionsFragment extends Fragment {
    private static final String SPELL_ID = "SpellID";
    private int selectedSpellId;

    public SpellOptionsFragment() {
    }

    private void registerExitFragmentButton(View v) {
        View btn = v.findViewById(R.id.close_fragment_button);
        btn.setOnClickListener(view -> {
            // Remove current fragment
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }
    public static Fragment newInstance(int selectedSpellId) {
        SpellOptionsFragment fragment = new SpellOptionsFragment();
        Bundle args = new Bundle();
        args.putInt(SPELL_ID, selectedSpellId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedSpellId = getArguments().getInt(SPELL_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((DrawerLayout) this.getActivity().findViewById(R.id.player_info_drawer_layout))
                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_spell_options, container, false);
        registerExitFragmentButton(view);


        TextView tv = view.findViewById(R.id.deleteSpellTextButton);
        tv.setText("Delete " + selectedPlayer.getSpells().get(selectedSpellId).getName());
        view.findViewById(R.id.spellSettingsOverlayMenu).setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onDestroyView() {
        ((DrawerLayout) this.getActivity().findViewById(R.id.player_info_drawer_layout))
                .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onDestroyView();
    }
}
