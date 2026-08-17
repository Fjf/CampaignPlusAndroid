package com.example.campaignplus.player;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.campaignplus._data.items.AvailableItems;
import com.example.campaignplus._data.items.ItemData;
import com.example.campaignplus.player.Adapters.ItemSpinnerArrayAdapter;
import com.example.campaignplus.R;
import com.example.campaignplus._utils.HttpUtils;
import com.google.android.material.button.MaterialButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class CreateItemActivity extends AppCompatActivity {

    final String[] gearCategories = new String[]{
            "Select gear category...", "Armor", "Tools", "Adventuring Gear",
            "Mounts and Vehicles", "Weapon"
    };

    final String[] dice = new String[]{"No dice", "4", "6", "8", "10", "12", "20", "100"};

    final String[] damageTypes = new String[]{
            "acid", "bludgeoning", "cold", "fire", "force", "lightning", "necrotic", "piercing",
            "poison", "psychic", "radiant", "slashing", "thunder"
    };

    private static final String TAG = "AddItemActivity";
    public static ItemData selectedItem;

    private MaterialButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Create Item");
        setSupportActionBar(toolbar);

        Spinner gearCategoryDropdown = findViewById(R.id.gear_category_dropdown);
        Spinner diceDropdown = findViewById(R.id.dice_selection);
        Spinner damageTypeDropdown = findViewById(R.id.damage_type_selection);
        button = findViewById(R.id.autocomplete_items);

        Button createItemButton = findViewById(R.id.create_item_button);

        /*
         * Gear categories
         */
        ItemSpinnerArrayAdapter gearCategoryArrayAdapter = new ItemSpinnerArrayAdapter(this, R.layout.spinner_row, gearCategories);
        gearCategoryArrayAdapter.setDropDownViewResource(R.layout.spinner_row);
        gearCategoryDropdown.setAdapter(gearCategoryArrayAdapter);

        /*
         * Set dice selection dropdown menu
         */
        ItemSpinnerArrayAdapter spinnerArrayAdapter = new ItemSpinnerArrayAdapter(this, R.layout.spinner_row, dice);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_row);
        diceDropdown.setAdapter(spinnerArrayAdapter);

        /*
         * Set damage type dropdown menu
         */
        ItemSpinnerArrayAdapter damageArrayAdapter = new ItemSpinnerArrayAdapter(this, R.layout.spinner_row, damageTypes);
        damageArrayAdapter.setDropDownViewResource(R.layout.spinner_row);
        damageTypeDropdown.setAdapter(damageArrayAdapter);

        gearCategoryDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                findViewById(R.id.create_armor_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_tool_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_adventuring_gear_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_mount_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_weapon_wrapper).setVisibility(View.GONE);

                switch (gearCategories[i]) {
                    case "Armor":
                        findViewById(R.id.create_armor_wrapper).setVisibility(View.VISIBLE);
                        break;
                    case "Tools":
                        findViewById(R.id.create_tool_wrapper).setVisibility(View.VISIBLE);
                        break;
                    case "Adventuring Gear":
                        findViewById(R.id.create_adventuring_gear_wrapper).setVisibility(View.VISIBLE);
                        break;
                    case "Mounts and Vehicles":
                        findViewById(R.id.create_mount_wrapper).setVisibility(View.VISIBLE);
                        break;
                    case "Weapon":
                        findViewById(R.id.create_weapon_wrapper).setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                findViewById(R.id.create_armor_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_tool_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_adventuring_gear_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_mount_wrapper).setVisibility(View.GONE);
                findViewById(R.id.create_weapon_wrapper).setVisibility(View.GONE);
            }
        });

    }

    public void buttonCreateItem(View view) throws JSONException {
        long categoryId = ((Spinner) findViewById(R.id.gear_category_dropdown)).getSelectedItemId();

        String name = ((EditText) findViewById(R.id.item_name)).getText().toString();
        String description = ((EditText) findViewById(R.id.item_information)).getText().toString();

        // Dont allow empty strings.
        if (name.length() == 0 || description.length() == 0) return;
        if (categoryId == 0) return;

        String category = gearCategories[(int) categoryId];

        JSONObject itemJsonObject = new JSONObject();
        JSONObject itemInfoObject = new JSONObject();

        // Put standard data
        itemJsonObject.put("name", name);
        itemJsonObject.put("description", description);
        itemJsonObject.put("category", category);
        itemJsonObject.put("weight", 0);  // TODO: Create field for weight
        itemJsonObject.put("cost", 0);  // TODO: Create field for weight

        /*
         * Put extra data specifically for Weapon-types
         */
        if (Objects.equals(category, "Weapon")) {
            String diceType = (String) ((Spinner) findViewById(R.id.dice_selection)).getSelectedItem();
            String damageType = (String) ((Spinner) findViewById(R.id.damage_type_selection)).getSelectedItem();

            String diceAmount = ((EditText) findViewById(R.id.dice_amount)).getText().toString();
            String flatDamage = ((EditText) findViewById(R.id.flat_damage)).getText().toString();

            itemInfoObject.put("damage_type", damageType);
            itemInfoObject.put("damage_dice", diceType + "d" + diceAmount);
            itemInfoObject.put("bonus_damage", flatDamage);
            itemInfoObject.put("bonus_damage_type", damageType);
        }

        // Add the info object as additional field to the full item
        itemJsonObject.put("item_info", itemInfoObject);

        try {
            createItem(itemJsonObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int strIndexOf(String[] items, String text) {
        String item;
        for (int i = 0; i < items.length; i++) {
            item = items[i];
            if (item.equals(text)) {
                return i;
            }

        }
        return -1;
    }

    private void createItem(JSONObject data) {
        String jsonString = data.toString();
        HttpUtils.post("user/items", jsonString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        AvailableItems.updateItem(responseObject);
                    } catch (JSONException e) {
                        HttpUtils.showToast(CreateItemActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                    }
                    finish();
                }
            }
        });
    }
}
