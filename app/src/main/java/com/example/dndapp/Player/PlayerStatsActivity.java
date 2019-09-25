package com.example.dndapp.Player;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.R;
import com.example.dndapp._data.StatsData;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.PlayerStatsData;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlayerStatsActivity extends AppCompatActivity {
    private static final String TAG = "PlayerStatsActivity";
    private String playerId;
    private float x1;
    private float x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_stats);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        playerId = preferences.getString("player_id", null);

        try {
            getPlayerData();
            getRemotePlayerProficiencies();
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    /**
     * @param str Input string
     * @return The converted string, or -1 if no convertible number was given.
     */
    private int tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void updateStats(View view) throws UnsupportedEncodingException {
        String st = ((TextView)findViewById(R.id.totStr)).getText().toString();
        String de = ((TextView)findViewById(R.id.totDex)).getText().toString();
        String co = ((TextView)findViewById(R.id.totCon)).getText().toString();
        String wi = ((TextView)findViewById(R.id.totWis)).getText().toString();
        String in = ((TextView)findViewById(R.id.totInt)).getText().toString();
        String ch = ((TextView)findViewById(R.id.totCha)).getText().toString();

        PlayerStatsData psd = new PlayerStatsData(
                tryParseInt(st),
                tryParseInt(de),
                tryParseInt(co),
                tryParseInt(wi),
                tryParseInt(in),
                tryParseInt(ch)
        );

        JSONObject object;
        try {
            object = psd.toJSON();
            object.put("player_id", playerId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        StringEntity entity = new StringEntity(object.toString());
        String url = "setplayerinfo";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success"))
                        Log.d(TAG, "Something went wrong updating stats on the server.");
                    else
                        finish();
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

    public JSONObject getPlayerProficiencies() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("acrobatics", ((CheckBox) findViewById(R.id.proficiency_acr)).isChecked());
        obj.put("animal_handling", ((CheckBox) findViewById(R.id.proficiency_anh)).isChecked());
        obj.put("arcana", ((CheckBox) findViewById(R.id.proficiency_arc)).isChecked());
        obj.put("athletics", ((CheckBox) findViewById(R.id.proficiency_ath)).isChecked());
        obj.put("deception", ((CheckBox) findViewById(R.id.proficiency_dec)).isChecked());
        obj.put("history", ((CheckBox) findViewById(R.id.proficiency_his)).isChecked());
        obj.put("insight", ((CheckBox) findViewById(R.id.proficiency_ins)).isChecked());
        obj.put("intimidation", ((CheckBox) findViewById(R.id.proficiency_intimidation)).isChecked());
        obj.put("investigation", ((CheckBox) findViewById(R.id.proficiency_inv)).isChecked());
        obj.put("medicine", ((CheckBox) findViewById(R.id.proficiency_med)).isChecked());
        obj.put("nature", ((CheckBox) findViewById(R.id.proficiency_nat)).isChecked());
        obj.put("perception", ((CheckBox) findViewById(R.id.proficiency_perception)).isChecked());
        obj.put("performance", ((CheckBox) findViewById(R.id.proficiency_per)).isChecked());
        obj.put("persuasion", ((CheckBox) findViewById(R.id.proficiency_persuasion)).isChecked());
        obj.put("religion", ((CheckBox) findViewById(R.id.proficiency_rel)).isChecked());
        obj.put("sleight_of_hand", ((CheckBox) findViewById(R.id.proficiency_soh)).isChecked());
        obj.put("stealth", ((CheckBox) findViewById(R.id.proficiency_ste)).isChecked());
        obj.put("survival", ((CheckBox) findViewById(R.id.proficiency_sur)).isChecked());

        return obj;
    }

    public void setPlayerProficiencies(JSONObject obj) throws JSONException {
        ((CheckBox) findViewById(R.id.proficiency_acr)).setChecked(obj.getBoolean("acrobatics"));
        ((CheckBox) findViewById(R.id.proficiency_anh)).setChecked(obj.getBoolean("animal_handling"));
        ((CheckBox) findViewById(R.id.proficiency_arc)).setChecked(obj.getBoolean("arcana"));
        ((CheckBox) findViewById(R.id.proficiency_ath)).setChecked(obj.getBoolean("athletics"));
        ((CheckBox) findViewById(R.id.proficiency_dec)).setChecked(obj.getBoolean("deception"));
        ((CheckBox) findViewById(R.id.proficiency_his)).setChecked(obj.getBoolean("history"));
        ((CheckBox) findViewById(R.id.proficiency_ins)).setChecked(obj.getBoolean("insight"));
        ((CheckBox) findViewById(R.id.proficiency_intimidation)).setChecked(obj.getBoolean("intimidation"));
        ((CheckBox) findViewById(R.id.proficiency_inv)).setChecked(obj.getBoolean("investigation"));
        ((CheckBox) findViewById(R.id.proficiency_med)).setChecked(obj.getBoolean("medicine"));
        ((CheckBox) findViewById(R.id.proficiency_nat)).setChecked(obj.getBoolean("nature"));
        ((CheckBox) findViewById(R.id.proficiency_perception)).setChecked(obj.getBoolean("perception"));
        ((CheckBox) findViewById(R.id.proficiency_per)).setChecked(obj.getBoolean("performance"));
        ((CheckBox) findViewById(R.id.proficiency_persuasion)).setChecked(obj.getBoolean("persuasion"));
        ((CheckBox) findViewById(R.id.proficiency_rel)).setChecked(obj.getBoolean("religion"));
        ((CheckBox) findViewById(R.id.proficiency_soh)).setChecked(obj.getBoolean("sleight_of_hand"));
        ((CheckBox) findViewById(R.id.proficiency_ste)).setChecked(obj.getBoolean("stealth"));
        ((CheckBox) findViewById(R.id.proficiency_sur)).setChecked(obj.getBoolean("survival"));
    }

    public void updatePlayerProficiencies(View view) throws JSONException, UnsupportedEncodingException {
        findViewById(R.id.loading_bar).setVisibility(View.VISIBLE);

        JSONObject obj = getPlayerProficiencies();

        StringEntity entity = new StringEntity(obj.toString());
        String url = String.format(Locale.ENGLISH, "player/%s/proficiencies", playerId);
        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onFinish() {
                findViewById(R.id.loading_bar).setVisibility(View.GONE);
                super.onFinish();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success"))
                        Toast.makeText(PlayerStatsActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                    else
                        finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, errorResponse.toString());
            }
        });
    }

    private void getPlayerData() {
        String url = String.format(Locale.ENGLISH, "player/%s/data", playerId);

        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Toast.makeText(PlayerStatsActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StatsData data = new StatsData(response.getJSONObject("info"));
                    setStats(data);
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

    private void setStats(StatsData data) {
        EditText et;

        et = findViewById(R.id.totCha);
        et.setText(data.getCharisma());

        et = findViewById(R.id.totDex);
        et.setText(data.getDexterity());

        et = findViewById(R.id.totStr);
        et.setText(data.getStrength());

        et = findViewById(R.id.totInt);
        et.setText(data.getIntelligence());

        et = findViewById(R.id.totWis);
        et.setText(data.getWisdom());

        et = findViewById(R.id.totCon);
        et.setText(data.getConstitution());
    }

    public void getRemotePlayerProficiencies() throws JSONException, UnsupportedEncodingException {
        String url = String.format(Locale.ENGLISH, "player/%s/proficiencies", playerId);

        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Toast.makeText(PlayerStatsActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setPlayerProficiencies(response.getJSONObject("proficiencies"));
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
