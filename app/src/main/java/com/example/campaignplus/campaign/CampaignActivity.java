package com.example.campaignplus.campaign;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.transition.Slide;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.campaignplus._data.DataCache;
import com.example.campaignplus.campaign.Adapters.PlayerListAdapter;
import com.example.campaignplus.campaign.Fragments.SelectPlayerFragment;
import com.example.campaignplus.campaign.Fragments.ShowQRFragment;
import com.example.campaignplus.R;
import com.example.campaignplus._data.PlayerData;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus.player.CreatePlayerActivity;
import com.example.campaignplus.player.PlayerInfoActivity;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CampaignActivity extends AppCompatActivity {
    private static final int CREATE_CHARACTER_INTENT = 0;
    private final String TAG = "CampaignActivity";
    private ListView playerList;
    private String userName;
    private int campaignId;
    private String campaignCode;

    public CampaignActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);

        Intent intent = getIntent();
        campaignId = intent.getIntExtra("campaign_id", -1);
        campaignCode = intent.getStringExtra("campaign_code");
        String campaignName = intent.getStringExtra("campaign_name");

        SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        userName = preferences.getString("username", null);

        playerList = findViewById(R.id.playerList);
        updatePlayerList(DataCache.getPlayers(campaignId));

        // Attaching the layout to the toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(campaignName);
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);

        setEventListeners();
    }

    private void setEventListeners() {
        findViewById(R.id.player_add_existing_button).setOnClickListener(view -> {
            Fragment fragment = SelectPlayerFragment.newInstance(campaignCode);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.campaign_content_layout, fragment);
            ft.addToBackStack(null);
            ft.commit();
        });
        findViewById(R.id.player_save_created).setOnClickListener(view -> {
            Intent intent = new Intent(this, CreatePlayerActivity.class);
            startActivityForResult(intent, CREATE_CHARACTER_INTENT);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_campaign, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_CHARACTER_INTENT) {
            if (data == null) // Player returned without creating a character
                return;

            int pid = data.getIntExtra("player_id", -1);

            try {
                addToCampaign(pid);
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToCampaign(int pid) throws JSONException, UnsupportedEncodingException {
        String url = String.format(Locale.ENGLISH, "player/%d/campaign", pid);

        JSONObject data = new JSONObject();
        data.put("campaign_code", campaignCode);
        StringEntity args = new StringEntity(data.toString());

        HttpUtils.put(url, args, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    DataCache.updatePlayer(new PlayerData(response));
                    updatePlayerList(DataCache.getPlayers(campaignId));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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

        if (id == R.id.action_show_map) {
            Intent intent = new Intent(CampaignActivity.this, MapViewActivity.class);
            intent.putExtra("campaign_id", this.campaignId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            Intent intent = new Intent(CampaignActivity.this, PlayerInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_show_qr) {
            openFragment(ShowQRFragment.newInstance(campaignCode));
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFragment(ShowQRFragment fragment) {
        fragment.setEnterTransition(new Slide(Gravity.TOP));
        fragment.setExitTransition(new Slide(Gravity.BOTTOM));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.campaign_content_layout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void updatePlayerList(final ArrayList<PlayerData> entries) {
        PlayerListAdapter adapter = new PlayerListAdapter(this, entries, userName);
        playerList.setAdapter(adapter);
    }
}
