package com.example.campaignplus.campaign;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.campaignplus.PdfViewerActivity;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus.player.PlayerInfoActivity;
import com.example.campaignplus.campaign.Adapters.CampaignListAdapter;
import com.example.campaignplus.campaign.Fragments.SelectPlayerFragment;
import com.example.campaignplus.R;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CampaignOverviewActivity extends AppCompatActivity {
    private static final int QR_CODE = 0;
    private final String TAG = "CampaignOverviewActivity";
    private ListView campaignList;
    private CampaignListAdapter campaignListData;
    private String[] codes;
    private String[] names;
    private Toolbar toolbar;
    private Spinner playerSpinner;
    private ProgressBar progressBar;
    private String campaignCode = "";
    private EditText et;
    private int[] ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign_overview);

        et = findViewById(R.id.campaign_code);

        progressBar = findViewById(R.id.loading_bar);
        playerSpinner = findViewById(R.id.player_spinner);

        campaignList = findViewById(R.id.campaigns);
        campaignList.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            if (position > codes.length) {
                throw new IndexOutOfBoundsException("Code array index out of bounds.");
            }

            // Start campaign overview.
            Intent intent = new Intent(CampaignOverviewActivity.this, CampaignActivity.class);
            intent.putExtra("campaign_id", ids[position]);
            intent.putExtra("campaign_code", codes[position]);
            intent.putExtra("campaign_name", names[position]);
            startActivity(intent);
        });

        getJoinedCampaigns();

        // Attaching the layout to the toolbar object
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("All Campaigns");
        // Setting toolbar as the ActionBar with setSupportActionBar() call
        setSupportActionBar(toolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_campaign, menu);
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

    private void getJoinedCampaigns() {
        HttpUtils.get("campaigns", new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "Invalid response: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        names = new String[jsonArray.length()];
                        codes = new String[jsonArray.length()];
                        ids = new int[jsonArray.length()];
                        // Iterate over all campaign entries in list.
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject entry = jsonArray.getJSONObject(i);
                            names[i] = entry.getString("name");
                            ids[i] = entry.getInt("id");
                            codes[i] = entry.getString("code");
                        }
                        updateCampaignsList(names);
                    } catch (JSONException e) {
                        Log.d(TAG, "Invalid response: " + responseBody);
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Invalid response: " + response.message());
                }
            }
        });
    }

    private void updateCampaignsList(String[] data) {
        campaignListData = new CampaignListAdapter(this, data);

        runOnUiThread(() -> {
            campaignList.setAdapter(campaignListData);
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_CODE) {
            if (resultCode == RESULT_OK) {
                String campaign_code = data.getStringExtra("QR_CODE");
                if (campaign_code != null && campaign_code.length() != 6)
                    return;

                campaignCode = campaign_code;
                et.setText(campaignCode);

                if (!validateCode(campaignCode)) {
                    Toast.makeText(this, "This code is invalid.", Toast.LENGTH_SHORT).show();
                    return;
                }

                openSelectPlayerFragment();
            }
        }
    }

    public void addPlayerCampaignButton(View view) {
        joinCampaign(campaignCode);
    }

    private void joinCampaign(String code) {
        String url = "campaigns/join/" + code;
        HttpUtils.post(url, null, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(CampaignOverviewActivity.this, "This campaign does not exist.", Toast.LENGTH_SHORT).show();
                    CampaignOverviewActivity.this.getSupportFragmentManager().popBackStackImmediate();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> CampaignOverviewActivity.this.getSupportFragmentManager().popBackStackImmediate());
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(CampaignOverviewActivity.this, "This campaign does not exist.", Toast.LENGTH_SHORT).show();
                        CampaignOverviewActivity.this.getSupportFragmentManager().popBackStackImmediate();
                    });
                }
            }
        });
    }


    public void joinCampaignButton(View view) {
        campaignCode = et.getText().toString();
        if (!validateCode(campaignCode)) {
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
        HttpUtils.get("user/players", new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "Invalid response: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        DataCache.setPlayerData(jsonArray);

                        SelectPlayerFragment fragment = SelectPlayerFragment.newInstance(campaignCode);
                        fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
                        fragment.setExitTransition(new Slide(Gravity.TOP));

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction ft = fragmentManager.beginTransaction();

                        ft.replace(R.id.campaign_content_layout, fragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "Invalid response: " + response.message());
                }
            }
        });

    }


    public void startQRScanner(View view) {
        Intent intent = new Intent(CampaignOverviewActivity.this, QRCodeScannerActivity.class);
        startActivityForResult(intent, QR_CODE);
    }

    public void eatClickEvent(View view) {
    }
}
