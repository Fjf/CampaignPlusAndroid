package com.example.dndapp.Main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.dndapp.Login.MainActivity;
import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.Player.PlayerInfoActivity;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.Playthrough.PlaythroughActivity;
import com.example.dndapp.Playthrough.Adapters.PlaythroughListAdapter;
import com.example.dndapp.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SecondActivity extends AppCompatActivity {
    private String TAG = "SecondActivity";
    private ListView playthroughsList;
    private PlaythroughListAdapter playthroughsListData;
    private String[] codes;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        playthroughsList = (ListView)findViewById(R.id.playthroughs);
        playthroughsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                if (position > codes.length) {
                    throw new IndexOutOfBoundsException("Code array index out of bounds.");
                }
                Intent intent = new Intent(SecondActivity.this, PlaythroughActivity.class);
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
            Intent intent = new Intent(SecondActivity.this, PdfViewerActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_showpc) {
            Intent intent = new Intent(SecondActivity.this, PlayerInfoActivity.class);
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
        Log.d(TAG, data.toString());
        playthroughsListData = new PlaythroughListAdapter(this, data);

        playthroughsList.setAdapter(playthroughsListData);

    }
}
