package com.example.dndapp.Player;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.dndapp.Playthrough.PlayerArrayElement;
import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlayerInfoActivity extends AppCompatActivity {
    private static final String TAG = "PlayerInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_info);


        try {
            getPlayerData();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getPlayerData() throws UnsupportedEncodingException {
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        String playerId = preferences.getString("player_id", null);

        Log.d(TAG, "--------- Player ID = "+ playerId);

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
                TextView tv;
                try {
                    JSONObject info = (JSONObject) response.get("info");

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
