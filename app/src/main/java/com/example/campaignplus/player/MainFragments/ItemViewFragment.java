package com.example.campaignplus.player.MainFragments;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.example.campaignplus._data.items.AvailableItems;
import com.example.campaignplus._data.items.EquipmentItem;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus.player.Adapters.EquipmentListAdapter;
import com.example.campaignplus.player.CreateItemActivity;
import com.example.campaignplus.player.Fragments.SelectItemFragment;
import com.example.campaignplus.player.Fragments.ItemInfoFragment;
import com.example.campaignplus.player.RecyclerItemClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

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
                getActivity().runOnUiThread(() -> {
                    if (selectedPlayer.equipment.size() == 0) {
                        view.findViewById(R.id.no_items_text).setVisibility(View.VISIBLE);
                    } else {
                        view.findViewById(R.id.no_items_text).setVisibility(View.GONE);
                    }
                    itemAdapter.notifyDataSetChanged();
                });
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

        view.findViewById(R.id.add_item_fab).setOnClickListener(this::openAddItemFragment);
    }

    public void openItemFragment(View view) {
        if (selectedPlayer.equipment.isEmpty()) {
            Toast.makeText(this.getContext(), "You have no items.", Toast.LENGTH_SHORT).show();
            return;
        }

        Fragment fragment = new ItemInfoFragment(new ItemInfoFragment.OnCompleteCallback() {
            @Override
            public void success(int itemId) {
                itemAdapter.notifyItemChanged(itemId);
            }

            @Override
            public void cancel() {

            }
        });
        fragment.setEnterTransition(new Slide(Gravity.END));
        fragment.setExitTransition(new Slide(Gravity.START));

        openFragment(fragment);
    }

    public void openAddItemFragment(View view) {
        Fragment fragment = new SelectItemFragment(new SelectItemFragment.OnCompleteCallback() {
            @Override
            public void success(int itemId) {
                // Push added item to remote.
                try {
                    uploadItemRemote(itemId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void cancel() {
                // Don't do anything on cancel
            }
        });
        fragment.setEnterTransition(new Slide(Gravity.END));
        fragment.setExitTransition(new Slide(Gravity.START));
        openFragment(fragment);
    }

    public void uploadItemRemote(int itemId) throws JSONException {
        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("item_id", itemId);
        data.put("amount", 1);

        String url = String.format(Locale.ENGLISH, "player/%s/item", selectedPlayer.getId());
        HttpUtils.post(url, data.toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error adding item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        selectedPlayer.equipment.add(new EquipmentItem(jsonObject));
                        getActivity().runOnUiThread(() -> {
                            itemAdapter.notifyItemInserted(selectedPlayer.equipment.size() - 1);
                            Toast.makeText(getContext(), "Successfully added item.", Toast.LENGTH_SHORT).show();
                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error adding item: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });

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
        HttpUtils.delete(url, new okhttp3.Callback() {
    @Override
    public void onFailure(okhttp3.Call call, IOException e) {
        Log.d(TAG, "Invalid response: " + e.getMessage());
    }

    @Override
    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            try {
                JSONObject jsonObject = new JSONObject(responseBody);
                if (!jsonObject.getBoolean("success")) {
                    Log.d(TAG, "Something went wrong deleting your item.");
                    return;
                }

                getActivity().runOnUiThread(() -> {
                    closeMenus();
                    getPlayerItems();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Invalid response: " + response.message());
        }
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
