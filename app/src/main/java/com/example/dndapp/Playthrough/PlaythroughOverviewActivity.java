package com.example.dndapp.Playthrough;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.Player.PlayerInfoActivity;
import com.example.dndapp.Playthrough.Adapters.SpinnerInitialTextAdapter;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.Playthrough.Adapters.PlaythroughListAdapter;
import com.example.dndapp.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PlaythroughOverviewActivity extends AppCompatActivity {
    private static final int QR_CODE = 0;
    private String TAG = "PlaythroughOverviewActivity";
    private ListView playthroughsList;
    private PlaythroughListAdapter playthroughsListData;
    private String[] codes;
    private Toolbar toolbar;
    private Spinner playerSpinner;
    private ProgressBar progressBar;
    private String playthroughCode = "";
    private EditText et;

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
                Intent intent = new Intent(PlaythroughOverviewActivity.this, PlaythroughActivity.class);
                intent.putExtra("code", codes[position]);
                startActivity(intent);
            }
        });

        getJoinedPlaythroughs();

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
            Intent intent = new Intent(PlaythroughOverviewActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            Intent intent = new Intent(PlaythroughOverviewActivity.this, PlayerInfoActivity.class);
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
                    String[] data = new String[response.length()];
                    codes = new String[response.length()];
                    // Iterate over all playthrough entries in list.
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject entry = response.getJSONObject(i);
                        data[i] = entry.getString("name");
                        codes[i] = entry.getString("code");
                    }
                    updatePlaythroughsList(data);
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

    public void joinPlaythroughButton(View view) {
        final EditText et = findViewById(R.id.playthrough_code);
        findViewById(R.id.select_pc_overlay).setVisibility(View.GONE);

        try {
            joinPlaythrough(et.getText().toString());
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
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong joining this playthrough   .");
                        // TODO: Give user feedback about this.
                        return;
                    }
                    et.setText("");
                    getJoinedPlaythroughs();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_CODE) {
            if(resultCode == RESULT_OK) {
                String playthrough_code = data.getStringExtra("QR_CODE");
                if (playthrough_code.length() != 6)
                    return;

                playthroughCode = playthrough_code;
                et.setText(playthroughCode);
                selectPlayerCharacter();
            }
        }
    }

    private void selectPlayerCharacter() {
        progressBar.setVisibility(View.VISIBLE);

        final Context self = this;
        // Load all your player characters from database.
        String url = "getuserplayers";
        HttpUtils.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong obtaining players.");
                        // TODO: Give user feedback about this.
                        return;
                    }

                    JSONArray arr = response.getJSONArray("players");

                    // Show or don't show the player dropdown spinner if there is no data yet.
                    if (arr.length() > 0) {
                        PlayerData[] items = new PlayerData[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            items[i] = new PlayerData(obj);
                        }

                        ArrayAdapter<PlayerData> arrayAdapter = new ArrayAdapter<>(self, android.R.layout.simple_spinner_item, items);
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        final SpinnerInitialTextAdapter adapter = new SpinnerInitialTextAdapter(arrayAdapter, R.layout.player_spinner_nothing_selected_row,
                                // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                                self);
                        playerSpinner.setPrompt("Select your PC");

                        playerSpinner.setAdapter(adapter);

                        playerSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                PlayerData item = (PlayerData) adapter.getItem(position);
                                if (item == null)
                                    return;

                                try {
                                    updatePlayerPlaythrough(item.getId());
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                        playerSpinner.setVisibility(View.VISIBLE);
                    } else {
                        playerSpinner.setVisibility(View.GONE);
                    }

                    // Show the overlay and stop the loading bar
                    progressBar.setVisibility(View.GONE);
                    findViewById(R.id.select_pc_overlay).setVisibility(View.VISIBLE);
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

    private void updatePlayerPlaythrough(int id) throws UnsupportedEncodingException, JSONException {
        findViewById(R.id.select_pc_overlay).setVisibility(View.GONE);

        final JSONObject data = new JSONObject();
        data.put("player_id", id);
        data.put("playthrough_code", playthroughCode);
        StringEntity entity = new StringEntity(data.toString());

        String url = "setplayerplaythrough";
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Log.d(TAG, "Something went wrong updating your player's playthrough.");
                        // TODO: Give user feedback about this.
                        return;
                    }
                    et.setText("");

                    getJoinedPlaythroughs();
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
        Intent intent = new Intent(PlaythroughOverviewActivity.this, QRCodeScannerActivity.class);
        startActivityForResult(intent, QR_CODE);
    }
}
