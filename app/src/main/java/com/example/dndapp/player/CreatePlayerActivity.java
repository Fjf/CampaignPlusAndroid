package com.example.dndapp.player;

import android.content.Intent;

import com.example.dndapp._data.DataCache;
import com.example.dndapp._data.PlayerData;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.R;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.classinfo.MainClassInfo;
import com.example.dndapp._utils.CallBack;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.player.Adapters.ClassAdapter;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.dndapp._data.DataCache.availableClasses;
import static com.example.dndapp._data.DataCache.playerData;
import static com.example.dndapp._data.DataCache.selectedPlayer;

public class CreatePlayerActivity extends AppCompatActivity {
    private TextView playerName;
    private TextView playerRace;
    private TextInputEditText playerBackstory;
    private Spinner availableClassesSpinner;
    private ClassAdapter classAdapter;
    private Button addPlayerButton;
    private ArrayList<String> names;
    private boolean isCreation;

    private ArrayList<Integer> selectedClassIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_player);

        // Load toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);

        selectedClassIds = new ArrayList<>();

        // Find views
        playerName = findViewById(R.id.player_name_field);
        playerRace = findViewById(R.id.player_race_field);
        playerBackstory = findViewById(R.id.player_backstory_field);
        availableClassesSpinner = findViewById(R.id.player_class_field);

        Intent intent = getIntent();
        isCreation = intent.getBooleanExtra("create", true);

        if (isCreation) {
            toolbar.setTitle("Create Player");
        } else {
            toolbar.setTitle("Edit " + selectedPlayer.getName());
            playerName.setText(selectedPlayer.getName());
            playerRace.setText(selectedPlayer.getRace());
            playerBackstory.setText(selectedPlayer.getBackstory());
            selectedClassIds.addAll(selectedPlayer.getMainClassIds());
        }

        setSupportActionBar(toolbar);

        ListView lv = findViewById(R.id.player_class_list);

        classAdapter = new ClassAdapter(this, selectedClassIds);
        lv.setAdapter(classAdapter);

        // Onclick, remove the clicked entry from the list, and update the ListView.
        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedClassIds.remove(i);
            classAdapter.notifyDataSetChanged();
        });

        ImageButton bt = findViewById(R.id.player_class_add);
        bt.setOnClickListener(view -> addSelectedClass(availableClassesSpinner.getSelectedItemId()));

        addPlayerButton = findViewById(R.id.player_create_new_button);
        addPlayerButton.setOnClickListener(view -> {
            try {
                if (isCreation)
                    createNewCharacter();
                else
                    updateExistingCharacter();
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        });

        getClassData();
    }

    /**
     * This function will add an object to the selected list which is also shown in the list.
     *
     * @param selectedItemId The id from classes list will be added to the selected list.
     */
    private void addSelectedClass(long selectedItemId) {
        if (selectedItemId > availableClasses.size())
            return;

        // We can assume the ordering of the class hashmap hasn't changed between creation and now.
        MainClassInfo selected = null;
        Iterator<MainClassInfo> iterator = availableClasses.values().iterator();
        while (iterator.hasNext()) {
            selected = iterator.next();
            if (Objects.equals(selected.getName(), names.get((int) selectedItemId))) {
                break;
            }
        }


        assert selected != null;

        // Don't add already added classes to the list.
        for (Integer s : selectedClassIds) {
            if (s == selected.getId())
                return;
        }

        selectedClassIds.add(selected.getId());
        classAdapter.notifyDataSetChanged();
    }


    /**
     * Retrieves the class data from the server.
     * On success; passes the data to 'updateClassSpinner'.
     * On error; notifies user
     */
    private void getClassData() {
        MyPlayerCharacterList.updateClassData(new CallBack() {
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
        names = new ArrayList<>();
        for (MainClassInfo mci : DataCache.availableClasses.values()) {
            names.add(mci.getName());
        }
        Collections.sort(names);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availableClassesSpinner.setAdapter(arrayAdapter);
    }

    public void createNewCharacter() throws UnsupportedEncodingException, JSONException {
        String name = playerName.getText().toString();
        String race = playerRace.getText().toString();

        selectedPlayer.setName(name);
        selectedPlayer.setRace(race);
        selectedPlayer.setBackstory(Objects.requireNonNull(playerBackstory.getText()).toString());
        selectedPlayer.setMainClassIds(selectedClassIds);

        StringEntity entity = new StringEntity(selectedPlayer.toJSON().toString());

        findViewById(R.id.player_create_new_button).setEnabled(false);

        HttpUtils.post("user/player", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Toast.makeText(CreatePlayerActivity.this, "New player created successfully.", Toast.LENGTH_SHORT).show();

                    // After creating new character, this overlay may close.

                    PlayerData player = new PlayerData(response);
                    playerData.add(player);

                    Intent data = new Intent();
                    data.putExtra("player_id", player.getId());
                    setResult(RESULT_OK, data);
                    finish();
                } catch (JSONException e) {
                    Toast.makeText(CreatePlayerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                findViewById(R.id.player_create_new_button).setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(CreatePlayerActivity.this, response, Toast.LENGTH_SHORT).show();
                findViewById(R.id.player_create_new_button).setEnabled(true);
            }
        });
    }

    private void updateExistingCharacter() throws JSONException, UnsupportedEncodingException {
        String name = playerName.getText().toString();
        String race = playerRace.getText().toString();

        selectedPlayer.setName(name);
        selectedPlayer.setRace(race);
        selectedPlayer.setBackstory(Objects.requireNonNull(playerBackstory.getText()).toString());
        selectedPlayer.setMainClassIds(selectedClassIds);

        StringEntity entity = new StringEntity(selectedPlayer.toJSON().toString());

        findViewById(R.id.player_create_new_button).setEnabled(false);

        String url = String.format(Locale.ENGLISH, "player/%d", selectedPlayer.getId());
        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    selectedPlayer.setData(response);

                    // After creating new character, this overlay may close.
                    Intent data = new Intent();
                    data.putExtra("player_id", selectedPlayer.getId());
                    setResult(RESULT_OK, data);

                    Toast.makeText(CreatePlayerActivity.this, "Updated player successfully.", Toast.LENGTH_SHORT).show();

                    finish();
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
