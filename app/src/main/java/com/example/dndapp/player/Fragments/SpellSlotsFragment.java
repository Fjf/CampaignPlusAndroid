package com.example.dndapp.player.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dndapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SpellSlotsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SpellSlotsFragment extends Fragment {
    public SpellSlotsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SpellSlotsFragment.
     */
    public static SpellSlotsFragment newInstance() {
        return new SpellSlotsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spell_slots, container, false);
    }
}
