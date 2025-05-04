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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.entity.StringEntity;

public class AddSpellFragment extends Fragment {

    private static final String TAG = "PlayerAddSpellFragment";
    private ViewGroup view;
    private SpellInstantAutoCompleteAdapter arrayAdapter;

    private AutoCompleteTextView spellInput;

    public AddSpellFragment() {
    }

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
                Toast.makeText(getContext(), "Error saving:" + e.getMessage(), Toast.LENGTH_LONG).show();
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
        btn.setOnClickListener(view -> {
            // Remove current fragment
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    private void getAllSpells() {
        arrayAdapter = new SpellInstantAutoCompleteAdapter(view.getContext(), new ArrayList<>());
        AutoCompleteTextView autocomplete = view.findViewById(R.id.autocomplete_spells);
        autocomplete.setAdapter(arrayAdapter);
        autocomplete.setThreshold(1);

        HttpUtils.get("user/spells", new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "Invalid response: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray spells = new JSONArray(responseBody);
                        arrayAdapter.clear();
                        for (int i = 0; i < spells.length(); i++) {
                            SpellData spell = new SpellData(spells.getJSONObject(i));
                            arrayAdapter.add(spell);
                        }
                        //Force the adapter to filter itself, necessary to show new data.
                        //Filter based on the current text because api call is asynchronous.
                        getActivity().runOnUiThread(() -> {
                            arrayAdapter.getFilter().filter(autocomplete.getText(), null);
                        });
                    } catch (JSONException e) {
                        Log.d(TAG, "Something went wrong retrieving data from the server.");
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "Invalid response: " + response.message());
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
            Toast.makeText(getContext(), "Cannot add, spell not found.", Toast.LENGTH_LONG).show();

            return;
        }

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("spell_id", id);

        String url = String.format(Locale.ENGLISH, "player/%s/spells", DataCache.selectedPlayer.getId());
        HttpUtils.post(url, data.toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d(TAG, "Invalid response: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);
                        SpellData spell = new SpellData(jsonObject);
                        DataCache.selectedPlayer.addSpell(spell);
                        getActivity().runOnUiThread(() -> {
                            spellInput.setText("");
                            Toast.makeText(getContext(), "Added spell.", Toast.LENGTH_SHORT).show();
                        });
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(TAG, "Invalid response: " + response.message());
                }
            }
        });

    }
}
