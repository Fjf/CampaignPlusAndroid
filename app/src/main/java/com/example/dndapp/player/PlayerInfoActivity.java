package com.example.dndapp.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.login.LoginActivity;
import com.example.dndapp.login.UserService.UserService;
import com.example.dndapp.player.Adapters.DrawerListAdapter;
import com.example.dndapp.player.Adapters.DrawerPCListAdapter;
import com.example.dndapp.player.Adapters.ItemListAdapter;
import com.example.dndapp.player.Adapters.SpellListAdapter;
import com.example.dndapp.player.Fragments.ClassInformationFragment;
import com.example.dndapp.player.Fragments.PlayerItemFragment;
import com.example.dndapp.player.Fragments.PlayerSpellFragment;
import com.example.dndapp.player.Fragments.StatsFragment;
import com.example.dndapp.player.Listeners.TextOnChangeSaveListener;
import com.example.dndapp.campaign.CampaignOverviewActivity;
import com.example.dndapp.R;
import com.example.dndapp._data.DrawerListData;
import com.example.dndapp._data.items.ItemData;
import com.example.dndapp._data.items.ItemType;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._data.PlayerStatsData;
import com.example.dndapp._data.SpellData;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.IgnoreFunctionCall;
import com.loopj.android.http.JsonHttpResponseHandler;

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

    private final int FRAGMENT_ID_ITEM =     0;
    private final int FRAGMENT_ID_ADD_ITEM = 1;
    private final int FRAGMENT_ID_SPELL = 2;
    private final int FRAGMENT_ID_ADD_SPELL = 3;
    private final int FRAGMENT_ID_PLAYER_STATS = 4;


    private static TextView totalStrength;
    private static TextView totalConstitution;
    private static TextView totalDexterity;
    private static TextView totalWisdom;
    private static TextView totalCharisma;
    private static TextView totalIntelligence;

    private static TextView strengthMod;
    private static TextView dexterityMod;
    private static TextView constitutionMod;
    private static TextView wisdomMod;
    private static TextView intelligenceMod;
    private static TextView charismaMod;

    private static TextView statArmorClass;
    private static TextView statMaxHP;
    private static TextView statLevel;

    public static SpellData[] psdDataSet = new SpellData[0];
    public static ItemData[] pidDataSet = new ItemData[0];
    public static int selectedSpellId;
    public static int selectedItemId;

    public static PlayerData selectedPlayer = MyPlayerCharacterList.emptyPlayer();

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
        int playerId = getIntent().getIntExtra("player_id", -1);
        trySelectingPlayer(playerId, false);

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
                        tv.setText("Delete " + pidDataSet[selectedItemId].getName());
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
                        selectedSpellId = position;

                        TextView tv = findViewById(R.id.deleteSpellTextButton);
                        tv.setText("Delete " + psdDataSet[selectedSpellId].getName());
                        findViewById(R.id.spellSettingsOverlayMenu).setVisibility(View.VISIBLE);
                    }
                })
        );
    }



    private void trySelectingPlayer(final int id, boolean forceUpdate) {
        if (forceUpdate || !MyPlayerCharacterList.hasInitialized) {
            MyPlayerCharacterList.initialize(new FunctionCall() {
                @Override
                public void success() {
                    if (MyPlayerCharacterList.playerData.size() == 0) {
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
            });
        } else {
            updatePlayerDrawer();
            updatePlayerInfo(id);
        }
    }

    private void updatePlayerDrawer() {
        // TODO: Update this to reuse adapters
        drawerPCListAdapter = new DrawerPCListAdapter(PlayerInfoActivity.this, R.layout.left_drawer_menu_item, MyPlayerCharacterList.playerData);
        leftDrawerPCList.setAdapter(drawerPCListAdapter);

        justifyListViewHeightBasedOnChildren(leftDrawerPCList);
    }

    private void updatePlayerInfo(int id) {
        // Always have a player selected.
        if (id == -1) {
            if (MyPlayerCharacterList.playerData.size() == 0)
                selectedPlayer = MyPlayerCharacterList.emptyPlayer();
            else
                selectedPlayer = MyPlayerCharacterList.playerData.get(0);
        } else {
            selectedPlayer = MyPlayerCharacterList.getPlayer(id);
        }

        // Get all information and items.
        assert selectedPlayer != null;
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
            SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.remove("username"); edit.remove("password");
            edit.apply();

            UserService.logout(new FunctionCall() {
                @Override
                public void success() {
                    Intent intent = new Intent(PlayerInfoActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
            // After creating a spell, update spells list.
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
        String url = String.format(Locale.ENGLISH, "player/%s/item", selectedPlayer.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong retrieving items from server.");
                        return;
                    }

                    JSONArray array = response.getJSONArray("items");
                    JSONObject obj;

                    if (array.length() == 0) {
                        findViewById(R.id.no_items_text).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.no_items_text).setVisibility(View.GONE);
                    }

                    pidDataSet = new ItemData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);

                        ItemType type = getItemType(obj.getString("category"));

                        pidDataSet[i] = new ItemData(obj, type);
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

    private ItemType getItemType(String category) {
        if (category.equals("Weapon"))
            return ItemType.WEAPON;

        // Fallback.
        return ItemType.ITEM;
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

    public static void setStatsFields() {
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
        String url = String.format(Locale.ENGLISH, "player/%s/spell", selectedPlayer.getId());
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong retrieving spells from the server.");
                        return;
                    }

                    JSONArray array = response.getJSONArray("spells");
                    JSONObject obj;

                    if (array.length() == 0) {
                        findViewById(R.id.no_spells_text).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.no_spells_text).setVisibility(View.GONE);
                    }

                    psdDataSet = new SpellData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        psdDataSet[i] = new SpellData(obj);
                    }


                    spellAdapter = new SpellListAdapter(psdDataSet);
                    spellRecyclerView.setAdapter(spellAdapter);
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
        String url = String.format(Locale.ENGLISH, "player/%s/spell/%d", selectedPlayer.getId(), psdDataSet[selectedSpellId].getId());
        HttpUtils.delete(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong retrieving spells from the server.");
                        return;
                    }

                    closeMenus();
                    getPlayerSpells();
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
        String url = String.format(Locale.ENGLISH, "player/%s/item/%d", selectedPlayer.getId(), pidDataSet[selectedItemId].getId());
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

        System.out.println("____________IM HERE");

        // Close fragments on top.
        int count = getSupportFragmentManager().getBackStackEntryCount();

        System.out.println(count);
        if (count > 0){
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    public void closeMenu(View view) {
        closeMenus();
    }

    public void showSpellPHB(View view) {
        closeMenus();

        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("REQUESTED_PAGE_NUMBER", psdDataSet[selectedSpellId].getPhb());
        startActivity(intent);
    }

    public void showItemPHB(View view) {
        closeMenus();

        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("REQUESTED_PAGE_NUMBER", pidDataSet[selectedSpellId].getPhb());
        startActivity(intent);
    }

    public void openItemFragment(View view) {
        openFragment(0);
    }

    public void openSpellFragment(View view) {
        openFragment(2);
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
            updatePlayerInfo(MyPlayerCharacterList.playerData.get(position).getId());
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
                if (psdDataSet.length == 0) {
                    Toast.makeText(this, "You have no spells.", Toast.LENGTH_SHORT).show();
                    return;
                }
                fragment = new PlayerSpellFragment();
                fragment.setEnterTransition(new Slide(Gravity.START));
                fragment.setExitTransition(new Slide(Gravity.END));
                break;
            case FRAGMENT_ID_ADD_SPELL: // Add Spell
                intent = new Intent(this, AddSpellActivity.class);
                startActivityForResult(intent, UPDATE_SPELL);
                drawerLayout.closeDrawer(leftDrawerWrapper);
                return;
            case FRAGMENT_ID_PLAYER_STATS: // Player Stats
                fragment = new StatsFragment();
                fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                fragment.setExitTransition(new Slide(Gravity.TOP));
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
