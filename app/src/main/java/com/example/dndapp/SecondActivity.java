package com.example.dndapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class SecondActivity extends AppCompatActivity {
    private String TAG = "SecondActivity";
    private ListView playthroughsList;
    private PlaythroughListAdapter playthroughsListData;
    private String[] codes;

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
