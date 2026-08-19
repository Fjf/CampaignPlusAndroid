package com.example.campaignplus.campaign;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.MapData;
import com.example.campaignplus._data.PlayerData;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus.campaign.Adapters.MapSelectionAdapter;
import com.example.campaignplus.campaign.Adapters.PlayerListAdapter;
import com.example.campaignplus.campaign.Fragments.ShowQRFragment;
import com.example.campaignplus.player.PlayerInfoActivity;
import com.otaliastudios.zoom.ZoomEngine;
import com.otaliastudios.zoom.ZoomImageView;
import android.graphics.Matrix;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MapViewActivity extends AppCompatActivity {
    private static final int CREATE_CHARACTER_INTENT = 0;
    private final String TAG = "CampaignActivity";
    private ListView playerList;
    private String userName;
    private int campaignId;
    private String campaignCode;

    private MapData rootMap;
    private MapData currentMap;
    private ArrayList<String> mapNameList = new ArrayList<>();
    private MapSelectionAdapter drawerMapListAdapter;
    private ArrayList<PinView> pinViews = new ArrayList<>();
    private ZoomImageView mapView;
    private FrameLayout pinOverlay;
    private Runnable updateRunnable;

    private static class PinView {
        ImageView pin;
        TextView label;
        double x;
        double y;

        PinView(ImageView pin, TextView label, double x, double y) {
            this.pin = pin;
            this.label = label;
            this.x = x;
            this.y = y;
        }
    }

    public MapViewActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapview);

        // Attaching the layout to the toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Map View");
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);

        Intent intent = getIntent();
        campaignId = intent.getIntExtra("campaign_id", -1);

        rootMap = new MapData(campaignId, new CallBack() {
            @Override
            public void success() {
                runOnUiThread(() -> selectMap(rootMap));
            }

            @Override
            public void error(String errorMessage) {
                runOnUiThread(() -> {
                    // Handle error if needed
                });
            }
        });


        RecyclerView mapItems = findViewById(R.id.left_drawer_items);
        mapItems.setLayoutManager(new LinearLayoutManager(this));
        drawerMapListAdapter = new MapSelectionAdapter(mapNameList, item -> {
            for (MapData child : currentMap.children) {
                if (child.name.equals(item)) {
                    selectMap(child);

                    DrawerLayout drawer = findViewById(R.id.campaign_content_layout);
                    ScrollView drawerWrapper = findViewById(R.id.left_drawer_wrapper);
                    drawer.closeDrawer(drawerWrapper);
                    return;
                }
            }
        });
        mapItems.setAdapter(drawerMapListAdapter);

        setSupportActionBar(toolbar);
        findViewById(R.id.back_to_parent_button).setOnClickListener(view -> {
            backToParent();
        });

        findViewById(R.id.back_to_character_button).setOnClickListener(view -> {
            Intent backIntent = new Intent(MapViewActivity.this, PlayerInfoActivity.class);
            backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(backIntent);
            finish();
        });

        pinOverlay = findViewById(R.id.pin_overlay);
    }

    private void backToParent() {
        selectMap(currentMap.parent);
    }

    private void selectMap(MapData data) {
        currentMap = data;
        getImage();
        setDrawerLocations();

        boolean hasParent = currentMap.parent != null;
        findViewById(R.id.back_to_parent_button).setVisibility(hasParent ? View.VISIBLE : View.INVISIBLE);
    }

    private void setDrawerLocations() {
        drawerMapListAdapter.notifyItemRangeRemoved(0, mapNameList.size());
        mapNameList.clear();
        for (MapData child : currentMap.children) {
            mapNameList.add(child.name);
        }

        RecyclerView mapItems = findViewById(R.id.left_drawer_items);
        mapItems.post(() -> {
            drawerMapListAdapter.notifyItemRangeInserted(0, mapNameList.size());
            mapItems.smoothScrollToPosition(0);
        });
    }


    private void getImage() {
        mapView = findViewById(R.id.map_container);
        pinViews.clear();
        
        if (updateRunnable != null) {
            mapView.removeCallbacks(updateRunnable);
        }

        currentMap.fetchImage(new CallBack() {
            @Override
            public void success() {
                runOnUiThread(() -> {
                    if (currentMap.bitmap == null) {
                        Log.e("MapView", "Bitmap is null after success callback");
                        return;
                    }
                    BitmapDrawable image = new BitmapDrawable(getResources(), currentMap.bitmap);
                    mapView.setImageDrawable(image);

                    mapView.post(() -> {
                        Log.d("MapView", "Bitmap: " + (currentMap.bitmap != null) + ", Width: " + mapView.getWidth() + ", Height: " + mapView.getHeight());
                        Log.d("MapView", "Image dimensions: " + currentMap.bitmap.getWidth() + "x" + currentMap.bitmap.getHeight());
                        Log.d("MapView", "Children count: " + currentMap.children.size());
                        if (currentMap.bitmap != null && mapView.getWidth() > 0 && mapView.getHeight() > 0) {
                            addMapPins();
                        }
                    });
                });
            }

            @Override
            public void error(String errorMessage) {
                Log.e("MapView", "Error fetching image: " + errorMessage);
            }
        });

    }

    private void addMapPins() {
        pinViews.clear();
        
        // Clear existing pins from overlay
        pinOverlay.removeAllViews();

        for (MapData child : currentMap.children) {
            if (!child.visible) {
                continue;
            }

            ImageView pin = new ImageView(this);
            pin.setImageResource(R.drawable.location_pin);
            pin.setScaleType(ImageView.ScaleType.FIT_CENTER);

            int pinSize = (int) (48 * getResources().getDisplayMetrics().density);

            FrameLayout.LayoutParams pinParams = new FrameLayout.LayoutParams(pinSize, pinSize);
            pin.setLayoutParams(pinParams);

            TextView label = new TextView(this);
            label.setText(child.name);
            label.setTextColor(Color.WHITE);
            label.setTextSize(14);
            label.setGravity(Gravity.CENTER);
            label.setShadowLayer(3, 1, 1, Color.BLACK);
            label.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

            final MapData childData = child;
            pin.setOnClickListener(v -> {
                selectMap(childData);
                DrawerLayout drawer = findViewById(R.id.campaign_content_layout);
                ScrollView drawerWrapper = findViewById(R.id.left_drawer_wrapper);
                drawer.closeDrawer(drawerWrapper);
            });

            pin.setContentDescription(child.name);
            label.setContentDescription(child.name);

            pinOverlay.addView(pin);
            pinOverlay.addView(label);

            pinViews.add(new PinView(pin, label, child.x, child.y));
        }

        setupPinUpdateListeners();
        updatePinPositions(mapView.getEngine().getMatrix());
    }

    private void setupPinUpdateListeners() {
        if (mapView == null || currentMap == null || currentMap.bitmap == null) {
            return;
        }
        
        mapView.getEngine().addListener(new ZoomEngine.Listener() {
            @Override
            public void onUpdate(ZoomEngine engine, Matrix matrix) {
                updatePinPositions(matrix);
            }

            @Override
            public void onIdle(ZoomEngine engine) {
            }
        });
    }

    private void updatePinPositions(Matrix matrix) {
        if (mapView == null || pinOverlay == null || currentMap == null || currentMap.bitmap == null) {
            return;
        }

        int viewWidth = mapView.getWidth();
        int viewHeight = mapView.getHeight();
        int imageWidth = currentMap.bitmap.getWidth();
        int imageHeight = currentMap.bitmap.getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        float[] values = new float[9];
        matrix.getValues(values);
        float matrixScaleX = values[Matrix.MSCALE_X];
        float matrixScaleY = values[Matrix.MSCALE_Y];
        float matrixTransX = values[Matrix.MTRANS_X];
        float matrixTransY = values[Matrix.MTRANS_Y];

        Log.d("MapView", "View: " + viewWidth + "x" + viewHeight + ", Image: " + imageWidth + "x" + imageHeight);
        Log.d("MapView", "Matrix: scale=" + matrixScaleX + ", trans=(" + matrixTransX + ", " + matrixTransY + ")");

        int pinSize = (int) (48 * getResources().getDisplayMetrics().density);
        int halfSize = pinSize / 2;

        for (PinView pinView : pinViews) {
            float[] pinPoint = new float[]{(float) pinView.x, (float) pinView.y};
            matrix.mapPoints(pinPoint);
            
            float finalX = pinPoint[0];
            float finalY = pinPoint[1];

            Log.d("MapView", "Pin " + pinView.pin.getContentDescription() + ": orig=(" + pinView.x + "," + pinView.y + ") mapped=(" + finalX + "," + finalY + ")");

            FrameLayout.LayoutParams pinParams = new FrameLayout.LayoutParams(pinSize, pinSize);
            pinParams.leftMargin = (int) (finalX - halfSize);
            pinParams.topMargin = (int) (finalY - pinSize);
            pinView.pin.setLayoutParams(pinParams);

            FrameLayout.LayoutParams labelParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            );
            labelParams.leftMargin = (int) finalX;
            labelParams.topMargin = (int) (finalY - pinSize - 25);
            pinView.label.setLayoutParams(labelParams);
            pinView.label.post(() -> pinView.label.setTranslationX(-pinView.label.getWidth() / 2f));
        }
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
            // Player id can maybe be used here.
            assert data != null;
            int pid = data.getIntExtra("player_id", -1);

            try {
                addToCampaign(pid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addToCampaign(int pid) throws JSONException {
        String url = String.format(Locale.ENGLISH, "player/%d/campaign", pid);

        JSONObject data = new JSONObject();
        data.put("campaign_code", campaignCode);
        String jsonString = data.toString();

        HttpUtils.put(url, jsonString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> 
                    HttpUtils.showToast(MapViewActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT)
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    getPlayers();
                } else {
                    runOnUiThread(() -> 
                        HttpUtils.showToast(MapViewActivity.this, "Failed to add to campaign", Toast.LENGTH_SHORT)
                    );
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.campaign_content_layout);
        ScrollView drawerWrapper = findViewById(R.id.left_drawer_wrapper);
        if (id == android.R.id.home) {
            drawer.openDrawer(drawerWrapper);
        } if (id == R.id.action_showpc) {
            Intent intent = new Intent(MapViewActivity.this, PlayerInfoActivity.class);
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

    private void getPlayers() {
        String url = String.format(Locale.ENGLISH, "campaign/%d/players", campaignId);
        HttpUtils.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Failed to fetch players: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        if (!responseObject.getBoolean("success")) {
                            // Maybe do something here
                            return;
                        }
                        JSONArray players = responseObject.getJSONArray("players");
                        // Process players...
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
