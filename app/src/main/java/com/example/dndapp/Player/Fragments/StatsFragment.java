package com.example.dndapp.Player.Fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dndapp.Player.PlayerInfoActivity;
import com.example.dndapp.R;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;
import static com.example.dndapp.Player.PlayerInfoActivity.playerId;
import static com.example.dndapp.Player.PlayerInfoActivity.playerProficiencyData;
import static com.example.dndapp.Player.PlayerInfoActivity.playerStatsData;

public class StatsFragment extends android.app.Fragment {
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
                return  i;
        }
        return -1;
    }

    /**
     * @param str Input string
     * @return The converted string, or -1 if no convertible number was given.
     */
    private int tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public StatsFragment() {
    }

    @Override
    public void onDestroy() {

        String st = ((TextView)view.findViewById(R.id.totStr)).getText().toString();
        String de = ((TextView)view.findViewById(R.id.totDex)).getText().toString();
        String co = ((TextView)view.findViewById(R.id.totCon)).getText().toString();
        String wi = ((TextView)view.findViewById(R.id.totWis)).getText().toString();
        String in = ((TextView)view.findViewById(R.id.totInt)).getText().toString();
        String ch = ((TextView)view.findViewById(R.id.totCha)).getText().toString();

        playerStatsData.setStrength(Integer.valueOf(st));
        playerStatsData.setDexterity(Integer.valueOf(de));
        playerStatsData.setConstitution(Integer.valueOf(co));
        playerStatsData.setWisdom(Integer.valueOf(wi));
        playerStatsData.setIntelligence(Integer.valueOf(in));
        playerStatsData.setCharisma(Integer.valueOf(ch));

        try {
            String url = String.format("player/%s/data", playerId);
            StringEntity entity = new StringEntity(playerStatsData.toJSON().toString());
            // Upload changed data to the server.
            HttpUtils.put(url, entity, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(view.getContext(), "Successfully uploaded player data.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(view.getContext(), "Something went wrong uploading player data.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
        PlayerInfoActivity.setStatsFields();

        super.onDestroy();
    }

    private void setOnChangeRefreshListeners() {
        int[] ids = new int[]{
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
                R.id.proficiency_sur,
                R.id.saving_throws_cha,
                R.id.saving_throws_dex,
                R.id.saving_throws_str,
                R.id.saving_throws_con,
                R.id.saving_throws_int,
                R.id.saving_throws_wis
        };

        for (int id : ids) {
            CheckBox vw = view.findViewById(id);

            vw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    try {
                        getPlayerProficiencies();
                        getPlayerSavingThrows();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setPlayerProficiencies();
                    setPlayerProficiencyBonus();
                }
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
        SharedPreferences preferences = getActivity().getSharedPreferences("PlayerData", MODE_PRIVATE);
        editor = preferences.edit();

        // Load the currently selected exhaustion button and the corresponding informational text.
        int id = preferences.getInt("current_exhaustion", radioButtons[0]);

        // See if the selected radiobutton still exists.
        if (indexOf(radioButtons, id) == -1)
            id = radioButtons[0];

        RadioButton rb = view.findViewById(id);
        rb.setChecked(true);
        setExhaustionInfoText(id);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setExhaustionInfoText(checkedId);

                editor.putInt("current_exhaustion", checkedId);
                editor.apply();
            }
        });

        setDefaultValues();
        setStats();
        setPlayerProficiencies();
        setPlayerProficiencyBonus();

        registerTextChangeListeners();
        setOnChangeRefreshListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setDefaultValues() {
        ((EditText) view.findViewById(R.id.level_input)).setText(playerStatsData.getLevel());
        ((EditText) view.findViewById(R.id.hp_input)).setText(playerStatsData.getMaxHP());
        ((EditText) view.findViewById(R.id.armor_input)).setText(playerStatsData.getArmorClass());
        ((EditText) view.findViewById(R.id.speed_input)).setText(playerStatsData.getSpeed());
    }


    private void registerTextChangeListeners() {
        // TODO: Make this generic and use less duplicate code.

        view.findViewById(R.id.level_input).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EditText et = (EditText) v;
                String text = et.getText().toString();
                if (text.length() > 0) {
                    playerStatsData.setLevel(Integer.valueOf(text));
                    setPlayerProficiencyBonus();
                }
                return false;
            }
        });

        view.findViewById(R.id.hp_input).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EditText et = (EditText) v;

                String text = et.getText().toString();
                if (text.length() > 0)
                    playerStatsData.setMaxHP(Integer.valueOf(text));
                return false;
            }
        });

        view.findViewById(R.id.armor_input).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EditText et = (EditText) v;
                String text = et.getText().toString();
                if (text.length() > 0)
                    playerStatsData.setArmorClass(Integer.valueOf(text));
                return false;
            }
        });

        view.findViewById(R.id.speed_input).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                EditText et = (EditText) v;
                String text = et.getText().toString();
                if (text.length() > 0)
                    playerStatsData.setSpeed(Integer.valueOf(text));
                return false;
            }
        });

        // Add eventlisteners for every stat input field.
        int[] statsIds = new int[]{R.id.totCha, R.id.totStr, R.id.totDex, R.id.totInt, R.id.totWis};
        for (int id : statsIds) {
            view.findViewById(id).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        getPlayerStats();
                        setPlayerProficiencyBonus();
                    }
                }
            });
        }
    }

    public void getPlayerStats() {
        try {
            playerStatsData.setDexterity(Integer.valueOf(((EditText) view.findViewById(R.id.totDex)).getText().toString()));
            playerStatsData.setStrength(Integer.valueOf(((EditText) view.findViewById(R.id.totStr)).getText().toString()));
            playerStatsData.setConstitution(Integer.valueOf(((EditText) view.findViewById(R.id.totCon)).getText().toString()));
            playerStatsData.setCharisma(Integer.valueOf(((EditText) view.findViewById(R.id.totCha)).getText().toString()));
            playerStatsData.setIntelligence(Integer.valueOf(((EditText) view.findViewById(R.id.totInt)).getText().toString()));
            playerStatsData.setWisdom(Integer.valueOf(((EditText) view.findViewById(R.id.totWis)).getText().toString()));
        } catch (NumberFormatException ignored) { }
    }

    public void getPlayerSavingThrows() {
        playerStatsData.setSaveCharisma(((CheckBox) view.findViewById(R.id.saving_throws_cha)).isChecked());
        playerStatsData.setSaveDexterity(((CheckBox) view.findViewById(R.id.saving_throws_dex)).isChecked());
        playerStatsData.setSaveConstitution(((CheckBox) view.findViewById(R.id.saving_throws_con)).isChecked());
        playerStatsData.setSaveStrength(((CheckBox) view.findViewById(R.id.saving_throws_str)).isChecked());
        playerStatsData.setSaveWisdom(((CheckBox) view.findViewById(R.id.saving_throws_wis)).isChecked());
        playerStatsData.setSaveIntelligence(((CheckBox) view.findViewById(R.id.saving_throws_int)).isChecked());
    }

    public JSONObject getPlayerProficiencies() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("acrobatics", ((CheckBox) view.findViewById(R.id.proficiency_acr)).isChecked());
        obj.put("animal_handling", ((CheckBox) view.findViewById(R.id.proficiency_anh)).isChecked());
        obj.put("arcana", ((CheckBox) view.findViewById(R.id.proficiency_arc)).isChecked());
        obj.put("athletics", ((CheckBox) view.findViewById(R.id.proficiency_ath)).isChecked());
        obj.put("deception", ((CheckBox) view.findViewById(R.id.proficiency_dec)).isChecked());
        obj.put("history", ((CheckBox) view.findViewById(R.id.proficiency_his)).isChecked());
        obj.put("insight", ((CheckBox) view.findViewById(R.id.proficiency_ins)).isChecked());
        obj.put("intimidation", ((CheckBox) view.findViewById(R.id.proficiency_intimidation)).isChecked());
        obj.put("investigation", ((CheckBox) view.findViewById(R.id.proficiency_inv)).isChecked());
        obj.put("medicine", ((CheckBox) view.findViewById(R.id.proficiency_med)).isChecked());
        obj.put("nature", ((CheckBox) view.findViewById(R.id.proficiency_nat)).isChecked());
        obj.put("perception", ((CheckBox) view.findViewById(R.id.proficiency_perception)).isChecked());
        obj.put("performance", ((CheckBox) view.findViewById(R.id.proficiency_per)).isChecked());
        obj.put("persuasion", ((CheckBox) view.findViewById(R.id.proficiency_persuasion)).isChecked());
        obj.put("religion", ((CheckBox) view.findViewById(R.id.proficiency_rel)).isChecked());
        obj.put("sleight_of_hand", ((CheckBox) view.findViewById(R.id.proficiency_soh)).isChecked());
        obj.put("stealth", ((CheckBox) view.findViewById(R.id.proficiency_ste)).isChecked());
        obj.put("survival", ((CheckBox) view.findViewById(R.id.proficiency_sur)).isChecked());

        playerProficiencyData.setData(obj);
        return obj;
    }

    public void setPlayerProficiencies() {
        ((CheckBox) view.findViewById(R.id.proficiency_acr)).setChecked(playerProficiencyData.isAcrobatics());
        ((CheckBox) view.findViewById(R.id.proficiency_anh)).setChecked(playerProficiencyData.isAnimalHandling());
        ((CheckBox) view.findViewById(R.id.proficiency_arc)).setChecked(playerProficiencyData.isArcana());
        ((CheckBox) view.findViewById(R.id.proficiency_ath)).setChecked(playerProficiencyData.isAthletics());
        ((CheckBox) view.findViewById(R.id.proficiency_dec)).setChecked(playerProficiencyData.isDeception());
        ((CheckBox) view.findViewById(R.id.proficiency_his)).setChecked(playerProficiencyData.isHistory());
        ((CheckBox) view.findViewById(R.id.proficiency_ins)).setChecked(playerProficiencyData.isInsight());
        ((CheckBox) view.findViewById(R.id.proficiency_intimidation)).setChecked(playerProficiencyData.isIntimidation());
        ((CheckBox) view.findViewById(R.id.proficiency_inv)).setChecked(playerProficiencyData.isInvestigation());
        ((CheckBox) view.findViewById(R.id.proficiency_med)).setChecked(playerProficiencyData.isMedicine());
        ((CheckBox) view.findViewById(R.id.proficiency_nat)).setChecked(playerProficiencyData.isNature());
        ((CheckBox) view.findViewById(R.id.proficiency_perception)).setChecked(playerProficiencyData.isPerception());
        ((CheckBox) view.findViewById(R.id.proficiency_per)).setChecked(playerProficiencyData.isPerformance());
        ((CheckBox) view.findViewById(R.id.proficiency_persuasion)).setChecked(playerProficiencyData.isPersuasion());
        ((CheckBox) view.findViewById(R.id.proficiency_rel)).setChecked(playerProficiencyData.isReligion());
        ((CheckBox) view.findViewById(R.id.proficiency_soh)).setChecked(playerProficiencyData.isSleightOfHand());
        ((CheckBox) view.findViewById(R.id.proficiency_ste)).setChecked(playerProficiencyData.isStealth());
        ((CheckBox) view.findViewById(R.id.proficiency_sur)).setChecked(playerProficiencyData.isSurvival());


        ((CheckBox) view.findViewById(R.id.saving_throws_cha)).setChecked(playerStatsData.isSaveCharisma());
        ((CheckBox) view.findViewById(R.id.saving_throws_dex)).setChecked(playerStatsData.isSaveDexterity());
        ((CheckBox) view.findViewById(R.id.saving_throws_str)).setChecked(playerStatsData.isSaveStrength());
        ((CheckBox) view.findViewById(R.id.saving_throws_con)).setChecked(playerStatsData.isSaveConstitution());
        ((CheckBox) view.findViewById(R.id.saving_throws_int)).setChecked(playerStatsData.isSaveIntelligence());
        ((CheckBox) view.findViewById(R.id.saving_throws_wis)).setChecked(playerStatsData.isSaveWisdom());
    }

    public void setPlayerProficiencyBonus() {
        ((TextView) view.findViewById(R.id.proficiency_acr_bon)).setText(playerProficiencyData.getAcrobaticBonus());
        ((TextView) view.findViewById(R.id.proficiency_anh_bon)).setText(playerProficiencyData.getAnimalHandlingBonus());
        ((TextView) view.findViewById(R.id.proficiency_arc_bon)).setText(playerProficiencyData.getArcanaBonus());
        ((TextView) view.findViewById(R.id.proficiency_ath_bon)).setText(playerProficiencyData.getAthleticsBonus());
        ((TextView) view.findViewById(R.id.proficiency_dec_bon)).setText(playerProficiencyData.getDeceptionBonus());
        ((TextView) view.findViewById(R.id.proficiency_his_bon)).setText(playerProficiencyData.getHistoryBonus());
        ((TextView) view.findViewById(R.id.proficiency_ins_bon)).setText(playerProficiencyData.getInsightBonus());
        ((TextView) view.findViewById(R.id.proficiency_intimidation_bon)).setText(playerProficiencyData.getIntimidationBonus());
        ((TextView) view.findViewById(R.id.proficiency_inv_bon)).setText(playerProficiencyData.getInvestigationBonus());
        ((TextView) view.findViewById(R.id.proficiency_med_bon)).setText(playerProficiencyData.getMedicineBonus());
        ((TextView) view.findViewById(R.id.proficiency_nat_bon)).setText(playerProficiencyData.getNatureBonus());
        ((TextView) view.findViewById(R.id.proficiency_perception_bon)).setText(playerProficiencyData.getPerceptionBonus());
        ((TextView) view.findViewById(R.id.proficiency_per_bon)).setText(playerProficiencyData.getPerformanceBonus());
        ((TextView) view.findViewById(R.id.proficiency_persuasion_bon)).setText(playerProficiencyData.getPersuasionBonus());
        ((TextView) view.findViewById(R.id.proficiency_rel_bon)).setText(playerProficiencyData.getReligionBonus());
        ((TextView) view.findViewById(R.id.proficiency_soh_bon)).setText(playerProficiencyData.getSleightOfHandBonus());
        ((TextView) view.findViewById(R.id.proficiency_ste_bon)).setText(playerProficiencyData.getStealthBonus());
        ((TextView) view.findViewById(R.id.proficiency_sur_bon)).setText(playerProficiencyData.getSurvivalBonus());

        // Saving throws
        ((TextView) view.findViewById(R.id.saving_throws_cha_bon)).setText(playerStatsData.getSavingThrowsCha());
        ((TextView) view.findViewById(R.id.saving_throws_dex_bon)).setText(playerStatsData.getSavingThrowsDex());
        ((TextView) view.findViewById(R.id.saving_throws_str_bon)).setText(playerStatsData.getSavingThrowsStr());
        ((TextView) view.findViewById(R.id.saving_throws_con_bon)).setText(playerStatsData.getSavingThrowsCon());
        ((TextView) view.findViewById(R.id.saving_throws_int_bon)).setText(playerStatsData.getSavingThrowsInt());
        ((TextView) view.findViewById(R.id.saving_throws_wis_bon)).setText(playerStatsData.getSavingThrowsWis());
    }

    private void setStats() {
        EditText et;

        et = view.findViewById(R.id.totCha);
        et.setText(playerStatsData.getCharisma());

        et = view.findViewById(R.id.totDex);
        et.setText(playerStatsData.getDexterity());

        et = view.findViewById(R.id.totStr);
        et.setText(playerStatsData.getStrength());

        et = view.findViewById(R.id.totInt);
        et.setText(playerStatsData.getIntelligence());

        et = view.findViewById(R.id.totWis);
        et.setText(playerStatsData.getWisdom());

        et = view.findViewById(R.id.totCon);
        et.setText(playerStatsData.getConstitution());

    }

    private void setExhaustionInfoText(int checkedId) {
        int id = indexOf(radioButtons, checkedId);
        if (id > -1 && id < arr.length)
            tv.setText(arr[id]);
    }
}
