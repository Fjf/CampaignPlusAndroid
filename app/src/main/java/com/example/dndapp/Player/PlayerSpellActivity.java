package com.example.dndapp.Player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.R;
import com.example.dndapp._data.SpellData;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class PlayerSpellActivity extends AppCompatActivity {
    private static final String TAG = "PlayerSpellActivity";
    private float x1;
    private float x2;
    private SpellData[] psdDataSet;
    private int currentSpellId;
    private String playerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.from_right, R.anim.to_right);

        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        playerId = preferences.getString("player_id", null);


        currentSpellId = getIntent().getIntExtra("SPELL_ID", -1);

        setContentView(R.layout.activity_player_spell);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSpells();

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
                        if (x1 > x2) { // Right swipe
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
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.from_left, R.anim.to_left);
    }

    private void fillSpellData(SpellData current) {
        TextView ct = findViewById(R.id.spell_info_casting_time);
        TextView hl = findViewById(R.id.spell_info_higher_level);
        TextView de = findViewById(R.id.spell_info_description);
        TextView co = findViewById(R.id.spell_info_components);
        TextView du = findViewById(R.id.spell_info_duration);
        TextView le = findViewById(R.id.spell_info_level);

        String description = "null";
        try {
            description = new String(current.getDescription().getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ct.setText(current.getCastingTime());
        hl.setText(current.getHigherLevel());
        de.setText(description);
        co.setText(current.getComponents());
        du.setText(current.getDuration());
        le.setText(Integer.toString(current.getLevel()));
    }

    private void getSpells() {
        String url = String.format(Locale.ENGLISH, "player/%s/spell", playerId);
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

                    psdDataSet = new SpellData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        psdDataSet[i] = new SpellData(obj);
                    }

                    if (psdDataSet.length == 0) {
                        Toast t = Toast.makeText(getApplicationContext(), "You dont have any spells.", Toast.LENGTH_LONG);
                        t.show();
                        finish();
                        return;
                    }

                    // Default spell information is the first entry
                    SpellData current = psdDataSet[0];
                    for (SpellData sd : psdDataSet) {
                        if (sd.getId() == currentSpellId) {
                            current = sd;
                        }
                    }

                    createSpellDropdown();

                    fillSpellData(current);

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

    public void openPlayerInfoActivity(View view) {
        finish();
    }

    public void openPlayerItemActivity(View view) {
        Intent intent = new Intent(PlayerSpellActivity.this, PlayerItemActivity.class);
        startActivity(intent);
        finish();
    }

    private void createSpellDropdown() {
        int selection = 0;
        String[] users = new String[psdDataSet.length];
        for (int i = 0; i < psdDataSet.length; i++) {
            users[i] = psdDataSet[i].getName();
            if (psdDataSet[i].getId() == currentSpellId)
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
                fillSpellData(psdDataSet[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_phb) {
            Intent intent = new Intent(PlayerSpellActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            // Player info is always the parent.
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
