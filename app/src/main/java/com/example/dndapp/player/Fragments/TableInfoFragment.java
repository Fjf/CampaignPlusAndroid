package com.example.dndapp.player.Fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.dndapp.R;
import com.example.dndapp._data.DataCache;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;
import com.example.dndapp.player.Adapters.SpellSlotItemAdapter;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TableInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TableInfoFragment extends Fragment {
    public TableInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SpellSlotsFragment.
     */
    public static TableInfoFragment newInstance() {
        return new TableInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void registerExitFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.close_fragment_button);
        btn.setOnClickListener(view -> {
            // Remove current fragment
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_table_info, container, false);

        /*
         * Register exit button
         */
        Toolbar toolbar = view.findViewById(R.id.fragment_toolbar);
        toolbar.setTitle("Table info for level " + DataCache.selectedPlayer.statsData.getLevel());
        registerExitFragmentButton(toolbar);

        /*
         * Create list of table information
         */
        SpellSlotItemAdapter adapter = SpellSlotItemAdapter.newInstance((Activity) getContext(), R.layout.table_item_layout);
        ListView list = view.findViewById(R.id.table_info_list);
        list.setAdapter(adapter);

        /*
         * Register reset counters button
         */
        View btn = view.findViewById(R.id.reset_button);
        btn.setOnClickListener(v -> {
            // Remove current fragment
            adapter.resetCounters();
        });

        return view;
    }
}
