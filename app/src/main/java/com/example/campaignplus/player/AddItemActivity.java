package com.example.campaignplus.player;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.example.campaignplus.player.Fragments.AddItemFragment;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AddItemActivity extends AppCompatActivity {

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
    private AppCompatEditText amount;
    private ImageButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Item");
        setSupportActionBar(toolbar);

        Spinner gearCategoryDropdown = findViewById(R.id.gear_category_dropdown);
        Spinner diceDropdown = findViewById(R.id.dice_selection);
        Spinner damageTypeDropdown = findViewById(R.id.damage_type_selection);
        button = findViewById(R.id.autocomplete_items);
        amount = findViewById(R.id.add_items_amount);
        submitButton = findViewById(R.id.add_items_submit);

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

        button.setOnClickListener(v -> {
            Fragment fragment = new AddItemFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.add_item_activity, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

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

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof AddItemFragment)
            ((AddItemFragment) fragment).setListener(new AddItemFragment.Listener() {
                @Override
                public void onDetached(AddItemFragment fragment) {
                    if (selectedItem != null)
                        button.setText(selectedItem.getName());
                }
            });
    }

    public void buttonCreateItem(View view) throws JSONException {
        long categoryId = ((Spinner) findViewById(R.id.gear_category_dropdown)).getSelectedItemId();

        ItemData itemData = null;

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
        } catch (UnsupportedEncodingException e) {
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

    public void playerAddItem(View view) throws JSONException, UnsupportedEncodingException {
        Editable amountText = amount.getText();

        // Input validation.
        if (selectedItem == null) {
            Toast.makeText(this, "No item selected.", Toast.LENGTH_SHORT).show();
            return;
        } else if (amountText == null) {
            Toast.makeText(this, "No amount defined.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("item_id", selectedItem.getId());
        data.put("amount", amountText.toString());
        StringEntity entity = new StringEntity(data.toString(), Charset.defaultCharset());

        submitButton.setEnabled(false);

        String url = String.format(Locale.ENGLISH, "player/%s/item", selectedPlayer.getId());
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                /*
                 * Close the window
                 */
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                submitButton.setEnabled(true);
                Toast.makeText(AddItemActivity.this, "Successfully added item.", Toast.LENGTH_SHORT).show();

                Intent data = new Intent();
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                submitButton.setEnabled(true);
                Toast.makeText(AddItemActivity.this, "Error adding item: Status code.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String response = errorResponse == null ? null : errorResponse.toString();
                onFailure(statusCode, headers, response, throwable);
            }

        });
    }

    private void createItem(JSONObject data) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(data.toString(), Charset.defaultCharset());
        HttpUtils.post("user/items", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    AvailableItems.updateItem(response);
                } catch (JSONException e) {
                    Toast.makeText(AddItemActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d(TAG, "Error: " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String response = errorResponse == null ? null : errorResponse.toString();
                onFailure(statusCode, headers, response, throwable);
            }

        });
    }
}
