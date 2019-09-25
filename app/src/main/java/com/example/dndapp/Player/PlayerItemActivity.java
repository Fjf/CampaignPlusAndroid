package com.example.dndapp.Player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.R;
import com.example.dndapp._data.ItemData;
import com.example.dndapp._data.ItemType;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlayerItemActivity extends AppCompatActivity {

    private float x1;
    private float x2;

    private ItemData[] pidDataSet;
    private int currentItemId;
    private String TAG = "PlayerItemActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentItemId = getIntent().getIntExtra("ITEM_ID", -1);

        this.overridePendingTransition(R.anim.from_left, R.anim.to_left);

        setContentView(R.layout.activity_player_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getItems();
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        findViewById(R.id.view_scroll_bar).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        if (Math.abs(x1 - x2) < 100) {
                            v.performClick();
                            return true;
                        }
                        if (x1 < x2) { // Left swipe
                            openPlayerInfoActivity(v);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_left);
    }

    private void getItems() throws JSONException, UnsupportedEncodingException {
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

        StringEntity entity = new StringEntity(data.toString());
        String url = "getplayeritems";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong retrieving spells from the server.");
                        return;
                    }

                    JSONArray array = response.getJSONArray("items");
                    JSONObject obj;

                    pidDataSet = new ItemData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);

                        ItemType type = getItemType(obj.getString("category"));

                        pidDataSet[i] = new ItemData(obj, type);
                    }

                    // TODO: Give better feedback than this.
                    // Maybe add dummy data so it will fill in the blanks at least.
                    if (pidDataSet.length == 0) {
                        Toast t = Toast.makeText(getApplicationContext(), "You dont have any items.", Toast.LENGTH_LONG);
                        t.show();
                        finish();
                        return;
                    }

                    // Default spell information is the first entry
                    ItemData current = pidDataSet[0];
                    for (ItemData sd : pidDataSet) {
                        if (sd.getId() == currentItemId) {
                            current = sd;
                        }
                    }

                    createItemDropdown();

                    fillItemData(current);
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

    private ItemType getItemType(String category) {
        if (category.equals("Weapon"))
            return ItemType.WEAPON;

        // Fallback.
        return ItemType.ITEM;
    }


    private void createItemDropdown() {
        int selection = 0;
        String[] users = new String[pidDataSet.length];
        for (int i = 0; i < pidDataSet.length; i++) {
            users[i] = pidDataSet[i].getName();
            if (pidDataSet[i].getId() == currentItemId)
                selection = i;
        }

        Spinner spin = findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_big_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(selection);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillItemData(pidDataSet[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void fillItemData(ItemData itemData) {
        TextView na = findViewById(R.id.item_info_value);
        TextView am = findViewById(R.id.item_info_amount);

        na.setText(itemData.getNormalValue());
        am.setText(String.valueOf(itemData.getAmount()));

        if (itemData.getType() == ItemType.WEAPON) {
            hideAllBut(R.id.item_weapon_info);

            TextView da = findViewById(R.id.weapon_info_damage);
            TextView dt = findViewById(R.id.weapon_info_damage_type);
            TextView ra = findViewById(R.id.weapon_info_range);
            TextView tr = findViewById(R.id.weapon_info_throw_range);

            da.setText(itemData.getNormalDamage());
            dt.setText(itemData.getDamageType());
            ra.setText(itemData.getNormalRange());
            tr.setText(itemData.getNormalThrowRange());
        } else {
            hideAllBut(0);
        }
    }

    public void openPlayerInfoActivity(View v) {
        finish();
    }

    public void openPlayerSpellActivity(View v) {
        Intent intent = new Intent(PlayerItemActivity.this, PlayerSpellActivity.class);
        startActivity(intent);
        finish();
    }

    private void hideAllBut(int showId) {
        int[] ids = new int[]{R.id.item_weapon_info};

        for (int id : ids) {
            if (id == showId)
                findViewById(id).setVisibility(View.VISIBLE);
            else
                findViewById(id).setVisibility(View.GONE);
        }
    }
}
