package com.example.campaignplus.campaign;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.MapData;
import com.example.campaignplus._data.PlayerData;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus.campaign.Adapters.MapSelectionAdapter;
import com.example.campaignplus.campaign.Adapters.PlayerListAdapter;
import com.example.campaignplus.campaign.Fragments.ShowQRFragment;
import com.example.campaignplus.player.PlayerInfoActivity;
import com.otaliastudios.zoom.ZoomImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.entity.StringEntity;

public class MapViewActivity extends AppCompatActivity {
    private static final int CREATE_CHARACTER_INTENT = 0;
    private final String TAG = "CampaignActivity";
    private ListView playerList;
    private String userName;
    private int campaignId;
    private String campaignCode;

    private MapData rootMap;
    private MapData currentMap;
    private ArrayList<String> mapNameList = new ArrayList<>();
    private MapSelectionAdapter drawerMapListAdapter;

    public MapViewActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);

        // Attaching the layout to the toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Map View");
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);

        Intent intent = getIntent();
        campaignId = intent.getIntExtra("campaign_id", -1);

        rootMap = new MapData(campaignId, new CallBack() {
            @Override
            public void success() {
                selectMap(rootMap);
            }

            @Override
            public void error(String errorMessage) {

            }
        });


        RecyclerView mapItems = findViewById(R.id.left_drawer_items);
        mapItems.setLayoutManager(new LinearLayoutManager(this));
        drawerMapListAdapter = new MapSelectionAdapter(mapNameList, item -> {
            for (MapData child : currentMap.children) {
                if (child.name.equals(item)) {
                    selectMap(child);

                    DrawerLayout drawer = findViewById(R.id.campaign_content_layout);
                    ScrollView drawerWrapper = findViewById(R.id.left_drawer_wrapper);
                    drawer.closeDrawer(drawerWrapper);
                    return;
                }
            }
        });
        mapItems.setAdapter(drawerMapListAdapter);

        setSupportActionBar(toolbar);
        findViewById(R.id.back_to_parent_button).setOnClickListener(view -> {
            backToParent();
        });

    }

    private void backToParent() {
        selectMap(currentMap.parent);
    }

    private void selectMap(MapData data) {
        currentMap = data;
        getImage();
        setDrawerLocations();

        boolean hasParent = currentMap.parent != null;
        findViewById(R.id.back_to_parent_button).setVisibility(hasParent ? View.VISIBLE : View.INVISIBLE);
    }

    private void setDrawerLocations() {
        drawerMapListAdapter.notifyItemRangeRemoved(0, mapNameList.size());
        mapNameList.clear();
        for (MapData child : currentMap.children) {
            mapNameList.add(child.name);
        }

        RecyclerView mapItems = findViewById(R.id.left_drawer_items);
        mapItems.post(() -> {
            drawerMapListAdapter.notifyItemRangeInserted(0, mapNameList.size());
            mapItems.smoothScrollToPosition(0);
        });
    }


    private void getImage() {
        MapView mapView = findViewById(R.id.map_container);
        mapView.setOnMapSelectedCallback(mapData -> selectMap(mapData));

        currentMap.fetchImage(new CallBack() {
            @Override
            public void success() {
                runOnUiThread(() -> {
                    mapView.setBitmap(currentMap.bitmap);
                    mapView.setChildMaps(currentMap.children);
                    mapView.resetZoom();
                });
            }

            @Override
            public void error(String errorMessage) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_campaign, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_CHARACTER_INTENT) {
            // Player id can maybe be used here.
            assert data != null;
            int pid = data.getIntExtra("player_id", -1);

            try {
                addToCampaign(pid);
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToCampaign(int pid) throws JSONException, UnsupportedEncodingException {
        String url = String.format(Locale.ENGLISH, "player/%d/campaign", pid);

        JSONObject data = new JSONObject();
        data.put("campaign_code", campaignCode);

        HttpUtils.put(url, data.toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MapViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> getPlayers());
                } else {
                    runOnUiThread(() -> Toast.makeText(MapViewActivity.this, response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.campaign_content_layout);
        ScrollView drawerWrapper = findViewById(R.id.left_drawer_wrapper);
        if (id == android.R.id.home) {
            drawer.openDrawer(drawerWrapper);
        }
        if (id == R.id.action_showpc) {
            Intent intent = new Intent(MapViewActivity.this, PlayerInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_qr) {
            openFragment(ShowQRFragment.newInstance(campaignCode));
        }

        return super.onOptionsItemSelected(item);
    }


    private void openFragment(ShowQRFragment fragment) {
        fragment.setEnterTransition(new Slide(Gravity.TOP));
        fragment.setExitTransition(new Slide(Gravity.BOTTOM));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.campaign_content_layout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void getPlayers() {
        String url = String.format(Locale.ENGLISH, "campaign/%d/players", campaignId);
        HttpUtils.get(url, new okhttp3.Callback() {
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
                            // Maybe do something here
                            return;
                        }

                        JSONArray array = jsonObject.getJSONArray("players");
                        DataCache.setPlayerData(array);

                        runOnUiThread(() -> updatePlayerList(DataCache.playerData));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Invalid response: " + response.message());
                }
            }
        });

    }

    private void updatePlayerList(final ArrayList<PlayerData> entries) {
        PlayerListAdapter adapter = new PlayerListAdapter(this, entries, userName);
        playerList.setAdapter(adapter);
    }
}
