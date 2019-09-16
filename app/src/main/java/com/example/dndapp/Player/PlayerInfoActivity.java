package com.example.dndapp.Player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.Player.Adapters.ItemListAdapter;
import com.example.dndapp.Player.Adapters.SpellListAdapter;
import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.PlayerItemData;
import com.example.dndapp._utils.PlayerSpellData;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlayerInfoActivity extends AppCompatActivity {
    private static final String TAG = "PlayerInfoActivity";

    private RecyclerView itemRecyclerView;
    private RecyclerView.Adapter itemAdapter;
    private RecyclerView.LayoutManager itemLayoutManager;

    private RecyclerView spellRecyclerView;
    private RecyclerView.Adapter spellAdapter;
    private RecyclerView.LayoutManager spellLayoutManager;

    private Toolbar toolbar;

    private final int UPDATE_STATS = 0;
    private final int UPDATE_ITEMS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_info);


        // Attaching the layout to the toolbar object
        toolbar = findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);

        // Get all information and items.
        try {
            getPlayerData();
            getPlayerItems();
            getPlayerSpells();
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }

        itemRecyclerView = findViewById(R.id.player_item_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        itemRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        itemLayoutManager = new LinearLayoutManager(this);
        itemRecyclerView.setLayoutManager(itemLayoutManager);

        spellRecyclerView = findViewById(R.id.player_spell_list);
        spellRecyclerView.setHasFixedSize(true);

        spellLayoutManager = new LinearLayoutManager(this);
        spellRecyclerView.setLayoutManager(spellLayoutManager);


    }

    public void switchViewAddItem(android.view.View view) {
        Intent intent = new Intent(PlayerInfoActivity.this, AddItemActivity.class);
        startActivityForResult(intent, UPDATE_ITEMS);
    }

    public void switchViewUpdateStats(View view) {
        Intent intent = new Intent(PlayerInfoActivity.this, PlayerStatsActivity.class);
        startActivityForResult(intent, UPDATE_STATS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_ITEMS) {
            // After adding an item, update the item list from the server.
            try {
                getPlayerItems();
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        } else if (requestCode == UPDATE_STATS) {
            // After updating your stats, update player stats.
            try {
                getPlayerData();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_phb) {
            Intent intent = new Intent(PlayerInfoActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            // TODO: Decide what to do here.
//            Intent intent = new Intent(PlayerInfoActivity.this, PlayerInfoActivity.class);
//            startActivity(intent);
//            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getPlayerItems() throws UnsupportedEncodingException, JSONException {
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", null);

        // No player was selected yet.
        // TODO: Tell the user to select a character or playthrough.
        if (playerId == null)
            return;

        JSONObject data = new JSONObject();
        data.put("player_id", playerId);


        final Context self = this;
        StringEntity entity = new StringEntity(data.toString());
        String url = "getplayeritems";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong retrieving items from server.");
                        return;
                    }

                    JSONArray array = response.getJSONArray("items");
                    JSONObject obj;

                    PlayerItemData[] pidDataSet = new PlayerItemData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        pidDataSet[i] = new PlayerItemData(obj);
                    }

                    itemAdapter = new ItemListAdapter(pidDataSet);
                    itemRecyclerView.setAdapter(itemAdapter);

                    itemRecyclerView.addOnItemTouchListener(
                        new RecyclerItemClickListener(self, itemRecyclerView,new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Intent intent = new Intent(PlayerInfoActivity.this, PdfViewerActivity.class);
                                intent.putExtra("REQUESTED_PAGE_NUMBER", 143);
                                startActivity(intent);
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                // do whatever
                            }
                            })
                    );
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

    private void getPlayerData() throws UnsupportedEncodingException {
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", null);

        // No player was selected yet.
        // TODO: Tell the user to select a character or playthrough.
        if (playerId == null)
            return;

        JSONObject data = new JSONObject();
        try {
            data.put("player_id", playerId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringEntity entity = new StringEntity(data.toString());
        String url = "getplayerdata";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println(response.toString());

                TextView tv;
                RadioButton rb;
                try {
                    JSONObject info = (JSONObject) response.get("info");

                    // Base stats.
                    tv = findViewById(R.id.statStrengthTotal);
                    tv.setText(info.getString("strength"));

                    tv = (TextView) findViewById(R.id.statConstitutionTotal);
                    tv.setText(info.getString("constitution"));


                    tv = findViewById(R.id.statDexterityTotal);
                    tv.setText(info.getString("dexterity"));

                    tv = findViewById(R.id.statWisdomTotal);
                    tv.setText(info.getString("wisdom"));

                    tv = findViewById(R.id.statCharismaTotal);
                    tv.setText(info.getString("charisma"));

                    tv = findViewById(R.id.statIntelligenceTotal);
                    tv.setText(info.getString("intelligence"));

                    // Saving throws.
                    if   (info.getBoolean("saving_throws_str")) {
                        rb = findViewById(R.id.stStrength);
                        rb.toggle();
                    } if (info.getBoolean("saving_throws_dex")) {
                        rb = findViewById(R.id.stDexterity);
                        rb.toggle();
                    } if (info.getBoolean("saving_throws_con")) {
                        rb = findViewById(R.id.stConstitution);
                        rb.toggle();
                    } if (info.getBoolean("saving_throws_wis")) {
                        rb = findViewById(R.id.stWisdom);
                        rb.toggle();
                    } if (info.getBoolean("saving_throws_int")) {
                        rb = findViewById(R.id.stIntelligence);
                        rb.toggle();
                    } if (info.getBoolean("saving_throws_cha")) {
                        rb = findViewById(R.id.stCharisma);
                        rb.toggle();
                    }

                    // All other information
                    tv = findViewById(R.id.statArmorClass);
                    tv.setText(info.getString("armor_class"));

                    tv = findViewById(R.id.statMaxHP);
                    tv.setText(info.getString("max_hp"));

//                    tv = findViewById(R.id.statSpeed);
//                    tv.setText(info.getString("speed"));
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

    private void getPlayerSpells() throws UnsupportedEncodingException, JSONException {
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", null);

        // No player was selected yet.
        // TODO: Tell the user to select a character or playthrough.
        if (playerId == null) {
            Log.d(TAG, "There was not player selected yet.");
            return;
        }

        JSONObject data = new JSONObject();
        data.put("player_id", playerId);


        final Context self = this;
        StringEntity entity = new StringEntity(data.toString());
        String url = "getplayerspells";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong retrieving spells from the server.");
                        return;
                    }

                    JSONArray array = response.getJSONArray("spells");
                    JSONObject obj;

                    final PlayerSpellData[] psdDataSet = new PlayerSpellData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        psdDataSet[i] = new PlayerSpellData(obj);
                    }

                    spellAdapter = new SpellListAdapter(psdDataSet);
                    spellRecyclerView.setAdapter(spellAdapter);

                    spellRecyclerView.addOnItemTouchListener(
                        new RecyclerItemClickListener(self, spellRecyclerView,new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Intent intent = new Intent(PlayerInfoActivity.this, PdfViewerActivity.class);
                                intent.putExtra("REQUESTED_PAGE_NUMBER", psdDataSet[position].getPhb());
                                startActivity(intent);
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                // do whatever
                            }
                        })
                    );
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
}
