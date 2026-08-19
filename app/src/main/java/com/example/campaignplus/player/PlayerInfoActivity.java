package com.example.campaignplus.player;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.campaignplus.PdfViewerActivity;
import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.DrawerListData;
import com.example.campaignplus._data.MyPlayerCharacterList;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.IgnoreCallback;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus.campaign.CampaignOverviewActivity;
import com.example.campaignplus.login.LoginActivity;
import com.example.campaignplus.login.UserService.UserService;
import com.example.campaignplus.player.Adapters.DrawerListAdapter;
import com.example.campaignplus.player.Adapters.DrawerPCListAdapter;
import com.example.campaignplus.player.MainFragments.ClassInformationFragment;
import com.example.campaignplus.player.Fragments.AddSpellFragment;
import com.example.campaignplus.player.Fragments.SpellInfoFragment;
import com.example.campaignplus.player.Fragments.StatsFragment;
import com.example.campaignplus.player.Fragments.TableInfoFragment;
import com.example.campaignplus.player.Fragments.CreateSpellFragment;
import com.example.campaignplus.player.MainFragments.ItemViewFragment;
import com.example.campaignplus.player.MainFragments.PlayerViewFragment;
import com.example.campaignplus.player.MainFragments.SpellViewFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class PlayerInfoActivity extends AppCompatActivity {

    private static final String TAG = "PlayerInfoActivity";
    private final int UPDATE_STATS = 0;
    private final int UPDATE_SPELL = 2;
    private final int CREATE_PLAYER_RESULT = 5;

    private SharedPreferences preferences;

    private DrawerLayout drawerLayout;
    private ListView leftDrawerList;
    private ListView leftDrawerPCList;
    private View leftDrawerWrapper;
    private DrawerPCListAdapter drawerPCListAdapter;
    private BottomNavigationView bottomNavigation;
    private androidx.viewpager2.widget.ViewPager2 viewPager;
    private PlayerViewPagerAdapter viewPagerAdapter;

    public PlayerInfoActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_info);

        // Attaching the layout to the toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Character Sheet");
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);
        setSupportActionBar(toolbar);

        SharedPreferences tempPrefs = getSharedPreferences("PlayerData_default", MODE_PRIVATE);
        int defaultPlayerId = tempPrefs.getInt("player_id", -1);
        if (selectedPlayer != null && selectedPlayer.getId() != -1) {
            preferences = getSharedPreferences("PlayerData_" + selectedPlayer.getId(), MODE_PRIVATE);
        } else if (defaultPlayerId != -1) {
            preferences = getSharedPreferences("PlayerData_" + defaultPlayerId, MODE_PRIVATE);
        } else {
            preferences = tempPrefs;
        }

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
        drawerPCListAdapter = new DrawerPCListAdapter(PlayerInfoActivity.this, R.layout.left_drawer_menu_item, DataCache.playerData);
        leftDrawerPCList.setAdapter(drawerPCListAdapter);

        justifyListViewHeightBasedOnChildren(leftDrawerList);

        updatePlayerList();
        /*
         * Setup ViewPager2 for swipeable pages
         * Note: Adapter will be initialized after player data is loaded
         */
        viewPager = findViewById(R.id.view_pager);

        /*
         * Bottom bar programmatically switching between views
         */
        bottomNavigation = findViewById(R.id.bottom_navigation);
        // We are currently in the player account view, so we have to enable this one.
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            if (viewPagerAdapter == null) {
                return false;
            }
            int position = viewPagerAdapter.getPositionForItemId(item.getItemId());
            if (viewPager.getCurrentItem() != position) {
                viewPager.setCurrentItem(position);
            }
            return true;
        });

        // Sync ViewPager with Bottom Navigation
        viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (viewPagerAdapter != null) {
                    int itemId = viewPagerAdapter.getItemIdForPosition(position);
                    bottomNavigation.setSelectedItemId(itemId);
                }
            }
        });

    }

    private void updatePlayerList() {
        /*
         * Get playerlist from server
         */
        MyPlayerCharacterList.initialize(new CallBack() {
            @Override
            public void success() {
                if (DataCache.playerData.size() == 0) {
                    Toast.makeText(PlayerInfoActivity.this, "You have no player characters.", Toast.LENGTH_SHORT).show();
                    return;
                }
                updatePlayerInfo(preferences.getInt("player_id", -1));
                updatePlayerDrawer();
            }

            @Override
            public void error(String errorMessage) {
                // Server error.
            }
        });
    }

    private void openDefaultFragment() {
        if (viewPagerAdapter != null) {
            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (isFinishing()) {
            return;
        }
        
        Intent intent = getIntent();
        int playerId = -1;
        if (intent != null) {
            playerId = intent.getIntExtra("player_id", -1);
        }

        if (playerId == -1 && preferences != null) {
            playerId = preferences.getInt("player_id", -1);
        }
        final int pid = playerId;

        MyPlayerCharacterList.updatePlayerData(new CallBack() {
            @Override
            public void success() {
                updatePlayerInfo(pid);
            }

            @Override
            public void error(String errorMessage) {
                Log.e(TAG, "Failed to update player data: " + errorMessage);
                runOnUiThread(() -> {
                    HttpUtils.showToast(PlayerInfoActivity.this, "Failed to load player data: " + errorMessage, Toast.LENGTH_LONG);
                });
            }
        });
        updatePlayerDrawer();
    }

    public void updatePlayerDrawer() {
        drawerPCListAdapter.notifyDataSetChanged();
        justifyListViewHeightBasedOnChildren(leftDrawerPCList);
    }

    @SuppressLint("RestrictedApi")
    private void updatePlayerInfo(int id) {
        Log.d(TAG, "Updating player info for ID: " + id);
        selectedPlayer = DataCache.getPlayer(id);
        Log.d(TAG, "Selected player: " + (selectedPlayer != null ? selectedPlayer.getName() : "null"));

        // Save current page position before switching
        final int currentPage;
        if (viewPagerAdapter != null) {
            currentPage = viewPager.getCurrentItem();
        } else {
            currentPage = 0;
        }

        bottomNavigation.getMenu().findItem(R.id.page_account).setTitle(selectedPlayer.getName());

        if (selectedPlayer.getId() != -1) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("player_id", selectedPlayer.getId());
            editor.apply();
        }

        selectedPlayer.updatePlayerData(new CallBack() {
            @Override
            public void success() {
                Log.d(TAG, "Player data updated successfully");
                runOnUiThread(() -> {
                    // Recreate ViewPager adapter when player changes
                    if (selectedPlayer != null) {
                        viewPagerAdapter = new PlayerViewPagerAdapter(PlayerInfoActivity.this, selectedPlayer);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setOffscreenPageLimit(1);
                    }
                    // Restore the current page instead of always going to page 0
                    if (viewPagerAdapter != null) {
                        int maxPage = viewPagerAdapter.getItemCount() - 1;
                        int targetPage = Math.min(currentPage, maxPage);
                        viewPager.setCurrentItem(targetPage, false);
                        int itemId = viewPagerAdapter.getItemIdForPosition(targetPage);
                        bottomNavigation.setSelectedItemId(itemId);
                    }
                });
            }

            @Override
            public void error(String errorMessage) {
                Log.e(TAG, "Failed to update player data: " + errorMessage);
                runOnUiThread(() -> {
                    HttpUtils.showToast(PlayerInfoActivity.this, errorMessage, Toast.LENGTH_SHORT);
                });
            }
        });
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
            requestDeletePlayer();
        } else if (id == R.id.action_update_app) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(HttpUtils.getUrl() + "/app"));
            startActivity(browserIntent);
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
            UserService.logout(new CallBack() {
                @Override
                public void success() {
                    Intent intent = new Intent(PlayerInfoActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);

                    String[] toClearPreferences = {"LoginData"};
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
                    HttpUtils.showToast(PlayerInfoActivity.this, errorMessage, Toast.LENGTH_SHORT);

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

        if (requestCode == UPDATE_STATS) {
            // After updating your stats, update player stats.
            selectedPlayer.updatePlayerData(new IgnoreCallback());
        } else if (requestCode == UPDATE_SPELL) {
            Log.d("------------------------", "got response");

            // After adding a spell, update spells list.
        } else if (requestCode == CREATE_PLAYER_RESULT) {
            final int playerId = data.getIntExtra("player_id", -1);
            if (playerId == -1)
                return;

            MyPlayerCharacterList.updatePlayerData(new CallBack() {
                @Override
                public void success() {
                    updatePlayerDrawer();
                }

                @Override
                public void error(String errorMessage) {
                    // Nothing here?
                }
            });
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // Overwrite previous intent when navigating back to this activity.
        this.setIntent(intent);
        super.onNewIntent(intent);
    }

    private void requestDeletePlayer() {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    deletePlayer();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void deletePlayer() {
        String url = String.format(Locale.ENGLISH, "player/%s", selectedPlayer.getId());
        HttpUtils.delete(url, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                // Handle error
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        MyPlayerCharacterList.updatePlayerData(new CallBack() {
                            @Override
                            public void success() {
                                updatePlayerInfo(DataCache.playerData.get(0).getId());
                                updatePlayerDrawer();
                            }

                            @Override
                            public void error(String errorMessage) {

                            }
                        });
                    });
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        // Close fragments on top.
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        }
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

        final int FRAGMENT_ID_ADD_ITEM = 0;
        final int FRAGMENT_ID_CREATE_SPELL = 1;
        final int FRAGMENT_ID_SPELL = 2;
        final int FRAGMENT_ID_SPELL_SLOTS = 3;
        final int FRAGMENT_ID_PLAYER_STATS = 4;

        switch (position) {
            case FRAGMENT_ID_ADD_ITEM:
                intent = new Intent(this, CreateItemActivity.class);
                startActivity(intent);
                return;
            case FRAGMENT_ID_CREATE_SPELL:
                fragment = new CreateSpellFragment();
                fragment.setEnterTransition(new Slide(Gravity.START));
                fragment.setExitTransition(new Slide(Gravity.END));
                break;
            case FRAGMENT_ID_SPELL: // My Spells
                if (selectedPlayer.getSpells().size() == 0) {
                    Toast.makeText(this, "You have no spells.", Toast.LENGTH_SHORT).show();
                    return;
                }
                fragment = new SpellInfoFragment();
                fragment.setEnterTransition(new Slide(Gravity.START));
                fragment.setExitTransition(new Slide(Gravity.END));
                break;
            case FRAGMENT_ID_PLAYER_STATS: // Player Stats
                fragment = new StatsFragment();
                fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                fragment.setExitTransition(new Slide(Gravity.TOP));
                break;
            case FRAGMENT_ID_SPELL_SLOTS: // Player spell slots
                fragment = new TableInfoFragment();
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
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }
}
