package com.example.dndapp.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.R;
import com.example.dndapp._data.DataCache;
import com.example.dndapp._data.DrawerListData;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._data.PlayerStatsData;
import com.example.dndapp._data.SpellData;
import com.example.dndapp._data.items.EquipmentItem;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.IgnoreFunctionCall;
import com.example.dndapp.campaign.CampaignOverviewActivity;
import com.example.dndapp.login.LoginActivity;
import com.example.dndapp.login.UserService.UserService;
import com.example.dndapp.player.Adapters.DrawerListAdapter;
import com.example.dndapp.player.Adapters.DrawerPCListAdapter;
import com.example.dndapp.player.Adapters.ItemListAdapter;
import com.example.dndapp.player.Adapters.SpellListAdapter;
import com.example.dndapp.player.Fragments.ClassInformationFragment;
import com.example.dndapp.player.Fragments.PlayerAddSpellFragment;
import com.example.dndapp.player.Fragments.PlayerItemFragment;
import com.example.dndapp.player.Fragments.PlayerSpellFragment;
import com.example.dndapp.player.Fragments.SpellOptionsFragment;
import com.example.dndapp.player.Fragments.SpellSlotsFragment;
import com.example.dndapp.player.Fragments.StatsFragment;
import com.example.dndapp.player.Listeners.TextOnChangeSaveListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import static com.example.dndapp._data.DataCache.selectedPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class PlayerInfoActivity extends AppCompatActivity {
    private static final String TAG = "PlayerInfoActivity";

    private RecyclerView itemRecyclerView;
    private RecyclerView.Adapter itemAdapter;
    private RecyclerView.LayoutManager itemLayoutManager;

    private RecyclerView spellRecyclerView;
    private RecyclerView.Adapter spellAdapter;
    private RecyclerView.LayoutManager spellLayoutManager;

    private Toolbar toolbar;


    private TextView totalStrength;
    private TextView totalConstitution;
    private TextView totalDexterity;
    private TextView totalWisdom;
    private TextView totalCharisma;
    private TextView totalIntelligence;

    private TextView strengthMod;
    private TextView dexterityMod;
    private TextView constitutionMod;
    private TextView wisdomMod;
    private TextView intelligenceMod;
    private TextView charismaMod;

    private TextView statArmorClass;
    private TextView statMaxHP;
    private TextView statLevel;

    public static EquipmentItem[] pidDataSet = new EquipmentItem[0];
    public static int selectedSpellId;
    public static int selectedItemId;

    private final int UPDATE_STATS = 0;
    private final int UPDATE_ITEMS = 1;
    private final int UPDATE_SPELL = 2;
    private final int SHOW_SPELLS = 3;
    private final int SHOW_ITEMS = 4;
    private final int CREATE_PLAYER_RESULT = 5;


    private float x1;
    private float x2;

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private ListView leftDrawerList;
    private ListView leftDrawerPCList;
    private View leftDrawerWrapper;
    private DrawerPCListAdapter drawerPCListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate called for " + TAG);
        setContentView(R.layout.activity_player_info);   // Attaching the layout to the toolbar object

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Character Sheet");
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);
        setSupportActionBar(toolbar);

        registerStatViews();

        String[] leftDrawerItemTitles = getResources().getStringArray(R.array.player_info_left_drawer_titles);
        TypedArray leftDrawerItemIcons = getResources().obtainTypedArray(R.array.player_info_left_drawer_icons);
        ArrayList<DrawerListData> dld = createDrawerListData(leftDrawerItemTitles, leftDrawerItemIcons);

        // Load drawer variables.
        drawerLayout = findViewById(R.id.player_info_drawer_layout);
        leftDrawerList = findViewById(R.id.left_drawer);
        leftDrawerPCList = findViewById(R.id.left_drawer_pc);
        leftDrawerWrapper = findViewById(R.id.left_drawer_wrapper);

        leftDrawerList.setAdapter(new DrawerListAdapter(this, R.layout.left_drawer_menu_item, dld));
        leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        leftDrawerPCList.setOnItemClickListener(new DrawerPCItemClickListener());

        justifyListViewHeightBasedOnChildren(leftDrawerList);

        /*
         *  Get player information and try to load the correct player object into local views.
         */
        preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        int playerId = preferences.getInt("player_id", -1);
        trySelectingPlayer(playerId, true);

        // Set onchange listener for current hp.
        ((EditText) findViewById(R.id.statCurrentHP)).setText(preferences.getString("current_hp", "0"));
        findViewById(R.id.statCurrentHP).setOnKeyListener(new TextOnChangeSaveListener(preferences, "current_hp"));

        // Set onchange listener for bonus hp.
        ((EditText) findViewById(R.id.statTemporaryHP)).setText(preferences.getString("temporary_hp", "0"));
        findViewById(R.id.statTemporaryHP).setOnKeyListener(new TextOnChangeSaveListener(preferences, "temporary_hp"));

        itemRecyclerView = findViewById(R.id.player_item_list);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        itemRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        itemLayoutManager = new LinearLayoutManager(this);
        itemRecyclerView.setLayoutManager(itemLayoutManager);

        itemRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, itemRecyclerView, new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        selectedItemId = position;
                        openFragment(0);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        selectedItemId = position;

                        TextView tv = findViewById(R.id.deleteItemTextButton);
                        tv.setText("Delete " + pidDataSet[selectedItemId].getItem().getName());
                        findViewById(R.id.itemSettingsOverlayMenu).setVisibility(View.VISIBLE);
                    }
                })
        );

        spellRecyclerView = findViewById(R.id.player_spell_list);
        spellRecyclerView.setHasFixedSize(true);

        spellLayoutManager = new LinearLayoutManager(this);
        spellRecyclerView.setLayoutManager(spellLayoutManager);

        spellRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, spellRecyclerView, new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        selectedSpellId = position;
                        openFragment(2);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                        Fragment fragment = SpellOptionsFragment.newInstance(position);
                        fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                        fragment.setExitTransition(new Slide(Gravity.TOP));

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();

                        ft.replace(R.id.player_info_drawer_layout, fragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                })
        );
    }

    public void refreshItems() {
        getPlayerItems();
    }

    private void trySelectingPlayer(final int id, boolean forceUpdate) {
        MyPlayerCharacterList.initialize(new FunctionCall() {
            @Override
            public void success() {
                if (DataCache.playerData.size() == 0) {
                    Toast.makeText(PlayerInfoActivity.this, "You have no player characters.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updatePlayerDrawer();
                updatePlayerInfo(id);
            }

            @Override
            public void error(String errorMessage) {
                // Server error.
            }
        }, forceUpdate);
    }

    private void updatePlayerDrawer() {
        // TODO: Update this to reuse adapters
        drawerPCListAdapter = new DrawerPCListAdapter(PlayerInfoActivity.this, R.layout.left_drawer_menu_item, DataCache.playerData);
        leftDrawerPCList.setAdapter(drawerPCListAdapter);

        justifyListViewHeightBasedOnChildren(leftDrawerPCList);
    }

    private void updatePlayerInfo(int id) {
        // Set currently selected playerid to preferences to autoload next time
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("player_id", id);
        editor.apply();

        // Always have a player selected.
        if (id == -1) {
            if (DataCache.playerData.size() == 0)
                selectedPlayer = new PlayerData();
            else
                selectedPlayer = DataCache.playerData.get(0);
        } else {
            selectedPlayer = DataCache.getPlayer(id);
        }

        if (selectedPlayer == null) {
            // We have an invalid ID in our SharedPreferences, remove this.
            editor.clear();
            editor.apply();
            selectedPlayer = MyPlayerCharacterList.emptyPlayer();
        }


        selectedPlayer.updatePlayerData(new FunctionCall() {
            @Override
            public void success() {
                setStatsFields();
                toolbar.setTitle(selectedPlayer.getName());
            }

            @Override
            public void error(String errorMessage) {
                Toast.makeText(PlayerInfoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Don't retrieve information from a placeholder player object.
        if (selectedPlayer.getId() != -1) {
            getPlayerItems();
            getPlayerSpells();
        }
    }

    private ArrayList<DrawerListData> createDrawerListData(String[] leftDrawerItemTitles, TypedArray leftDrawerItemIcons) {
        ArrayList<DrawerListData> dld = new ArrayList<>();
        for (int i = 0; i < leftDrawerItemTitles.length; i++) {
            dld.add(new DrawerListData(leftDrawerItemTitles[i], leftDrawerItemIcons.getDrawable(i)));
        }
        return dld;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_character_sheet, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_show_phb) {
            Intent intent = new Intent(this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_campaigns) {
            Intent intent = new Intent(this, CampaignOverviewActivity.class);
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            drawerLayout.openDrawer(leftDrawerWrapper);
        } else if (id == R.id.action_delete_player) {
            if (selectedPlayer == null) {
                Toast.makeText(this, "There is no player currently selected.", Toast.LENGTH_SHORT).show();
                return true;
            }
            deletePlayer();
        } else if (id == R.id.action_show_abilities) {
            if (selectedPlayer == null) {
                Toast.makeText(this, "There is no player currently selected.", Toast.LENGTH_SHORT).show();
                return true;
            }

            Fragment fragment = ClassInformationFragment.newInstance(0);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.player_info_drawer_layout, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else if (id == R.id.action_create_player) {
            Intent intent = new Intent(this, CreatePlayerActivity.class);
            intent.putExtra("create", true);
            startActivityForResult(intent, CREATE_PLAYER_RESULT);
        } else if (id == R.id.action_update_player) {
            if (selectedPlayer == null) {
                Toast.makeText(this, "There is no player currently selected.", Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent intent = new Intent(this, CreatePlayerActivity.class);
            intent.putExtra("create", false);
            startActivityForResult(intent, CREATE_PLAYER_RESULT);
        } else if (id == R.id.action_logout) {
            UserService.logout(new FunctionCall() {
                @Override
                public void success() {
                    Intent intent = new Intent(PlayerInfoActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);

                    String[] toClearPreferences = {"LoginData", "PlayerData"};
                    for (String pref : toClearPreferences) {
                        SharedPreferences preferences = getSharedPreferences(pref, MODE_PRIVATE);
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.clear();
                        edit.apply();
                    }
                    finish();
                }

                @Override
                public void error(String errorMessage) {
                    Toast.makeText(PlayerInfoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(PlayerInfoActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == UPDATE_ITEMS) {
            // After adding an item, update the item list from the server.
            getPlayerItems();
        } else if (requestCode == UPDATE_STATS) {
            // After updating your stats, update player stats.
            selectedPlayer.updatePlayerData(new IgnoreFunctionCall());
        } else if (requestCode == UPDATE_SPELL) {
            Log.d("------------------------", "got response");
            // After adding a spell, update spells list.
            getPlayerSpells();
        } else if (requestCode == CREATE_PLAYER_RESULT) {
            final int playerId = data.getIntExtra("player_id", -1);
            if (playerId == -1)
                return;

            MyPlayerCharacterList.updatePlayerData(new FunctionCall() {
                @Override
                public void success() {
                    updatePlayerDrawer();
                    updatePlayerInfo(playerId);
                }

                @Override
                public void error(String errorMessage) {
                    // Nothing here?
                }
            });
        }
    }

    private void getPlayerItems() {
        String url = String.format(Locale.ENGLISH, "player/%s/items", selectedPlayer.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray items) {
                try {
                    Log.d(TAG, "Response length:" + items.length());
                    if (items.length() == 0) {
                        findViewById(R.id.no_items_text).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.no_items_text).setVisibility(View.GONE);
                    }

                    pidDataSet = new EquipmentItem[items.length()];
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject obj = items.getJSONObject(i);
                        pidDataSet[i] = new EquipmentItem(obj);
                    }

                    itemAdapter = new ItemListAdapter(pidDataSet);
                    itemRecyclerView.setAdapter(itemAdapter);
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

    @Override
    protected void onNewIntent(Intent intent) {
        // Overwrite previous intent when navigating back to this activity.
        this.setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        int playerId = intent.getIntExtra("player_id", -1);

        if (playerId != -1)
            trySelectingPlayer(playerId, true);
        super.onResume();
    }

    private void deletePlayer() {
        String url = String.format(Locale.ENGLISH, "player/%s", selectedPlayer.getId());
        HttpUtils.delete(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                MyPlayerCharacterList.updatePlayerData(new FunctionCall() {
                    @Override
                    public void success() {
                        trySelectingPlayer(-1, false);
                    }

                    @Override
                    public void error(String errorMessage) {

                    }
                });
            }
        });
    }


    private void registerStatViews() {
        totalStrength = findViewById(R.id.statStrengthTotal);
        totalConstitution = findViewById(R.id.statConstitutionTotal);
        totalDexterity = findViewById(R.id.statDexterityTotal);
        totalWisdom = findViewById(R.id.statWisdomTotal);
        totalCharisma = findViewById(R.id.statCharismaTotal);
        totalIntelligence = findViewById(R.id.statIntelligenceTotal);

        strengthMod = findViewById(R.id.stStrength);
        constitutionMod = findViewById(R.id.stConstitution);
        dexterityMod = findViewById(R.id.stDexterity);
        wisdomMod = findViewById(R.id.stWisdom);
        charismaMod = findViewById(R.id.stCharisma);
        intelligenceMod = findViewById(R.id.stIntelligence);

        statArmorClass = findViewById(R.id.statArmorClass);
        statMaxHP = findViewById(R.id.statMaxHP);
        statLevel = findViewById(R.id.level_value);
    }

    public void setStatsFields() {
        PlayerStatsData sd = selectedPlayer.statsData;
        TextView tv;

        // Base stats.
        totalStrength.setText(sd.getStrength());
        totalConstitution.setText(sd.getConstitution());
        totalDexterity.setText(sd.getDexterity());
        totalWisdom.setText(sd.getWisdom());
        totalCharisma.setText(sd.getCharisma());
        totalIntelligence.setText(sd.getIntelligence());

        // Stat modifier.
        strengthMod.setText(sd.getStrengthModifier());
        dexterityMod.setText(sd.getDexterityModifier());
        constitutionMod.setText(sd.getConstitutionModifier());
        wisdomMod.setText(sd.getWisdomModifier());
        intelligenceMod.setText(sd.getIntelligenceModifier());
        charismaMod.setText(sd.getCharismaModifier());

        // All other information
        statArmorClass.setText(sd.getArmorClass());
        statMaxHP.setText(sd.getMaxHP());
        statLevel.setText(sd.getLevel());
    }

    private void getPlayerSpells() {
        ArrayList<SpellData> spells = selectedPlayer.getSpells();
        DataCache.selectedPlayer.updateSpells(new FunctionCall() {
            @Override
            public void success() {
                if (spells.size() == 0) {
                    findViewById(R.id.no_spells_text).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.no_spells_text).setVisibility(View.GONE);
                }

                spellAdapter = new SpellListAdapter(spells);
                spellRecyclerView.setAdapter(spellAdapter);
                spellAdapter.notifyDataSetChanged();
            }

            @Override
            public void error(String errorMessage) {
                Log.d(TAG, "Error fetching player spells: " + errorMessage);

            }
        });
    }

    public void eatClickEvent(View view) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteSpell() {
        String url = String.format(
                Locale.ENGLISH,
                "player/%s/spells/%d",
                selectedPlayer.getId(),
                selectedPlayer.getSpells().get(selectedSpellId).getId());
        HttpUtils.delete(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    selectedPlayer.setSpells(response);
                    closeMenus();
                    getPlayerSpells();
                    findViewById(R.id.spellSettingsOverlayMenu).setVisibility(View.GONE);
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

    public void requestDeleteItem(View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteItem();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deleteItem() {
        String url = String.format(Locale.ENGLISH, "player/%s/item/%d", selectedPlayer.getId(), pidDataSet[selectedItemId].getInstanceId());
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
        findViewById(R.id.spellSettingsOverlayMenu).setVisibility(View.GONE);
        findViewById(R.id.itemSettingsOverlayMenu).setVisibility(View.GONE);
    }

    private boolean areMenusOpen() {
        return findViewById(R.id.spellSettingsOverlayMenu).getVisibility() == View.VISIBLE ||
                findViewById(R.id.itemSettingsOverlayMenu).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onBackPressed() {
        closeMenus();
        // Close fragments on top.
        int count = getSupportFragmentManager().getBackStackEntryCount();

        System.out.println(count);
        if (count > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    public void closeMenu(View view) {
        closeMenus();
    }

    public void showSpellPHB(View view) {
        closeMenus();

        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("REQUESTED_PAGE_NUMBER", selectedPlayer.getSpells().get(selectedSpellId).getPhb());
        startActivity(intent);
    }

    public void showItemPHB(View view) {
        closeMenus();

        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("REQUESTED_PAGE_NUMBER", pidDataSet[selectedSpellId].getItem().getPhb());
        startActivity(intent);
    }

    public void openItemFragment(View view) {
        openFragment(0);
    }

    public void openSpellFragment(View view) {
        openFragment(2);
    }

    public void refreshSpells() {
        getPlayerSpells();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openFragment(position);
        }
    }

    private class DrawerPCItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            drawerLayout.closeDrawer(leftDrawerWrapper);
            updatePlayerInfo(DataCache.playerData.get(position).getId());
        }
    }

    private void openFragment(int position) {
        Fragment fragment = null;
        Intent intent;

        if (selectedPlayer == null || selectedPlayer.getId() == -1) {
            Toast.makeText(this, "No player selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Close any open fragments before opening the next.
        if (this.getSupportFragmentManager().getBackStackEntryCount() != 0)
            this.getSupportFragmentManager().popBackStackImmediate();

        final int FRAGMENT_ID_ITEM = 0;
        final int FRAGMENT_ID_ADD_ITEM = 1;
        final int FRAGMENT_ID_SPELL = 2;
        final int FRAGMENT_ID_ADD_SPELL = 3;
        final int FRAGMENT_ID_SPELL_SLOTS = 4;
        final int FRAGMENT_ID_PLAYER_STATS = 5;

        switch (position) {
            case FRAGMENT_ID_ITEM: // My Items
                if (pidDataSet.length == 0) {
                    Toast.makeText(this, "You have no items.", Toast.LENGTH_SHORT).show();
                    return;
                }

                fragment = new PlayerItemFragment();
                fragment.setEnterTransition(new Slide(Gravity.END));
                fragment.setExitTransition(new Slide(Gravity.START));
                break;
            case FRAGMENT_ID_ADD_ITEM: // Add Item
                intent = new Intent(this, AddItemActivity.class);
                startActivityForResult(intent, UPDATE_ITEMS);
                drawerLayout.closeDrawer(leftDrawerWrapper);
                return;
            case FRAGMENT_ID_SPELL: // My Spells
                if (selectedPlayer.getSpells().size() == 0) {
                    Toast.makeText(this, "You have no spells.", Toast.LENGTH_SHORT).show();
                    return;
                }
                fragment = new PlayerSpellFragment();
                fragment.setEnterTransition(new Slide(Gravity.START));
                fragment.setExitTransition(new Slide(Gravity.END));
                break;
            case FRAGMENT_ID_ADD_SPELL: // Add Spell
                fragment = new PlayerAddSpellFragment();
                fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                fragment.setExitTransition(new Slide(Gravity.TOP));
                break;
            case FRAGMENT_ID_PLAYER_STATS: // Player Stats
                fragment = new StatsFragment();
                fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                fragment.setExitTransition(new Slide(Gravity.TOP));
                break;
            case FRAGMENT_ID_SPELL_SLOTS: // Player spell slots
                fragment = new SpellSlotsFragment();
                fragment.setEnterTransition(new Slide(Gravity.START));
                fragment.setExitTransition(new Slide(Gravity.END));
                break;
            default:
                break;
        }

        if (fragment != null) {
            // Got most of the fragment code from here
            // https://www.androidcode.ninja/android-navigation-drawer-example/

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.player_info_drawer_layout, fragment);
            ft.addToBackStack(null);
            ft.commit();

            leftDrawerList.setItemChecked(position, true);
            leftDrawerList.setSelection(position);
            drawerLayout.closeDrawer(leftDrawerWrapper);
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    public void justifyListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }
}
