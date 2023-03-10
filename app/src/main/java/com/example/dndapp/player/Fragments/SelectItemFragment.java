package com.example.dndapp.player.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.dndapp.R;
import com.example.dndapp._data.items.AvailableItems;
import com.example.dndapp._data.items.ItemData;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;
import com.example.dndapp.player.Adapters.SelectItemListAdapter;
import com.example.dndapp.player.AddItemActivity;

import java.util.ArrayList;
import java.util.Objects;

public class SelectItemFragment extends Fragment {
    @Nullable
    private Listener listener;
    private SelectItemListAdapter  adapter;
    private ArrayList<ItemData> items = new ArrayList<>();

    public SelectItemFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectItemFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectItemFragment newInstance() {
        return new SelectItemFragment();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_item, container, false);
        final ListView lv = view.findViewById(R.id.selectable_items);

        Toolbar tb = view.findViewById(R.id.toolbar);
        tb.setTitle("Add Item");
        registerExitFragmentButton(tb);

        items.addAll(AvailableItems.items);

        adapter = new SelectItemListAdapter(Objects.requireNonNull(this.getActivity()), items);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view1, position, id) -> {
            // Set selected item.
            int idx = view1.getId();
            AddItemActivity.selectedItem = AvailableItems.getItem(idx);

            // Remove current fragment.
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });

        SearchView searchView = view.findViewById(R.id.search_fragment_button);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterItems(s);
                return false;
            }
        });
        return view;
    }

    private void filterItems(String s) {
        items.clear();
        for (ItemData ability : AvailableItems.items) {
            if (ability.getName().toLowerCase().contains(s.toLowerCase())) {
                items.add(ability);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (listener != null)
            listener.onDetached(this);
    }

    public interface Listener {
        void onDetached(SelectItemFragment fragment);
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }
}
