package com.example.dndapp.player;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp._data.items.AvailableItems;
import com.example.dndapp._data.items.ItemData;
import com.example.dndapp.player.Adapters.ItemSpinnerArrayAdapter;
import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp._utils.PlayerItemData;
import com.example.dndapp.player.Fragments.SelectItemFragment;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static com.example.dndapp.player.PlayerInfoActivity.selectedPlayer;

public class AddItemActivity extends AppCompatActivity {
    private static final String TAG = "AddItemActivity";
    public static ItemData selectedItem;

    private String[] items = new String[0];
    private int[] ids = new int[0];
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

        Spinner spinner = findViewById(R.id.dice_selection);
        button = findViewById(R.id.autocomplete_items);
        amount = findViewById(R.id.add_items_amount);
        submitButton = findViewById(R.id.add_items_submit);

        final String[] dice = new String[]{"Select dice type...", "4", "6", "8", "10", "12", "20", "100"};
        ItemSpinnerArrayAdapter spinnerArrayAdapter = new ItemSpinnerArrayAdapter(this, R.layout.spinner_row, dice);

        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_row);
        spinner.setAdapter(spinnerArrayAdapter);

        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SelectItemFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.add_item_activity, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof SelectItemFragment)
            ((SelectItemFragment) fragment).setListener(new SelectItemFragment.Listener() {
                @Override
                public void onDetached(SelectItemFragment fragment) {
                    if (selectedItem != null)
                        button.setText(selectedItem.getName());
                }
            });
    }

    public void toggleWeaponInfo(android.view.View view) {

        if (((Switch) view).isChecked()) {
            findViewById(R.id.add_weapon_wrapper).setVisibility(View.VISIBLE);
        } else {
            // The original view.
            findViewById(R.id.add_weapon_wrapper).setVisibility(View.GONE);
        }
    }

    public void buttonAddItem(View view) {
        boolean isWeapon = ((Switch) findViewById(R.id.item_weapon_toggle)).isChecked();
        PlayerItemData pid = null;

        String name = ((EditText) findViewById(R.id.item_name)).getText().toString();
        String info = ((EditText) findViewById(R.id.item_information)).getText().toString();
        String amount = ((EditText) findViewById(R.id.item_amount)).getText().toString();

        // Dont allow empty strings.
        if (name.length() == 0 || info.length() == 0 || amount.length() == 0) return;

        if (!isWeapon) {
            pid = new PlayerItemData(name, info, Integer.parseInt(amount));
        } else {
            Spinner spinner = findViewById(R.id.dice_selection);
            if (spinner.getSelectedItemPosition() == 0) return; // Not a selection.
            // TODO: Highlight in red all unfilled input fields.

            String damageType = "slashing"; // TODO: Add field for damage type, probably dropdown.
            String diceType = spinner.getSelectedItem().toString();
            String diceAmount = ((EditText) findViewById(R.id.dice_amount)).getText().toString();
            String flatDamage = ((EditText) findViewById(R.id.flat_damage)).getText().toString();

            pid = new PlayerItemData(name, info, Integer.parseInt(amount), damageType,
                    Integer.parseInt(diceAmount), Integer.parseInt(diceType), Integer.parseInt(flatDamage));
        }

        try {
            addItem(pid);
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
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
        StringEntity entity = new StringEntity(data.toString());

        submitButton.setEnabled(false);
        
        String url = String.format(Locale.ENGLISH, "player/%s/item", selectedPlayer.getId());
        HttpUtils.post(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                submitButton.setEnabled(true);
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(AddItemActivity.this, "Successfully added item.", Toast.LENGTH_SHORT).show();

                        Intent data = new Intent();
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        Toast.makeText(AddItemActivity.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(AddItemActivity.this, "Invalidly formatted JSON received from server.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                submitButton.setEnabled(true);
                Toast.makeText(AddItemActivity.this, "Invalidly formatted JSON received from server.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String response = errorResponse == null ? null : errorResponse.toString();
                onFailure(statusCode, headers, response, throwable);
            }

        });
    }

    private void addItem(PlayerItemData pid) throws UnsupportedEncodingException, JSONException {
        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
        int playerId = preferences.getInt("player_id", -1);

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("player_id", playerId);
        data.put("name", pid.getName());
        data.put("extra_info", pid.getExtraInfo());
        data.put("amount", pid.getAmount());
        data.put("damage_type", pid.getDamageType());
        data.put("dice_type", pid.getDiceType());
        data.put("dice_amount", pid.getDiceAmount());
        data.put("flat_damage", pid.getFlatDamage());
        StringEntity entity = new StringEntity(data.toString());

//        HttpUtils.post("addplayeritem", entity, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                try {
//                    JSONObject serverResp = new JSONObject(response.toString());
//                    if (serverResp.getBoolean("success")) {
//                        finish();
//                    } else {
//                        Log.d(TAG, "An error occured: " + serverResp.getString("error"));
//                    }
//
//                } catch (JSONException e) {
//                    Log.d(TAG, "Invalid response: " + response.toString());
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
//                Log.d(TAG, "Invalid response: " + response);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                String response = errorResponse == null ? null : errorResponse.toString();
//                onFailure(statusCode, headers, response, throwable);
//            }
//
//        });
    }
}
