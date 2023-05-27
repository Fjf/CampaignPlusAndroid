package com.example.campaignplus.player.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.campaignplus.R;
import com.example.campaignplus._data.items.AvailableItems;
import com.example.campaignplus._data.items.ItemData;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.eventlisteners.ShortHapticFeedback;
import com.example.campaignplus.player.Adapters.SelectItemListAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class SelectItemFragment extends Fragment {
    @Nullable
    private Listener listener;
    private SelectItemListAdapter  adapter;
    private final ArrayList<ItemData> items = new ArrayList<>();

    public static abstract class OnCompleteCallback {
        /*
         * This class can be implemented if the user wishes to get a callback function executed.
         * It success will pass the selected item's id, and cancel is called upon exiting without
         *  selecting an item.
         */
        abstract public void success(int selectedItem);
        abstract public void cancel();
    }

    private OnCompleteCallback callback = null;

    public SelectItemFragment(OnCompleteCallback cb) {
        this.callback = cb;
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectItemFragment.
     */
    public static SelectItemFragment newInstance(OnCompleteCallback callback) {
        return new SelectItemFragment(callback);
    }

    private void registerExitFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.close_fragment_button);
        btn.setOnClickListener(view -> {
            // Remove current fragment
            if (callback != null)
                callback.cancel();
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AvailableItems.updateItems(new CallBack() {
            @Override
            public void success() {
                // Update item list
                items.clear();
                items.addAll(AvailableItems.items);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void error(String errorMessage) {
                Toast.makeText(getContext(), "Error while fetching items: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_item, container, false);
        final ListView lv = view.findViewById(R.id.selectable_items);

        Toolbar tb = view.findViewById(R.id.toolbar);
        tb.setTitle("Select Item");
        registerExitFragmentButton(tb);

        adapter = new SelectItemListAdapter(Objects.requireNonNull(this.getActivity()), items);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view1, position, id) -> {
            // Tell parent we are done.
            if (callback != null)
                callback.success(view1.getId());
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
}
