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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

        // use a linear layout manager - use view.getContext() instead of container.getContext()
        RecyclerView.LayoutManager itemLayoutManager = new LinearLayoutManager(view.getContext());
        itemRecyclerView.setLayoutManager(itemLayoutManager);

        // Set the adapter based on currently selected player.
        // Initialize with empty list, will be populated by getPlayerItems()
        itemAdapter = new EquipmentListAdapter(selectedPlayer.equipment);
        itemRecyclerView.setAdapter(itemAdapter);

        setEventListeners();
        // Delay getPlayerItems() until after the view is fully attached
        view.post(this::getPlayerItems);

        return view;
    }

    public void getPlayerItems() {
        // Check if view is available and fragment is attached
        if (view == null || !isAdded()) {
            return;
        }
        
        selectedPlayer.getEquipment(new CallBack() {
            @Override
            public void success() {
                // Ensure UI updates happen on the main thread and view exists
                if (getActivity() != null && view != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update the adapter's data directly
                        if (itemAdapter != null) {
                            itemAdapter.notifyDataSetChanged();
                        }
                        
                        // Show/hide empty state
                        View noItemsText = view.findViewById(R.id.no_items_text);
                        if (noItemsText != null) {
                            noItemsText.setVisibility(selectedPlayer.equipment.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });
                }
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
        String jsonString = data.toString();

        String url = String.format(Locale.ENGLISH, "player/%s/item", selectedPlayer.getId());
        HttpUtils.post(url, jsonString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                HttpUtils.showToast(getContext(), "Error adding item: " + e.getMessage(), Toast.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        // Update list if we added an item
                        selectedPlayer.equipment.add(new EquipmentItem(responseObject));
                        itemAdapter.notifyItemInserted(selectedPlayer.equipment.size() - 1);
                        HttpUtils.showToast(getContext(), "Successfully added item.", Toast.LENGTH_SHORT);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON", e);
                        HttpUtils.showToast(getContext(), "Error processing response", Toast.LENGTH_SHORT);
                    }
                } else {
                    HttpUtils.showToast(getContext(), "Error adding item", Toast.LENGTH_SHORT);
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
        HttpUtils.delete(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Failed to delete item: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        if (!responseObject.getBoolean("success")) {
                            Log.d(TAG, "Something went wrong deleting your item.");
                            return;
                        }

                        closeMenus();
                        getPlayerItems();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
        // Only fetch items if the fragment is currently visible
        if (isAdded() && isResumed()) {
            getPlayerItems();
        }
    }

}
