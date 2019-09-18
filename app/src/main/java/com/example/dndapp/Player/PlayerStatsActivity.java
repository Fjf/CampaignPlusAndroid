package com.example.dndapp.Player;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.PlayerStatsData;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
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

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                if (x1 > x2) { // Left swipe
                    Intent intent = new Intent(PlayerStatsActivity.this, PlayerInfoActivity.class);
                    startActivity(intent);
                }
        }
        return false;
    }
}
