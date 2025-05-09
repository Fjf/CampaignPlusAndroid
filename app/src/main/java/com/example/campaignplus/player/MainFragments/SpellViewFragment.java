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
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.SpellData;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus.player.Adapters.SpellListAdapter;
import com.example.campaignplus.player.Fragments.SpellInfoFragment;
import com.example.campaignplus.player.RecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class SpellViewFragment extends PlayerInfoFragment {
    private static final String TAG = "SpellViewActivity";
    private Toolbar toolbar;

    private View view;
    private RecyclerView spellRecyclerView;
    private RecyclerView.Adapter spellAdapter;
    private RecyclerView.LayoutManager spellLayoutManager;
    public static int selectedSpellId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_player_spell_list, container, false);

        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Spells");

        spellRecyclerView = view.findViewById(R.id.player_spell_list);
        spellRecyclerView.setHasFixedSize(true);

        spellLayoutManager = new LinearLayoutManager(view.getContext());
        spellRecyclerView.setLayoutManager(spellLayoutManager);


        getPlayerSpells();
        setEventListeners();

        return view;
    }

    private void getPlayerSpells() {
        ArrayList<SpellData> spells = selectedPlayer.getSpells();
        DataCache.selectedPlayer.updateSpells(new CallBack() {
            @Override
            public void success() {
                if (spells.size() == 0) {
                    view.findViewById(R.id.no_spells_text).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.no_spells_text).setVisibility(View.GONE);
                }

                Collections.sort(spells, Comparator.comparingInt(a -> a.level));

                getActivity().runOnUiThread(() -> {
                    spellAdapter = new SpellListAdapter(spells);
                    spellRecyclerView.setAdapter(spellAdapter);
                    spellAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void error(String errorMessage) {
                Log.d(TAG, "Error fetching player spells: " + errorMessage);

            }
        });
    }

    public void requestDeleteSpell(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteSpell();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialog);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteSpell() {
        String url = String.format(
                Locale.ENGLISH,
                "player/%s/spells/%d",
                selectedPlayer.getId(),
                selectedPlayer.getSpells().get(selectedSpellId).id);
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
                        JSONArray jsonArray = new JSONArray(responseBody);
                        selectedPlayer.setSpells(jsonArray);
                        getActivity().runOnUiThread(() -> {
                            getPlayerSpells();
                            view.findViewById(R.id.overlay_spell_options).setVisibility(View.GONE);
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


    private void setEventListeners() {
        spellRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(view.getContext(), spellRecyclerView, new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        selectedSpellId = position;
                        openSpellFragment(view);
                    }

                    @Override
                    public void onDoubleTap(View v, int position) {
                        selectedSpellId = position;

                        TextView tv = view.findViewById(R.id.delete_spell_button);
                        tv.setText("Delete " + selectedPlayer.getSpells().get(selectedSpellId).name);

                        // Lock drawer and Show the Spell options.
                        ((DrawerLayout) getActivity().findViewById(R.id.player_info_drawer_layout))
                                .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        view.findViewById(R.id.overlay_spell_options).setVisibility(View.VISIBLE);
                    }
                })
        );

        view.findViewById(R.id.overlay_spell_options).setOnClickListener(v -> {
            v.setVisibility(View.GONE);
        });
        view.findViewById(R.id.delete_spell_button).setOnClickListener(this::requestDeleteSpell);
        view.findViewById(R.id.open_spell_fragment_button).setOnClickListener(this::openSpellFragment);
        view.findViewById(R.id.close_menu_button).setOnClickListener(this::closeMenu);
    }

    public void openSpellFragment(View view) {
        if (selectedPlayer.getSpells().size() == 0) {
            Toast.makeText(view.getContext(), "You have no spells.", Toast.LENGTH_SHORT).show();
            return;
        }
        Fragment fragment = new SpellInfoFragment();
        fragment.setEnterTransition(new Slide(Gravity.START));
        fragment.setExitTransition(new Slide(Gravity.END));

        openFragment(fragment);
    }

    public void closeMenu(View view) {
        closeMenus();
    }


    private void closeMenus() {
        view.findViewById(R.id.overlay_spell_options).setVisibility(View.GONE);
    }

    private boolean areMenusOpen() {
        return view.findViewById(R.id.overlay_spell_options).getVisibility() == View.VISIBLE;
    }


    private void openFragment(Fragment fragment) {
        if (selectedPlayer == null || selectedPlayer.getId() == -1) {
            Toast.makeText(view.getContext(), "No player selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fragment != null) {
            // Got most of the fragment code from here
            // https://www.androidcode.ninja/android-navigation-drawer-example/

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.player_info_drawer_layout, fragment, "spellViewFragment");
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void onUpdateCurrentPlayer() {
        getPlayerSpells();
    }
}
