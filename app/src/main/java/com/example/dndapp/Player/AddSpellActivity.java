package com.example.dndapp.Player;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dndapp.Player.Adapters.SpellInstantAutoCompleteAdapter;
import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.InstantAutoComplete;
import com.example.dndapp._data.SpellData;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AddSpellActivity extends AppCompatActivity {

    private static final String TAG = "AddSpellActivity";
    private ArrayList<SpellData> spellDataArray;
    private SpellInstantAutoCompleteAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spell);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", null);

        try {
            getAllSpells(playerId);
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void getAllSpells(String playerId) throws UnsupportedEncodingException, JSONException {
        final AddSpellActivity self = this;

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("player_id", playerId);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("getspells", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        // TODO: Add error message for user.
                        Log.d(TAG, "Spell retrieval wsa unsuccessful.");
                        return;
                    }

                    JSONArray array = response.getJSONArray("spells");
                    spellDataArray = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        spellDataArray.add(new SpellData(array.getJSONObject(i)));
                    }

                    arrayAdapter = new SpellInstantAutoCompleteAdapter(self, spellDataArray);

//                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(self, android.R.layout.simple_list_item_1, items);

                    InstantAutoComplete textView = findViewById(R.id.autocomplete_spells);
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

    public void playerAddSpell(View view) throws JSONException, UnsupportedEncodingException {
        InstantAutoComplete instantAutoComplete = findViewById(R.id.autocomplete_spells);
        String text = instantAutoComplete.getText().toString();

        int id = -1;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            SpellData data = (SpellData) arrayAdapter.getItem(i);
            assert data != null;
            if (data.getName().equals(text)) {
                id = data.getId();
                break;
            }
        }
        if (id == -1) {
            // Invalid ID found.
            return;
        }

        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", "-1");

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("player_id", playerId);
        data.put("spell_id", id);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("addplayerspell", entity, new JsonHttpResponseHandler() {
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

}
