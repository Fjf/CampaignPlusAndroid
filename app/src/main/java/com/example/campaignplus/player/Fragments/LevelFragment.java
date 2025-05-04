package com.example.campaignplus.player.Fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.campaignplus.R;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.eventlisteners.ShortHapticFeedback;

import org.json.JSONException;

import java.util.Objects;

public class LevelFragment extends Fragment {
    private String[] arr;

    private final int[] radioButtons = new int[]{
            R.id.exhaustion_0,
            R.id.exhaustion_1,
            R.id.exhaustion_2,
            R.id.exhaustion_3,
            R.id.exhaustion_4,
            R.id.exhaustion_5
    };

    private SharedPreferences.Editor editor;
    private TextView tv;
    private ViewGroup view;

    private int indexOf(int[] array, int value) {
        int id;
        for (int i = 0; i < array.length; i++) {
            id = array[i];
            if (id == value)
                return i;
        }
        return -1;
    }

    public static class OnCompleteCallback {
        public void success() {

        }

        public void cancel() {

        }
    }

    OnCompleteCallback callback;
    public LevelFragment(OnCompleteCallback callback) {
        this.callback = callback;
    }

    public LevelFragment() {
        this(new OnCompleteCallback());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_player_level, container, false);

        RadioGroup rg = view.findViewById(R.id.exhaustion_buttons);
        tv = view.findViewById(R.id.exhaustion_info);

        // Get the strings from the string.xml to show information to player.
        Resources res = getResources();
        arr = res.getStringArray(R.array.exhaustion_info_text);

        // Load the preferences storage to save player's current exhaustion.
        SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("PlayerData_" + selectedPlayer.getId(), MODE_PRIVATE);
        editor = preferences.edit();

        // Load the currently selected exhaustion button and the corresponding informational text.
        int id = preferences.getInt("current_exhaustion", radioButtons[0]);

        // See if the selected radiobutton still exists.
        if (indexOf(radioButtons, id) == -1)
            id = radioButtons[0];

        RadioButton rb = view.findViewById(id);
        rb.setChecked(true);
        setExhaustionInfoText(id);

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            setExhaustionInfoText(checkedId);

            editor.putInt("current_exhaustion", checkedId);
            editor.apply();
        });

        // Load toolbar eventlisteners.
        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        tb.setTitle("Player Stats Overview");
        registerExitFragmentButton(tb);
        registerSaveFragmentButton(tb);

        setDefaultValues();
        registerTextChangeListeners();

        return view;
    }

    private void registerExitFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.close_fragment_button);
        btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        btn.setOnClickListener(view -> {
            // Remove current fragment
            callback.cancel();
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    private void registerSaveFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.save_fragment_button);
        btn.setOnClickListener(view -> {
            try {
                selectedPlayer.upload(new CallBack() {
                    @Override
                    public void success() {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(view.getContext(), "Successfully uploaded player data.", Toast.LENGTH_SHORT).show();
                            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                        });

                        // Remove current fragment on successful upload.
                        callback.success();
                    }

                    @Override
                    public void error(String errorMessage) {
                        getActivity().runOnUiThread(() -> {

                            Toast.makeText(view.getContext(), "Something went wrong uploading player data: " + errorMessage, Toast.LENGTH_LONG).show();
                        });

                    }
                });
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setDefaultValues() {
        ((EditText) view.findViewById(R.id.level_input)).setText(selectedPlayer.statsData.getLevel());
        ((EditText) view.findViewById(R.id.hp_input)).setText(selectedPlayer.statsData.getMaxHP());
        ((EditText) view.findViewById(R.id.armor_input)).setText(selectedPlayer.statsData.getArmorClass());
        ((EditText) view.findViewById(R.id.speed_input)).setText(selectedPlayer.statsData.getSpeed());
    }


    private void registerTextChangeListeners() {
        view.findViewById(R.id.level_input).setOnKeyListener((v, keyCode, event) -> {
            String text = ((EditText) v).getText().toString();
            if (text.length() > 0) {
                selectedPlayer.statsData.setLevel(Integer.parseInt(text));
            }
            return false;
        });

        view.findViewById(R.id.hp_input).setOnKeyListener((v, keyCode, event) -> {
            String text = ((EditText) v).getText().toString();
            if (text.length() > 0)
                selectedPlayer.statsData.setMaxHP(Integer.parseInt(text));
            return false;
        });

        view.findViewById(R.id.armor_input).setOnKeyListener((v, keyCode, event) -> {
            String text = ((EditText) v).getText().toString();
            if (text.length() > 0)
                selectedPlayer.statsData.setArmorClass(Integer.parseInt(text));
            return false;
        });

        view.findViewById(R.id.speed_input).setOnKeyListener((v, keyCode, event) -> {
            String text = ((EditText) v).getText().toString();
            if (text.length() > 0)
                selectedPlayer.statsData.setSpeed(Integer.parseInt(text));
            return false;
        });
    }


    private void setExhaustionInfoText(int checkedId) {
        int id = indexOf(radioButtons, checkedId);
        if (id > -1 && id < arr.length) {
            if (id == 0) {
                tv.setText(arr[id]);
            } else {
                // After exhaustion level 0, they will add up together
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= id; i++) {
                    sb.append(arr[i]);
                    sb.append("\n");
                }
                tv.setText(sb.toString());
            }
        }

        // Scroll to bottom after updating text
        ScrollView scroll = view.findViewById(R.id.stats_overview);
        scroll.fullScroll(View.FOCUS_DOWN);
    }
}
