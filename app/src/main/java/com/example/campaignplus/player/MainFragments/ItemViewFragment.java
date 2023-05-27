package com.example.campaignplus.player.MainFragments;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.items.EquipmentItem;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus.player.Adapters.EquipmentListAdapter;
import com.example.campaignplus.player.Fragments.ItemInfoFragment;
import com.example.campaignplus.player.RecyclerItemClickListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class ItemViewFragment extends PlayerInfoFragment {
    private static final String TAG = "ItemViewFragment";

    private View view;
    public static int selectedItemId;

    private RecyclerView itemRecyclerView;
    private EquipmentListAdapter itemAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_player_item_list, container, false);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Items");
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);

        itemRecyclerView = view.findViewById(R.id.player_item_list);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        itemRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager itemLayoutManager = new LinearLayoutManager(container.getContext());
        itemRecyclerView.setLayoutManager(itemLayoutManager);

        // Set the adapter based on currently selected player.
        itemAdapter = new EquipmentListAdapter(selectedPlayer.equipment);
        itemRecyclerView.setAdapter(itemAdapter);

        onUpdateCurrentPlayer();
        setEventListeners();

        return view;
    }

    public void getPlayerItems() {
        selectedPlayer.getEquipment(new CallBack() {
            @Override
            public void success() {
                if (selectedPlayer.equipment.size() == 0) {
                    view.findViewById(R.id.no_items_text).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.no_items_text).setVisibility(View.GONE);
                }

                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void error(String errorMessage) {
                Log.d(TAG, "Error while fetching player items: " + errorMessage);
            }
        });
    }


    private void setEventListeners() {
        itemRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this.getContext(), itemRecyclerView, new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View v, int position) {
                        selectedItemId = position;
                        openItemFragment(v);
                    }

                    @Override
                    public void onDoubleTap(View v, int position) {
                        selectedItemId = position;

                        TextView tv = view.findViewById(R.id.delete_item_button);
                        tv.setText("Delete " + selectedPlayer.equipment.get(selectedItemId).getItem().getName());

                        // Lock drawer and Show the Spell options.
                        ((DrawerLayout) Objects.requireNonNull(getActivity()).findViewById(R.id.player_info_drawer_layout))
                                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        view.findViewById(R.id.overlay_item_options).setVisibility(View.VISIBLE);
                    }
                })
        );

        view.findViewById(R.id.overlay_item_options).setOnClickListener(v -> {
            v.setVisibility(View.GONE);
        });
        view.findViewById(R.id.delete_item_button).setOnClickListener(this::requestDeleteItem);
        view.findViewById(R.id.show_item_info_button).setOnClickListener(this::openItemFragment);
        view.findViewById(R.id.close_menu_button).setOnClickListener(this::closeMenu);
    }

    public void openItemFragment(View view) {
        if (selectedPlayer.equipment.isEmpty()) {
            Toast.makeText(this.getContext(), "You have no items.", Toast.LENGTH_SHORT).show();
            return;
        }

        Fragment fragment = new ItemInfoFragment();
        fragment.setEnterTransition(new Slide(Gravity.END));
        fragment.setExitTransition(new Slide(Gravity.START));

        openFragment(fragment);
    }

    public void closeMenu(View view) {
        closeMenus();
    }

    public void requestDeleteItem(View view) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    deleteItem();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialog);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteItem() {
        String url = String.format(Locale.ENGLISH, "player/%s/item/%d", selectedPlayer.getId(), selectedPlayer.equipment.get(selectedItemId).getInstanceId());
        HttpUtils.delete(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong deleting your item.");
                        return;
                    }

                    closeMenus();
                    getPlayerItems();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d(TAG, "Invalid response: " + response);
            }
        });
    }

    private void closeMenus() {
        view.findViewById(R.id.overlay_item_options).setVisibility(View.GONE);
    }

    private boolean areMenusOpen() {
        return view.findViewById(R.id.overlay_item_options).getVisibility() == View.VISIBLE;
    }


    private void openFragment(Fragment fragment) {
        if (selectedPlayer == null || selectedPlayer.getId() == -1) {
            Toast.makeText(this.getContext(), "No player selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fragment != null) {
            // Got most of the fragment code from here
            // https://www.androidcode.ninja/android-navigation-drawer-example/

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.player_info_drawer_layout, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void onUpdateCurrentPlayer() {
        getPlayerItems();
    }

}
