package com.example.dndapp.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import com.example.dndapp.player.Adapters.SpellInstantAutoCompleteAdapter;
import com.example.dndapp.R;
import com.example.dndapp._data.SpellData;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.dndapp.player.PlayerInfoActivity.selectedPlayer;

public class AddSpellActivity extends AppCompatActivity {

    private static final String TAG = "AddSpellActivity";
    private ArrayList<SpellData> spellDataArray;
    private SpellInstantAutoCompleteAdapter arrayAdapter;
    private int campaignId;
    private int playerId;

    private AutoCompleteTextView spellInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spell);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        playerId = preferences.getInt("player_id", -1);
        campaignId = preferences.getInt("campaign_id", -1);

        spellInput = findViewById(R.id.autocomplete_spells);

        getAllSpells();
    }

    private void getAllSpells() {
        final AddSpellActivity self = this;
        HttpUtils.get("user/spells", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray spells) {
                try {
                    spellDataArray = new ArrayList<>();
                    for (int i = 0; i < spells.length(); i++) {
                        spellDataArray.add(new SpellData(spells.getJSONObject(i), true));
                    }

                    arrayAdapter = new SpellInstantAutoCompleteAdapter(self, spellDataArray);

                    AutoCompleteTextView textView = findViewById(R.id.autocomplete_spells);
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
        String text = spellInput.getText().toString();

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

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("spell_id", id);
        StringEntity entity = new StringEntity(data.toString());

        String url = String.format(Locale.ENGLISH, "player/%s/spells", playerId);
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                finish();
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

    @Override
    protected void onPause() {
        // Hide the keyboard when the spell activity finishes.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(spellInput.getWindowToken(), 0);
        super.onPause();
    }
}
