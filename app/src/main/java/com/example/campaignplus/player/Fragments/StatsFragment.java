package com.example.campaignplus.player.Fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import org.json.JSONObject;

import java.util.Objects;

public class StatsFragment extends Fragment {
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
    private Handler mHandler = new Handler(Looper.getMainLooper());

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

    StatsFragment.OnCompleteCallback callback;

    public StatsFragment(StatsFragment.OnCompleteCallback callback) {
        this.callback = callback;
    }

    public StatsFragment() {
        this(new StatsFragment.OnCompleteCallback());
    }

    private void savePlayerData() throws JSONException {
        String st = ((TextView) view.findViewById(R.id.totStr)).getText().toString();
        String de = ((TextView) view.findViewById(R.id.totDex)).getText().toString();
        String co = ((TextView) view.findViewById(R.id.totCon)).getText().toString();
        String wi = ((TextView) view.findViewById(R.id.totWis)).getText().toString();
        String in = ((TextView) view.findViewById(R.id.totInt)).getText().toString();
        String ch = ((TextView) view.findViewById(R.id.totCha)).getText().toString();

        selectedPlayer.statsData.setStrength(Integer.parseInt(st));
        selectedPlayer.statsData.setDexterity(Integer.parseInt(de));
        selectedPlayer.statsData.setConstitution(Integer.parseInt(co));
        selectedPlayer.statsData.setWisdom(Integer.parseInt(wi));
        selectedPlayer.statsData.setIntelligence(Integer.parseInt(in));
        selectedPlayer.statsData.setCharisma(Integer.parseInt(ch));

        selectedPlayer.upload(new CallBack() {
            @Override
            public void success() {
                mHandler.post(() -> {
                    Toast.makeText(view.getContext(), "Successfully uploaded player data.", Toast.LENGTH_SHORT).show();

                });
            }

            @Override
            public void error(String errorMessage) {
                mHandler.post(() -> {

                    Toast.makeText(view.getContext(), "Something went wrong uploading player data: " + errorMessage, Toast.LENGTH_LONG).show();
                });

            }
        });
    }

    private void setOnChangeRefreshListeners() {
        int[] checkboxIds = new int[]{
                R.id.saving_throws_cha,
                R.id.saving_throws_dex,
                R.id.saving_throws_str,
                R.id.saving_throws_con,
                R.id.saving_throws_int,
                R.id.saving_throws_wis
        };
        for (int id : checkboxIds) {
            CheckBox vw = view.findViewById(id);

            vw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                try {
                    getPlayerProficiencies();
                    getPlayerSavingThrows();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPlayerProficiencies();
                setPlayerProficiencyBonus();
            });
        }

        int[] buttonIds = new int[]{
                R.id.proficiency_acr,
                R.id.proficiency_anh,
                R.id.proficiency_arc,
                R.id.proficiency_ath,
                R.id.proficiency_dec,
                R.id.proficiency_his,
                R.id.proficiency_ins,
                R.id.proficiency_intimidation,
                R.id.proficiency_inv,
                R.id.proficiency_med,
                R.id.proficiency_nat,
                R.id.proficiency_perception,
                R.id.proficiency_per,
                R.id.proficiency_persuasion,
                R.id.proficiency_rel,
                R.id.proficiency_soh,
                R.id.proficiency_ste,
                R.id.proficiency_sur
        };

