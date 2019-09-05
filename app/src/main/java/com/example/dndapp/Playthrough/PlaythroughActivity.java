package com.example.dndapp.Playthrough;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.dndapp.Main.SecondActivity;
import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.Player.PlayerInfoActivity;
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
    private Toolbar toolbar;

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

        // Attaching the layout to the toolbar object
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_phb) {
            Intent intent = new Intent(PlaythroughActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            Intent intent = new Intent(PlaythroughActivity.this, PlayerInfoActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        final SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        final String userName = preferences.getString("name", null);

        if (userName == null) {
            // Should not be able to come here without logging in.
            return;
        }

        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                List<PlayerArrayElement> entries = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        String pn = response.getJSONObject(i).getString("name");
                        String un = response.getJSONObject(i).getString("user_name");
                        String cn = response.getJSONObject(i).getString("class");

                        // Store the player's id in sharedpreferences for later information retrieval.
                        if (un.equals(userName)) {
                            String id = response.getJSONObject(i).getString("id");
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("player_id", id);
                            editor.apply();
                        }

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
