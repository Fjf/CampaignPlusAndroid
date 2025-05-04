package com.example.campaignplus.player.Fragments;

import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.SpellData;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus.player.Fragments.SpellInfoFragment;
import com.example.campaignplus.player.RecyclerItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CreateSpellFragment extends Fragment {
    private View view;
    public static int selectedSpellId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_spell, container, false);

        Toolbar toolbar = view.findViewById(R.id.fragment_toolbar);
        toolbar.setTitle("Create new item");


        setEventListeners();
        setSpinnerOptions();
        return view;
    }

    private void setSpinnerOptions() {
        Spinner school = view.findViewById(R.id.spell_info_school);
        String[] data = {"abjuration", "alteration", "conjuration", "divination", "enchantment",
                "illusion", "invocation", "necromancy"};
        school.setAdapter(new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, data));
    }

    private String _getText(View view) {
        return ((EditText) view).getText().toString();
    }

    private boolean _getBool(View view) {
        return ((ToggleButton) view).isChecked();
    }

    private SpellData createSpellFromFields() {
        SpellData spellData = new SpellData();
        spellData.level = Integer.parseInt(_getText(view.findViewById(R.id.spell_info_level)));
        spellData.name = _getText(view.findViewById(R.id.name));
        spellData.castingTime = _getText(view.findViewById(R.id.spell_info_casting_time));
        spellData.higherLevel = _getText(view.findViewById(R.id.spell_info_higher_level));
        spellData.duration = _getText(view.findViewById(R.id.spell_info_duration));
        spellData.components = _getText(view.findViewById(R.id.spell_info_components));
        spellData.range = _getText(view.findViewById(R.id.spell_info_range));
        spellData.concentration = _getBool(view.findViewById(R.id.concentration_button));
        spellData.ritual = _getBool(view.findViewById(R.id.ritual_button));
        spellData.school = ((Spinner) view.findViewById(R.id.spell_info_school)).getSelectedItem().toString();
        spellData.material = _getText(view.findViewById(R.id.spell_info_material));
        spellData.description = _getText(view.findViewById(R.id.spell_info_description));

        return spellData;
    }


    private void createNewSpell() throws JSONException, UnsupportedEncodingException {
        SpellData spellData;
        try {
            spellData = createSpellFromFields();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Spell level is not valid.", Toast.LENGTH_LONG).show();
            return;
        }
        HttpUtils.post("user/spells", spellData.toJSON().toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error creating spell: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate());
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error creating spell: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    private void setEventListeners() {
        Toolbar toolbar = view.findViewById(R.id.fragment_toolbar);
        ImageButton saveButton = toolbar.findViewById(R.id.save_fragment_button);
        ImageButton closeButton = toolbar.findViewById(R.id.close_fragment_button);

        saveButton.setOnClickListener(view -> {
            try {
                createNewSpell();
            } catch (JSONException | UnsupportedEncodingException e) {
                Log.d("CreateSpellFragment", "Encoding error:" + e.getMessage());
            }
        });

        closeButton.setOnClickListener(view -> {
            exitFragment();
        });
    }

    private void exitFragment() {
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();

    }

}