        for (int id : buttonIds) {
            Button vw = view.findViewById(id);

            vw.setOnClickListener(v -> {
                int value = Integer.parseInt((String) vw.getText());
                vw.setText(String.valueOf((value + 1) % 3));

                try {
                    getPlayerProficiencies();
                    getPlayerSavingThrows();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPlayerProficiencies();
                setPlayerProficiencyBonus();

            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_player_stats, container, false);

        RadioGroup rg = view.findViewById(R.id.exhaustion_buttons);
        tv = view.findViewById(R.id.exhaustion_info);

        // Get the strings from the string.xml to show information to player.
        Resources res = getResources();
        arr = res.getStringArray(R.array.exhaustion_info_text);

        // Load the preferences storage to save player's current exhaustion.
        SharedPreferences preferences = Objects.requireNonNull(getActivity()).getSharedPreferences("PlayerData_" + selectedPlayer.getId(), MODE_PRIVATE);
        editor = preferences.edit();

        // Load toolbar eventlisteners.
        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        tb.setTitle("Player Stats Overview");
        registerExitFragmentButton(tb);
        registerSaveFragmentButton(tb);

        setStats();
        setPlayerProficiencies();
        setPlayerProficiencyBonus();

        registerTextChangeListeners();
        setOnChangeRefreshListeners();

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
            // Remove current fragment
            try {
                savePlayerData();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            callback.success();
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void registerTextChangeListeners() {
        // Add eventlisteners for every stat input field.
        int[] statsIds = new int[]{R.id.totCha, R.id.totStr, R.id.totDex, R.id.totInt, R.id.totWis};
        for (int id : statsIds) {
            view.findViewById(id).setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    getPlayerStats();
                    setPlayerProficiencyBonus();
                }
            });
        }
    }

    public void getPlayerStats() {
        selectedPlayer.statsData.setDexterity(Integer.parseInt(((EditText) view.findViewById(R.id.totDex)).getText().toString()));
        selectedPlayer.statsData.setStrength(Integer.parseInt(((EditText) view.findViewById(R.id.totStr)).getText().toString()));
        selectedPlayer.statsData.setConstitution(Integer.parseInt(((EditText) view.findViewById(R.id.totCon)).getText().toString()));
        selectedPlayer.statsData.setCharisma(Integer.parseInt(((EditText) view.findViewById(R.id.totCha)).getText().toString()));
        selectedPlayer.statsData.setIntelligence(Integer.parseInt(((EditText) view.findViewById(R.id.totInt)).getText().toString()));
        selectedPlayer.statsData.setWisdom(Integer.parseInt(((EditText) view.findViewById(R.id.totWis)).getText().toString()));
    }

    public void getPlayerSavingThrows() {
        selectedPlayer.statsData.chaSave = (((CheckBox) view.findViewById(R.id.saving_throws_cha)).isChecked());
        selectedPlayer.statsData.dexSave = (((CheckBox) view.findViewById(R.id.saving_throws_dex)).isChecked());
        selectedPlayer.statsData.conSave = (((CheckBox) view.findViewById(R.id.saving_throws_con)).isChecked());
        selectedPlayer.statsData.strSave = (((CheckBox) view.findViewById(R.id.saving_throws_str)).isChecked());
        selectedPlayer.statsData.wisSave = (((CheckBox) view.findViewById(R.id.saving_throws_wis)).isChecked());
        selectedPlayer.statsData.intSave = (((CheckBox) view.findViewById(R.id.saving_throws_int)).isChecked());
    }

    private int getIntFromButton(int view_id) {
        return Integer.parseInt((String) ((Button) view.findViewById(view_id)).getText());
    }

    private void setButtonTextFromInt(int view_id, int value) {
        ((Button) view.findViewById(view_id)).setText(String.valueOf(value));
    }

    public void getPlayerProficiencies() throws JSONException {
        JSONObject obj = new JSONObject();


        obj.put("acrobatics", getIntFromButton(R.id.proficiency_acr));
        obj.put("animal_handling", getIntFromButton(R.id.proficiency_anh));
        obj.put("arcana", getIntFromButton(R.id.proficiency_arc));
        obj.put("athletics", getIntFromButton(R.id.proficiency_ath));
        obj.put("deception", getIntFromButton(R.id.proficiency_dec));
        obj.put("history", getIntFromButton(R.id.proficiency_his));
        obj.put("insight", getIntFromButton(R.id.proficiency_ins));
        obj.put("intimidation", getIntFromButton(R.id.proficiency_intimidation));
        obj.put("investigation", getIntFromButton(R.id.proficiency_inv));
        obj.put("medicine", getIntFromButton(R.id.proficiency_med));
        obj.put("nature", getIntFromButton(R.id.proficiency_nat));
        obj.put("perception", getIntFromButton(R.id.proficiency_perception));
        obj.put("performance", getIntFromButton(R.id.proficiency_per));
        obj.put("persuasion", getIntFromButton(R.id.proficiency_persuasion));
        obj.put("religion", getIntFromButton(R.id.proficiency_rel));
        obj.put("sleight_of_hand", getIntFromButton(R.id.proficiency_soh));
        obj.put("stealth", getIntFromButton(R.id.proficiency_ste));
        obj.put("survival", getIntFromButton(R.id.proficiency_sur));

        selectedPlayer.proficiencies.setData(obj);
    }

    public void setPlayerProficiencies() {
        setButtonTextFromInt(R.id.proficiency_acr, selectedPlayer.proficiencies.isAcrobatics());
        setButtonTextFromInt(R.id.proficiency_anh, selectedPlayer.proficiencies.isAnimalHandling());
        setButtonTextFromInt(R.id.proficiency_arc, selectedPlayer.proficiencies.isArcana());
        setButtonTextFromInt(R.id.proficiency_ath, selectedPlayer.proficiencies.isAthletics());
        setButtonTextFromInt(R.id.proficiency_dec, selectedPlayer.proficiencies.isDeception());
        setButtonTextFromInt(R.id.proficiency_his, selectedPlayer.proficiencies.isHistory());
        setButtonTextFromInt(R.id.proficiency_ins, selectedPlayer.proficiencies.isInsight());
        setButtonTextFromInt(R.id.proficiency_intimidation, selectedPlayer.proficiencies.isIntimidation());
        setButtonTextFromInt(R.id.proficiency_inv, selectedPlayer.proficiencies.isInvestigation());
        setButtonTextFromInt(R.id.proficiency_med, selectedPlayer.proficiencies.isMedicine());
        setButtonTextFromInt(R.id.proficiency_nat, selectedPlayer.proficiencies.isNature());
        setButtonTextFromInt(R.id.proficiency_perception, selectedPlayer.proficiencies.isPerception());
        setButtonTextFromInt(R.id.proficiency_per, selectedPlayer.proficiencies.isPerformance());
        setButtonTextFromInt(R.id.proficiency_persuasion, selectedPlayer.proficiencies.isPersuasion());
        setButtonTextFromInt(R.id.proficiency_rel, selectedPlayer.proficiencies.isReligion());
        setButtonTextFromInt(R.id.proficiency_soh, selectedPlayer.proficiencies.isSleightOfHand());
        setButtonTextFromInt(R.id.proficiency_ste, selectedPlayer.proficiencies.isStealth());
        setButtonTextFromInt(R.id.proficiency_sur, selectedPlayer.proficiencies.isSurvival());


        ((CheckBox) view.findViewById(R.id.saving_throws_cha)).setChecked(selectedPlayer.statsData.chaSave);
        ((CheckBox) view.findViewById(R.id.saving_throws_dex)).setChecked(selectedPlayer.statsData.dexSave);
        ((CheckBox) view.findViewById(R.id.saving_throws_str)).setChecked(selectedPlayer.statsData.strSave);
        ((CheckBox) view.findViewById(R.id.saving_throws_con)).setChecked(selectedPlayer.statsData.conSave);
        ((CheckBox) view.findViewById(R.id.saving_throws_int)).setChecked(selectedPlayer.statsData.intSave);
        ((CheckBox) view.findViewById(R.id.saving_throws_wis)).setChecked(selectedPlayer.statsData.wisSave);
    }

    public void setPlayerProficiencyBonus() {
        selectedPlayer.proficiencies.setSelectedPlayerData(selectedPlayer);

        ((TextView) view.findViewById(R.id.proficiency_acr_bon)).setText(selectedPlayer.proficiencies.getAcrobaticBonus());
        ((TextView) view.findViewById(R.id.proficiency_anh_bon)).setText(selectedPlayer.proficiencies.getAnimalHandlingBonus());
        ((TextView) view.findViewById(R.id.proficiency_arc_bon)).setText(selectedPlayer.proficiencies.getArcanaBonus());
        ((TextView) view.findViewById(R.id.proficiency_ath_bon)).setText(selectedPlayer.proficiencies.getAthleticsBonus());
        ((TextView) view.findViewById(R.id.proficiency_dec_bon)).setText(selectedPlayer.proficiencies.getDeceptionBonus());
        ((TextView) view.findViewById(R.id.proficiency_his_bon)).setText(selectedPlayer.proficiencies.getHistoryBonus());
        ((TextView) view.findViewById(R.id.proficiency_ins_bon)).setText(selectedPlayer.proficiencies.getInsightBonus());
        ((TextView) view.findViewById(R.id.proficiency_intimidation_bon)).setText(selectedPlayer.proficiencies.getIntimidationBonus());
        ((TextView) view.findViewById(R.id.proficiency_inv_bon)).setText(selectedPlayer.proficiencies.getInvestigationBonus());
        ((TextView) view.findViewById(R.id.proficiency_med_bon)).setText(selectedPlayer.proficiencies.getMedicineBonus());
        ((TextView) view.findViewById(R.id.proficiency_nat_bon)).setText(selectedPlayer.proficiencies.getNatureBonus());
        ((TextView) view.findViewById(R.id.proficiency_perception_bon)).setText(selectedPlayer.proficiencies.getPerceptionBonus());
        ((TextView) view.findViewById(R.id.proficiency_per_bon)).setText(selectedPlayer.proficiencies.getPerformanceBonus());
        ((TextView) view.findViewById(R.id.proficiency_persuasion_bon)).setText(selectedPlayer.proficiencies.getPersuasionBonus());
        ((TextView) view.findViewById(R.id.proficiency_rel_bon)).setText(selectedPlayer.proficiencies.getReligionBonus());
        ((TextView) view.findViewById(R.id.proficiency_soh_bon)).setText(selectedPlayer.proficiencies.getSleightOfHandBonus());
        ((TextView) view.findViewById(R.id.proficiency_ste_bon)).setText(selectedPlayer.proficiencies.getStealthBonus());
        ((TextView) view.findViewById(R.id.proficiency_sur_bon)).setText(selectedPlayer.proficiencies.getSurvivalBonus());

        // Saving throws
        ((TextView) view.findViewById(R.id.saving_throws_cha_bon)).setText(selectedPlayer.statsData.getSavingThrowsCha());
        ((TextView) view.findViewById(R.id.saving_throws_dex_bon)).setText(selectedPlayer.statsData.getSavingThrowsDex());
        ((TextView) view.findViewById(R.id.saving_throws_str_bon)).setText(selectedPlayer.statsData.getSavingThrowsStr());
        ((TextView) view.findViewById(R.id.saving_throws_con_bon)).setText(selectedPlayer.statsData.getSavingThrowsCon());
        ((TextView) view.findViewById(R.id.saving_throws_int_bon)).setText(selectedPlayer.statsData.getSavingThrowsInt());
        ((TextView) view.findViewById(R.id.saving_throws_wis_bon)).setText(selectedPlayer.statsData.getSavingThrowsWis());
    }

    private void setStats() {
        EditText et;

        et = view.findViewById(R.id.totCha);
        et.setText(selectedPlayer.statsData.getCharisma());

        et = view.findViewById(R.id.totDex);
        et.setText(selectedPlayer.statsData.getDexterity());

        et = view.findViewById(R.id.totStr);
        et.setText(selectedPlayer.statsData.getStrength());

        et = view.findViewById(R.id.totInt);
        et.setText(selectedPlayer.statsData.getIntelligence());

        et = view.findViewById(R.id.totWis);
        et.setText(selectedPlayer.statsData.getWisdom());

        et = view.findViewById(R.id.totCon);
        et.setText(selectedPlayer.statsData.getConstitution());

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
