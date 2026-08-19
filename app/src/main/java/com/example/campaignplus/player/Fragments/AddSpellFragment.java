package com.example.campaignplus.player.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.SpellData;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.eventlisteners.ShortHapticFeedback;
import com.example.campaignplus.player.Adapters.SpellInstantAutoCompleteAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddSpellFragment extends Fragment {

    private static final String TAG = "PlayerAddSpellFragment";
    private ViewGroup view;
    private SpellInstantAutoCompleteAdapter arrayAdapter;

    private AutoCompleteTextView spellInput;

    public AddSpellFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_add_spell, container, false);

        Toolbar toolbar = view.findViewById(R.id.fragment_toolbar);
        toolbar.setTitle("Add Spell to " + DataCache.selectedPlayer.getName());
        registerExitFragmentButton(toolbar);

        spellInput = view.findViewById(R.id.autocomplete_spells);
        AppCompatImageButton saveButton = view.findViewById(R.id.imageButton);
        saveButton.setOnClickListener(view -> {
            try {
                playerAddSpell();
            } catch (JSONException | UnsupportedEncodingException e) {
                HttpUtils.showToast(getContext(), "Error saving:" + e.getMessage(), Toast.LENGTH_LONG);
            }
        });

        getAllSpells();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void registerExitFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.close_fragment_button);
        btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        btn.setOnClickListener(v -> {
            // Remove current fragment
            if (getActivity() != null && getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getActivity().getSupportFragmentManager().popBackStackImmediate();
            }
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    private void getAllSpells() {
        arrayAdapter = new SpellInstantAutoCompleteAdapter(view.getContext(), new ArrayList<>());
        AutoCompleteTextView autocomplete = view.findViewById(R.id.autocomplete_spells);
        autocomplete.setAdapter(arrayAdapter);
        autocomplete.setThreshold(1);

        // TODO: Add to datacache
        HttpUtils.get("user/spells", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Failed to fetch spells: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONArray spells = new JSONArray(jsonResponse);
                        arrayAdapter.clear();
                        for (int i = 0; i < spells.length(); i++) {
                            SpellData spell = new SpellData(spells.getJSONObject(i));
                            arrayAdapter.add(spell);
                        }
                        // Force the adapter to filter itself on the main thread.
                        getActivity().runOnUiThread(() -> {
                            arrayAdapter.getFilter().filter(autocomplete.getText(), null);
                        });
                    } catch (JSONException e) {
                        Log.d(TAG, "Something went wrong retrieving data from the server.");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void playerAddSpell() throws JSONException, UnsupportedEncodingException {
        String text = spellInput.getText().toString();

        // Arrayadapter is names, we need the spell id instead.
        int id = -1;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            SpellData data = arrayAdapter.getItem(i);
            assert data != null;
            if (data.name.equals(text)) {
                id = data.id;
                break;
            }
        }
        if (id == -1) {
            // Invalid ID found.
            HttpUtils.showToast(getContext(), "Cannot add, spell not found.", Toast.LENGTH_LONG);

            return;
        }

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("spell_id", id);
        String jsonString = data.toString();

        String url = String.format(Locale.ENGLISH, "player/%s/spells", DataCache.selectedPlayer.getId());
        HttpUtils.post(url, jsonString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "Failed to add spell: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    try {
                        JSONObject responseObject = new JSONObject(jsonResponse);
                        SpellData spell = new SpellData(responseObject);
                        DataCache.selectedPlayer.addSpell(spell);
                        getActivity().runOnUiThread(() -> {
                            spellInput.setText("");
                            HttpUtils.showToast(getContext(), "Added spell.", Toast.LENGTH_SHORT);
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON response", e);
                        getActivity().runOnUiThread(() -> {
                            HttpUtils.showToast(getContext(), "Error adding spell: " + e.getMessage(), Toast.LENGTH_SHORT);
                        });
                    }
                }
            }
        });
    }
}
