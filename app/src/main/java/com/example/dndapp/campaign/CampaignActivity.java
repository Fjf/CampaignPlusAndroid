package com.example.dndapp.campaign;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.campaign.Adapters.PlayerListAdapter;
import com.example.dndapp.campaign.Fragments.ShowQRFragment;
import com.example.dndapp.R;
import com.example.dndapp._data.PlayerCharacterList;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.player.CreatePlayerActivity;
import com.example.dndapp.player.PlayerInfoActivity;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CampaignActivity extends AppCompatActivity {
    private static final int CREATE_CHARACTER_INTENT = 0;
    private final String TAG = "CampaignActivity";
    private ListView playerList;
    private Toolbar toolbar;
    private int playerId;
    private PlayerData[] playerDataArray;
    private int currentSelectedPlayer = -1;
    private String userName;
    private int campaignId;
    private View button;
    private String campaignCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playthrough);

        Intent intent = getIntent();
        campaignId = intent.getIntExtra("campaign_id", -1);
        campaignCode = intent.getStringExtra("campaign_code");
        String campaignName = intent.getStringExtra("campaign_name");

        SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        userName = preferences.getString("username", null);

        playerList = findViewById(R.id.playerList);
        getPlayers();

        // Attaching the layout to the toolbar object
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(campaignName);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playthrough, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CREATE_CHARACTER_INTENT) {
            // Player id can maybe be used here.
            assert data != null;
            int pid = data.getIntExtra("player_id", -1);

            try {
                addToPlaythrough(pid);
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToPlaythrough(int pid) throws JSONException, UnsupportedEncodingException {
        String url = String.format(Locale.ENGLISH, "player/%d/playthrough", pid);

        JSONObject data = new JSONObject();
        data.put("playthrough_code", campaignCode);
        StringEntity args = new StringEntity(data.toString());

        HttpUtils.put(url, args, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                getPlayers();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(CampaignActivity.this, errorResponse.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_show_phb) {
            Intent intent = new Intent(CampaignActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            Intent intent = new Intent(CampaignActivity.this, PlayerInfoActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_qr) {
            openFragment(ShowQRFragment.newInstance(campaignCode));
        }

        return super.onOptionsItemSelected(item);
    }

    public void createNewCharacter(View view) {
        Intent intent = new Intent(this, CreatePlayerActivity.class);
        startActivityForResult(intent, CREATE_CHARACTER_INTENT);
    }

    private void openFragment(ShowQRFragment fragment) {
        fragment.setEnterTransition(new Slide(Gravity.TOP));
        fragment.setExitTransition(new Slide(Gravity.BOTTOM));

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.playthrough_content_layout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void getPlayers() {
        String url = String.format(Locale.ENGLISH, "playthrough/%d/players", campaignId);
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    if (!response.getBoolean("success")) {
                        // Maybe do something here
                        return;
                    }

                    JSONArray array = response.getJSONArray("players");
                    PlayerCharacterList.setPlayerData(array);

                    updatePlayerList(PlayerCharacterList.playerData);
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

    private void updatePlayerList(final PlayerData[] entries) {
        playerDataArray = entries;

        PlayerListAdapter adapter = new PlayerListAdapter(this, entries, userName);
        playerList.setAdapter(adapter);
    }
}
