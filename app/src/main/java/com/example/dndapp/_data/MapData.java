package com.example.dndapp._data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.dndapp._utils.CallBack;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MapData {
    int id = -1;
    int campaignId;
    int parentMapId;

    String mapUrl;
    public Bitmap bitmap = null;

    public double x;
    public double y;
    public String name;
    public String story;

    boolean visible;
    public MapData parent = null;
    public ArrayList<MapData> children = new ArrayList<>();

    public MapData(JSONObject data) {
        super();
        setData(data);
    }

    public MapData(int campaignId, CallBack callback) {
        super();
        fetch(campaignId, callback);
    }

    private void setData(JSONObject data) {
        id = data.optInt("id");
        campaignId = data.optInt("campaign_id");
        parentMapId = data.optInt("parent_map_id");


        mapUrl = data.optString("map_url");

        x = data.optDouble("x");
        y = data.optDouble("y");
        name = data.optString("name");
        story = data.optString("story");

        visible = data.optBoolean("visible");

        JSONArray childrenData = data.optJSONArray("children");
        assert childrenData != null;

        children.clear();
        for (int i = 0; i < childrenData.length(); i++) {
            JSONObject childData = childrenData.optJSONObject(i);
            MapData child = new MapData(childData);
            child.parent = this;
            children.add(child);
        }
    }

    public void fetch(int campaignId, CallBack callback) {
        HttpUtils.get("campaigns/" + campaignId + "/maps", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                setData(response);
                callback.success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.error(responseString);
            }
        });
    }

    public void fetchImage(CallBack callback) {
        Thread thread = new Thread(() -> {
            InputStream in = null;
            int responseCode = -1;

            try {
                URL url = new URL(HttpUtils.getUrl() + mapUrl);
                Log.d("MapData", "Fetching Image @ " + url.toString());

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.connect();
                responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    in = con.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                }
                callback.success();
            } catch (Exception e) {
                callback.error(e.getMessage());
            }
        });
        thread.start();
    }
}
