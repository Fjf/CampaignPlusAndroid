package com.example.dndapp.campaign;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.player.PlayerInfoActivity;
import com.example.dndapp.campaign.Adapters.PlaythroughListAdapter;
import com.example.dndapp.campaign.Fragments.SelectPlayerFragment;
import com.example.dndapp.R;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CampaignOverviewActivity extends AppCompatActivity {
    private static final int QR_CODE = 0;
    private String TAG = "CampaignOverviewActivity";
    private ListView playthroughsList;
    private PlaythroughListAdapter playthroughsListData;
    private String[] codes;
    private String[] names;
    private Toolbar toolbar;
    private Spinner playerSpinner;
    private ProgressBar progressBar;
    private String playthroughCode = "";
    private EditText et;
    private int[] ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playthrough_overview);

        et = findViewById(R.id.playthrough_code);

        progressBar = findViewById(R.id.loading_bar);
        playerSpinner = findViewById(R.id.player_spinner);

        playthroughsList = findViewById(R.id.playthroughs);
        playthroughsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                if (position > codes.length) {
                    throw new IndexOutOfBoundsException("Code array index out of bounds.");
                }

                // Start campaign overview.
                Intent intent = new Intent(CampaignOverviewActivity.this, CampaignActivity.class);
                intent.putExtra("campaign_id", ids[position]);
                intent.putExtra("campaign_code", codes[position]);
                intent.putExtra("campaign_name", names[position]);
                startActivity(intent);
            }
        });

        getJoinedPlaythroughs();

        // Attaching the layout to the toolbar object
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("All Playthroughs");
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_phb) {
            Intent intent = new Intent(CampaignOverviewActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            Intent intent = new Intent(CampaignOverviewActivity.this, PlayerInfoActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getJoinedPlaythroughs() {
        String url = "getjoinedplaythroughs";
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    names = new String[response.length()];
                    codes = new String[response.length()];
                    ids = new int[response.length()];
                    // Iterate over all playthrough entries in list.
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject entry = response.getJSONObject(i);
                        names[i] = entry.getString("name");
                        ids[i] = entry.getInt("id");
                        codes[i] = entry.getString("code");
                    }
                    updatePlaythroughsList(names);
                } catch (JSONException e) {
                    Log.d(TAG, "Invalid response: " + response);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d(TAG, "Invalid response: " + response);
            }
        });
    }

    private void updatePlaythroughsList(String[] data) {
        playthroughsListData = new PlaythroughListAdapter(this, data);

        playthroughsList.setAdapter(playthroughsListData);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_CODE) {
            if(resultCode == RESULT_OK) {
                String playthrough_code = data.getStringExtra("QR_CODE");
                if (playthrough_code.length() != 6)
                    return;

                playthroughCode = playthrough_code;
                et.setText(playthroughCode);

                if (!validateCode(playthroughCode)) {
                    Toast.makeText(this, "This code is invalid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                openSelectPlayerFragment();
            }
        }
    }

    public void addPlayerPlaythroughButton(View view) {
        try {
            joinPlaythrough(playthroughCode);
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void joinPlaythrough(String code) throws UnsupportedEncodingException, JSONException {
        final JSONObject data = new JSONObject();
        data.put("playthrough_code", code);
        StringEntity entity = new StringEntity(data.toString());

        String url = "joinplaythrough";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Close fragment.
                CampaignOverviewActivity.this.getFragmentManager().popBackStackImmediate();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(CampaignOverviewActivity.this, "This playthrough does not exist.", Toast.LENGTH_SHORT).show();
                // Close fragment.
                CampaignOverviewActivity.this.getFragmentManager().popBackStackImmediate();
            }
        });
    }

    public void joinPlaythroughButton(View view) {
        playthroughCode = et.getText().toString();
        if (!validateCode(playthroughCode)) {
            Toast.makeText(this, "This code is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        openSelectPlayerFragment();
    }

    private boolean validateCode(String code) {
        return code.length() == 6;
    }

    private void openSelectPlayerFragment() {
        progressBar.setVisibility(View.VISIBLE);

        // Load all your player characters from database.
        String url = "user/players";
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
                // Stop the loading bar
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { progressBar.setVisibility(View.GONE); }
                });
                super.onPreProcessResponse(instance, response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Toast.makeText(CampaignOverviewActivity.this, "Something went wrong obtaining players.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONArray arr = response.getJSONArray("players");
                    MyPlayerCharacterList.setPlayerData(arr);


                    SelectPlayerFragment fragment = SelectPlayerFragment.newInstance(playthroughCode);
                    fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                    fragment.setExitTransition(new Slide(Gravity.TOP));

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();

                    ft.replace(R.id.playthrough_content_layout, fragment);
                    ft.addToBackStack(null);
                    ft.commit();

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



    public void startQRScanner(View view) {
        Intent intent = new Intent(CampaignOverviewActivity.this, QRCodeScannerActivity.class);
        startActivityForResult(intent, QR_CODE);
    }

    public void eatClickEvent(View view) { }
}
