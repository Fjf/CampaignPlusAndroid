package com.example.dndapp.Playthrough;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.Player.PlayerInfoActivity;
import com.example.dndapp.Playthrough.Adapters.PlayerListAdapter;
import com.example.dndapp.R;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlaythroughActivity extends AppCompatActivity {
    private final String TAG = "PlaythroughActivity";
    private String code;
    private ListView playerList;
    private Toolbar toolbar;
    private int playerId;
    private PlayerData[] playerDataArray;
    private int currentSelectedPlayer = -1;
    private String userName;
    private int playthroughId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playthrough);

        Intent intent = getIntent();
        code = intent.getStringExtra("code");

        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        userName = preferences.getString("name", null);
        playthroughId = preferences.getInt("playthrough_id", -1);

        playerList = findViewById(R.id.playerList);

        getPlayers();

        setHapticFeedback();

        // Attaching the layout to the toolbar object
        toolbar = findViewById(R.id.toolbar);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);
    }

    private void setHapticFeedback() {
        View.OnTouchListener vot = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                return false;
            }
        };

        findViewById(R.id.player_field_reload).setOnTouchListener(vot);
        findViewById(R.id.player_field_upload).setOnTouchListener(vot);
        findViewById(R.id.player_delete_button).setOnTouchListener(vot);
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

    private void getPlayers() {
        String url = String.format(Locale.ENGLISH, "playthrough/%d/players", playthroughId);
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    if (!response.getBoolean("success")) {
                        // Maybe do something here
                        return;
                    }

                    JSONArray array = response.getJSONArray("players");
                    PlayerData[] entries = new PlayerData[array.length()];

                    for (int i = 0; i < array.length(); i++) {
                        entries[i] = new PlayerData(array.getJSONObject(i));

                        // Store the player's id in sharedpreferences for later information retrieval.
                        if (entries[i].getUserName().equals(userName)) {
                            SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();

                            playerId = entries[i].getId();

                            editor.putString("player_id", String.valueOf(playerId));
                            editor.apply();
                        }
                    }

                    updatePlayerList(entries);

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
        Log.d(TAG, "Player id: " + currentSelectedPlayer);

//        Intent intent = new Intent(this, PlayerInfoActivity.class);
//        startActivity(intent);
    }

    private void updatePlayerList(final PlayerData[] entries) {
        playerDataArray = entries;

        PlayerListAdapter adapter = new PlayerListAdapter(this, entries, userName);
        playerList.setAdapter(adapter);
        playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentSelectedPlayer = position;

                setPlayerInfoField();
            }
        });
    }

    private void setPlayerInfoField() {
        // Cant set player info if nothing is selected.
        if (currentSelectedPlayer == -1)
            return;

        TextInputEditText pnf = findViewById(R.id.player_name_field);
        TextInputEditText prf = findViewById(R.id.player_race_field);
        TextInputEditText pcf = findViewById(R.id.player_class_field);
        TextInputEditText pbf = findViewById(R.id.player_backstory_field);

        pnf.setText(playerDataArray[currentSelectedPlayer].getName());
        prf.setText(playerDataArray[currentSelectedPlayer].getRace());
        pcf.setText(playerDataArray[currentSelectedPlayer].getClassName());
        pbf.setText(playerDataArray[currentSelectedPlayer].getBackstory());

        // Dont show update button when its not your player.
        if (playerDataArray[currentSelectedPlayer].getUserName().equals(userName)) {
            findViewById(R.id.player_field_upload).setVisibility(View.VISIBLE);
            findViewById(R.id.player_delete_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.player_field_upload).setVisibility(View.GONE);
            findViewById(R.id.player_delete_button).setVisibility(View.GONE);
        }

        // TODO: Fix the disabling of textviews of characters which are not yours.
//        for (TextInputEditText t : new TextInputEditText[]{pnf, prf, pcf, pbf}) {
//            t.setEnabled(clickable);
//            t.setClickable(clickable);
//        }
    }


    private PlayerData getPlayerInfoField() {
        TextInputEditText pnf = findViewById(R.id.player_name_field);
        TextInputEditText prf = findViewById(R.id.player_race_field);
        TextInputEditText pcf = findViewById(R.id.player_class_field);
        TextInputEditText pbf = findViewById(R.id.player_backstory_field);

        PlayerData pd = new PlayerData(playerId, pnf.getText().toString(), pcf.getText().toString(), prf.getText().toString());
        pd.setBackstory(pbf.getText().toString());

        return pd;
    }

    public void updatePlayerInfo(View view) throws JSONException, UnsupportedEncodingException {
        // Dont upload if no player is selected.
        if (currentSelectedPlayer == -1) {
            Toast.makeText(this, "No player selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        PlayerData pd = getPlayerInfoField();
        JSONObject obj = pd.toJSON();
        StringEntity entity = new StringEntity(obj.toString());

        String url = String.format(Locale.ENGLISH, "player/%d", playerId);

        findViewById(R.id.player_field_upload).setEnabled(false);
        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(PlaythroughActivity.this, "Playerdata updated successfully.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                findViewById(R.id.player_field_upload).setEnabled(true);

                getPlayers();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                // auto-generated stub
            }
        });
    }

    public void revertPlayerInfo(View view) {
        setPlayerInfoField();
    }

    public void deletePlayer(View view) {
        findViewById(R.id.player_delete_button).setEnabled(false);

        String url = String.format(Locale.ENGLISH, "player/%s", playerId);

        HttpUtils.delete(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(PlaythroughActivity.this, "Player deleted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PlaythroughActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                findViewById(R.id.player_delete_button).setEnabled(true);

                getPlayers();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                // auto-generated stub
            }
        });
    }

    public void createNewCharacter(View view) throws JSONException, UnsupportedEncodingException {
        JSONObject obj = new PlayerData(-1, "-", "-", "-").toJSON();
        StringEntity entity = new StringEntity(obj.toString());

        findViewById(R.id.player_create_new_button).setEnabled(false);

        String url = String.format(Locale.ENGLISH, "playthrough/%d/players", playthroughId);

        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(PlaythroughActivity.this, "New player created successfully.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                findViewById(R.id.player_create_new_button).setEnabled(true);

                getPlayers();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                // auto-generated stub
            }
        });
    }
}
