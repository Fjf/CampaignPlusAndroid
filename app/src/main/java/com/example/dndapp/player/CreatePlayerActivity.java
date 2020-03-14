package com.example.dndapp.player;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.R;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.IgnoreFunctionCall;
import com.example.dndapp.player.Adapters.ClassAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CreatePlayerActivity extends AppCompatActivity {
    private TextView playerName;
    private TextView playerRace;
    private Spinner playerClassSpinner;
    private ClassAdapter classAdapter;

    private ArrayList<MainClassInfo> allClasses;
    private ArrayList<MainClassInfo> selectedClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_player);

        // Load toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Create Player");
        setSupportActionBar(toolbar);

        // Find views
        playerName = findViewById(R.id.player_name_field);
        playerRace = findViewById(R.id.player_race_field);
        playerClassSpinner = findViewById(R.id.player_class_field);

        selectedClasses = new ArrayList<>();

        ListView lv = findViewById(R.id.player_class_list);

        classAdapter = new ClassAdapter(this, selectedClasses);
        lv.setAdapter(classAdapter);

        ImageButton bt = findViewById(R.id.player_class_add);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSelectedClass(playerClassSpinner.getSelectedItemPosition());
            }
        });

        getClassData();
    }

    /**
     * This function will add an object to the selected list which is also shown in the list.
     * @param selectedItemId The id from classes list will be added to the selected list.
     */
    private void addSelectedClass(int selectedItemId) {
        if (selectedItemId > allClasses.size())
            return;

        MainClassInfo mci = allClasses.get(selectedItemId);

        // Don't add already added classes to the list.
        for (MainClassInfo s : selectedClasses) {
            if (s.getId() == mci.getId())
                return;
        }

        selectedClasses.add(mci);
        classAdapter.notifyDataSetChanged();
    }


    /**
     * Retrieves the class data from the server.
     * On success; passes the data to 'updateClassSpinner'.
     * On error; notifies user
     */
    private void getClassData() {
        HttpUtils.get("user/classes", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        updateClassSpinner(response.getJSONArray("classes"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                // TODO: Maybe do something here
                Toast.makeText(CreatePlayerActivity.this, "Something went wrong retrieving classes from server.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClassSpinner(JSONArray classes) throws JSONException {
        allClasses = new ArrayList<>();
        for (int i = 0; i < classes.length(); i++) {
            allClasses.add(new MainClassInfo(classes.getJSONObject(i)));
        }

        ArrayList<String> names = new ArrayList<>();
        for (MainClassInfo mci : allClasses) {
            names.add(mci.getName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerClassSpinner.setAdapter(arrayAdapter);
    }

    public void createNewCharacter(View view) throws UnsupportedEncodingException, JSONException {
        String name = playerName.getText().toString();
        String race = playerRace.getText().toString();

        PlayerData pd = new PlayerData(-1, name, race);
        pd.addMainClasses(selectedClasses);

        StringEntity entity = new StringEntity(pd.toJSON().toString());

        System.out.println(pd.toJSON().toString());

        findViewById(R.id.player_create_new_button).setEnabled(false);

        HttpUtils.post("user/player", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(CreatePlayerActivity.this, "New player created successfully.", Toast.LENGTH_SHORT).show();

                        // After creating new character, this overlay may close.
                        Intent data = new Intent();
                        data.putExtra("player_id", response.getInt("player_id"));
                        setResult(RESULT_OK, data);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                findViewById(R.id.player_create_new_button).setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                findViewById(R.id.player_create_new_button).setEnabled(true);
            }
        });
    }
}
