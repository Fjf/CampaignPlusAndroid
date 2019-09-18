package com.example.dndapp.Player;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlayerSpellActivity extends AppCompatActivity {
    private static final String TAG = "PlayerSpellActivity";
    private float x1;
    private float x2;
    private SpellData[] psdDataSet;
    private int currentSpellId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentSpellId = getIntent().getIntExtra("SPELL_ID", -1);

        // Animation because this is to the right of the player info activity.
        this.overridePendingTransition(R.anim.from_right,
                R.anim.to_right);

        setContentView(R.layout.activity_player_spell);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            getSpells();
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
                        if (x1 > x2) { // Right swipe
                            Intent intent = new Intent(PlayerSpellActivity.this, PlayerInfoActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void fillSpellData(SpellData current) {
        TextView ct = findViewById(R.id.spell_info_casting_time);
        TextView hl = findViewById(R.id.spell_info_higher_level);
        TextView de = findViewById(R.id.spell_info_description);
        TextView co = findViewById(R.id.spell_info_components);
        TextView du = findViewById(R.id.spell_info_duration);
        TextView le = findViewById(R.id.spell_info_level);
        TextView na = findViewById(R.id.spell_info_name);

        String description = "test";
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
        na.setText(current.getName());
    }

    private void getSpells() throws JSONException, UnsupportedEncodingException {
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

                    psdDataSet = new SpellData[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        obj = array.getJSONObject(i);
                        psdDataSet[i] = new SpellData(obj);
                    }

                    // TODO: Give better feedback than this.
                    // Maybe add dummy data so it will fill in the blanks at least.
                    if (psdDataSet.length == 0) {
                        Toast t = Toast.makeText(getApplicationContext(), "You dont have any spells.", Toast.LENGTH_LONG);
                        t.show();
                        finish();
                    }

                    // Default spell information is the first entry
                    SpellData current = psdDataSet[0];
                    for (SpellData sd : psdDataSet) {
                        if (sd.getId() == currentSpellId) {
                            current = sd;
                        }
                    }

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
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
