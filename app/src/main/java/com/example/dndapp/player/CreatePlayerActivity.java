package com.example.dndapp.player;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.R;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.IgnoreFunctionCall;
import com.example.dndapp.player.Adapters.ClassAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.dndapp.player.PlayerInfoActivity.selectedPlayer;

public class CreatePlayerActivity extends AppCompatActivity {
    private TextView playerName;
    private TextView playerRace;
    private TextInputEditText playerBackstory;
    private Spinner playerClassSpinner;
    private ClassAdapter classAdapter;
    private Button addPlayerButton;

    private boolean isCreation;

    private ArrayList<MainClassInfo> selectedClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_player);

        // Load toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        selectedClasses = new ArrayList<>();

        // Find views
        playerName = findViewById(R.id.player_name_field);
        playerRace = findViewById(R.id.player_race_field);
        playerBackstory = findViewById(R.id.player_backstory_field);
        playerClassSpinner = findViewById(R.id.player_class_field);

        Intent intent = getIntent();
        isCreation = intent.getBooleanExtra("create", true);

        if (isCreation) {
            toolbar.setTitle("Create Player");
        } else {
            toolbar.setTitle("Edit " + selectedPlayer.getName());
            playerName.setText(selectedPlayer.getName());
            playerRace.setText(selectedPlayer.getRace());
            playerBackstory.setText(selectedPlayer.getBackstory());

            selectedClasses.addAll(selectedPlayer.getMainClassInfos());
        }

        setSupportActionBar(toolbar);



        ListView lv = findViewById(R.id.player_class_list);

        classAdapter = new ClassAdapter(this, selectedClasses);
        lv.setAdapter(classAdapter);

        // Onclick, remove the clicked entry from the list, and update the ListView.
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                selectedClasses.remove(i);
                classAdapter.notifyDataSetChanged();
            }
        });

        ImageButton bt = findViewById(R.id.player_class_add);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSelectedClass(playerClassSpinner.getSelectedItemPosition());
            }
        });

        addPlayerButton = findViewById(R.id.player_create_new_button);
        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (isCreation)
                        createNewCharacter();
                    else
                        updateExistingCharacter();
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        getClassData();
    }

    /**
     * This function will add an object to the selected list which is also shown in the list.
     * @param selectedItemId The id from classes list will be added to the selected list.
     */
    private void addSelectedClass(int selectedItemId) {
        if (selectedItemId > MyPlayerCharacterList.availableClasses.size())
            return;

        MainClassInfo mci = MyPlayerCharacterList.availableClasses.get(selectedItemId);

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
        MyPlayerCharacterList.updateClassData(new FunctionCall() {
            @Override
            public void success() {
                updateClassSpinner();
            }

            @Override
            public void error(String errorMessage) {
                Toast.makeText(CreatePlayerActivity.this, "Something went wrong retrieving classes from server: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClassSpinner() {
        ArrayList<String> names = new ArrayList<>();
        for (MainClassInfo mci : MyPlayerCharacterList.availableClasses) {
            names.add(mci.getName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerClassSpinner.setAdapter(arrayAdapter);
    }

    public void createNewCharacter() throws UnsupportedEncodingException, JSONException {
        String name = playerName.getText().toString();
        String race = playerRace.getText().toString();

        PlayerData pd = new PlayerData(-1, name, race);
        pd.addMainClasses(selectedClasses);

        StringEntity entity = new StringEntity(pd.toJSON().toString());

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

    private void updateExistingCharacter() throws JSONException, UnsupportedEncodingException {
        String name = playerName.getText().toString();
        String race = playerRace.getText().toString();

        PlayerData pd = new PlayerData(selectedPlayer.getId(), name, race);
        pd.setMainClasses(selectedClasses);

        StringEntity entity = new StringEntity(pd.toJSON().toString());

        findViewById(R.id.player_create_new_button).setEnabled(false);

        String url = String.format(Locale.ENGLISH, "player/%d/data", selectedPlayer.getId());

        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getBoolean("success")) {
                        // After creating new character, this overlay may close.
                        Intent data = new Intent();
                        data.putExtra("player_id", response.getInt("player_id"));
                        setResult(RESULT_OK, data);

                        Toast.makeText(CreatePlayerActivity.this, "Updated player successfully.", Toast.LENGTH_SHORT).show();

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
