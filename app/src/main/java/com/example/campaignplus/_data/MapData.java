package com.example.campaignplus._data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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


        mapUrl = data.optString("filename");

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
        HttpUtils.get("campaigns/" + campaignId + "/maps", new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        setData(jsonObject);
                        callback.success();
                    } catch (JSONException e) {
                        callback.error(e.getMessage());
                    }
                } else {
                    callback.error(response.message());
                }
            }
        });
    }

    public void fetchImage(CallBack callback) {
        try {
            OkHttpClient client = new OkHttpClient();
            String imageUrl = HttpUtils.getUrl() + "/static/images/uploads/" + mapUrl;
            Log.d("MapData", imageUrl);
            Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.error(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        InputStream in = response.body().byteStream();
                        bitmap = BitmapFactory.decodeStream(in);
                        in.close();
                        if (bitmap == null) {
                            Log.e("MapData", "Failed to decode bitmap!");
                            callback.error("Failed to decode image!");
                        } else {
                            Log.d("MapData", "Bitmap decoded successfully: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                            callback.success();
                        }
                    } else {
                        callback.error(response.message());
                    }
                }
            });
        } catch (Exception e) {
            callback.error(e.getMessage());
        }
    }
}
