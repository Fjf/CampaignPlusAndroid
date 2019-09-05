package com.example.dndapp.Playthrough;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.Player.Adapters.PlayerListAdapter;
import com.example.dndapp.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlaythroughActivity extends AppCompatActivity {
    private final String TAG = "PlaythroughActivity";
    private String code;
    private ListView playerList;

    private String[] names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playthrough);

        Intent intent = getIntent();
        code = intent.getStringExtra("code");

        playerList = (ListView)findViewById(R.id.playerList);

        try {
            getPlayers();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void getPlayers() throws UnsupportedEncodingException {
        JSONObject data = new JSONObject();
        try {
            data.put("playthrough_code", code);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        StringEntity entity = new StringEntity(data.toString());
        String url = "getplayers";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                List<PlayerArrayElement> entries = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        String pn = response.getJSONObject(i).getString("name");
                        String un = response.getJSONObject(i).getString("user_name");
                        String cn = response.getJSONObject(i).getString("class");
                        entries.add(new PlayerArrayElement(pn, un, cn));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }

                updatePlayerList(entries);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d(TAG, "Invalid response: " + response);
            }
        });
    }

    private void updatePlayerList(List<PlayerArrayElement> entries) {
        PlayerListAdapter adapter = new PlayerListAdapter(this, entries);
        playerList.setAdapter(adapter);
    }
}
