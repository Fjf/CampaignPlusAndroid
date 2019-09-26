package com.example.dndapp.Player;

import android.content.ClipData;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.dndapp.Player.Adapters.ItemInstantAutoCompleteAdapter;
import com.example.dndapp.Player.Adapters.ItemSpinnerArrayAdapter;
import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.InstantAutoComplete;
import com.example.dndapp._utils.PlayerItemData;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AddItemActivity extends AppCompatActivity {
    private static final String TAG = "AddItemActivity";

    private String[] items;
    private int[] ids;
    private String playerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner spinner = findViewById(R.id.dice_selection);


        final String[] dice = new String[]{"Select dice type...", "4", "6", "8", "10", "12", "20", "100"};
        ItemSpinnerArrayAdapter spinnerArrayAdapter = new ItemSpinnerArrayAdapter(this, R.layout.spinner_row, dice);

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_row);
        spinner.setAdapter(spinnerArrayAdapter);

        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        playerId = preferences.getString("player_id", null);

        try {
            getAllItems(playerId);
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void getAllItems(String playerId) throws UnsupportedEncodingException, JSONException {
        final AddItemActivity self = this;

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("player_id", playerId);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("getitems", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("items");
                    items = new String[array.length()];
                    ids = new int[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        items[i] = array.getJSONObject(i).getString("name");
                        ids[i] = array.getJSONObject(i).getInt("item_id");
                    }

//                    ItemInstantAutoCompleteAdapter arrayAdapter = new ItemInstantAutoCompleteAdapter(self, R.layout.item_selection_row, items);

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(self, android.R.layout.simple_list_item_1, items);

                    InstantAutoComplete textView = findViewById(R.id.autocomplete_items);
                    textView.setAdapter(arrayAdapter);
                    textView.setThreshold(1);
                } catch (JSONException e) {
                    Log.d(TAG, "Something went wrong retrieving data from the server.");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d(TAG, "Invalid response: " + response);
            }
        });
    }

    public void toggleWeaponInfo(android.view.View view) {

        if (((Switch) view).isChecked()) {
            findViewById(R.id.add_weapon_wrapper).setVisibility(View.VISIBLE);
        } else {
            // The original view.
            findViewById(R.id.add_weapon_wrapper).setVisibility(View.GONE);
        }
    }

    public void buttonAddItem(View view) {
        boolean isWeapon = ((Switch) findViewById(R.id.item_weapon_toggle)).isChecked();
        PlayerItemData pid = null;

        String name = ((EditText) findViewById(R.id.item_name)).getText().toString();
        String info = ((EditText) findViewById(R.id.item_information)).getText().toString();
        String amount = ((EditText) findViewById(R.id.item_amount)).getText().toString();

        // Dont allow empty strings.
        if (name.length() == 0 || info.length() == 0 || amount.length() == 0) return;

        if (!isWeapon) {
            pid = new PlayerItemData(name, info, Integer.parseInt(amount));
        } else {
            Spinner spinner = ((Spinner) findViewById(R.id.dice_selection));
            if (spinner.getSelectedItemPosition() == 0) return; // Not a selection.
            // TODO: Highlight in red all unfilled input fields.

            String damageType = "slashing"; // TODO: Add field for damage type, probably dropdown.
            String diceType = spinner.getSelectedItem().toString();
            String diceAmount = ((EditText) findViewById(R.id.dice_amount)).getText().toString();
            String flatDamage = ((EditText) findViewById(R.id.flat_damage)).getText().toString();

            pid = new PlayerItemData(name, info, Integer.parseInt(amount), damageType,
                    Integer.parseInt(diceAmount), Integer.parseInt(diceType), Integer.parseInt(flatDamage));
        }

        try {
            addItem(pid);
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private int strIndexOf(String[] items, String text) {
        String item;
        for (int i = 0; i < items.length; i++) {
            item = items[i];
            if (item.equals(text)) {
                return i;
            }

        }
        return -1;
    }

    public void playerAddItem(View view) throws JSONException, UnsupportedEncodingException {
        InstantAutoComplete instantAutoComplete = findViewById(R.id.autocomplete_items);
        String text = instantAutoComplete.getText().toString();
        EditText itemAmount = findViewById(R.id.add_items_amount);

        int idx = strIndexOf(items, text);
        if (idx == -1)
            return;

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("item_id", ids[idx]);
        data.put("amount", itemAmount.getText().toString());
        StringEntity entity = new StringEntity(data.toString());

        String url = String.format(Locale.ENGLISH, "player/%s/spell", playerId);
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    if (serverResp.getBoolean("success")) {
                        finish();
                    } else {
                        Log.d(TAG, "An error occured: " + serverResp.getString("error"));
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "Invalid response: " + response.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d(TAG, "Invalid response: " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String response = errorResponse == null ? null : errorResponse.toString();
                onFailure(statusCode, headers, response, throwable);
            }

        });
    }

    private void addItem(PlayerItemData pid) throws UnsupportedEncodingException, JSONException {
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", "-1");

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("player_id", playerId);
        data.put("name", pid.getName());
        data.put("extra_info", pid.getExtraInfo());
        data.put("amount", pid.getAmount());
        data.put("damage_type", pid.getDamageType());
        data.put("dice_type", pid.getDiceType());
        data.put("dice_amount", pid.getDiceAmount());
        data.put("flat_damage", pid.getFlatDamage());
        StringEntity entity = new StringEntity(data.toString());

//        HttpUtils.post("addplayeritem", entity, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                try {
//                    JSONObject serverResp = new JSONObject(response.toString());
//                    if (serverResp.getBoolean("success")) {
//                        finish();
//                    } else {
//                        Log.d(TAG, "An error occured: " + serverResp.getString("error"));
//                    }
//
//                } catch (JSONException e) {
//                    Log.d(TAG, "Invalid response: " + response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
//                Log.d(TAG, "Invalid response: " + response);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                String response = errorResponse == null ? null : errorResponse.toString();
//                onFailure(statusCode, headers, response, throwable);
//            }
//
//        });
    }
}
