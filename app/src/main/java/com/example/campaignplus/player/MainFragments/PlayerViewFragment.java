package com.example.campaignplus.player.MainFragments;

import static android.content.Context.MODE_PRIVATE;
import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.campaignplus.R;
import com.example.campaignplus._data.PlayerStatsData;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus._utils.TextProcessor;
import com.example.campaignplus.player.Fragments.StatsFragment;
import com.example.campaignplus.player.Listeners.TextOnChangeSaveListener;
import com.example.campaignplus.player.PlayerInfoActivity;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerViewFragment extends PlayerInfoFragment {
    private static final String TAG = "ItemViewActivity";

    private View view;
    private Toolbar toolbar;

    private TextView totalStrength;
    private TextView totalConstitution;
    private TextView totalDexterity;
    private EditText gold;
    private EditText silver;
    private EditText copper;
    private TextView totalWisdom;
    private TextView totalCharisma;
    private TextView totalIntelligence;

    private TextView strengthMod;
    private TextView dexterityMod;
    private TextView constitutionMod;
    private TextView wisdomMod;
    private TextView intelligenceMod;
    private TextView charismaMod;

    private TextView statArmorClass;
    private TextView statMaxHP;
    private TextView statLevel;
    private SharedPreferences preferences;

    private PlayerInfoActivity mainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (PlayerInfoActivity) getActivity();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_player_overview, container, false);

        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(selectedPlayer.getName());
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);

        registerStatViews();
        setStatsFields();
        setEventHandlers();

        return view;
    }

    private void setEventHandlers() {
        // Only update the money remotely after a short delay
        View.OnKeyListener sharedTextWatcher = new View.OnKeyListener() {
            final Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
            Runnable workRunnable;

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                selectedPlayer.gold = TextProcessor.parseInt(gold.getText().toString(), 0);
                selectedPlayer.silver = TextProcessor.parseInt(silver.getText().toString(), 0);
                selectedPlayer.copper = TextProcessor.parseInt(copper.getText().toString(), 0);

                handler.removeCallbacks(workRunnable);
                workRunnable = () -> uploadPlayerData(view);
                handler.postDelayed(workRunnable, 2000 /*delay*/);
                return false;
            }
        };

        /*
         * Open statsview when clicking the stats layout
         */
        view.findViewById(R.id.StatsLayout).setOnClickListener(view -> openStatsFragment());
        view.findViewById(R.id.levelGroup).setOnClickListener(view -> openStatsFragment());

        gold.setOnKeyListener(sharedTextWatcher);
        silver.setOnKeyListener(sharedTextWatcher);
        copper.setOnKeyListener(sharedTextWatcher);

        /*
         *  Get player information and try to load the correct player object into local views.
         */
        preferences = view.getContext().getSharedPreferences("PlayerData", MODE_PRIVATE);
        int playerId = preferences.getInt("player_id", -1);

        // Set onchange listener for current hp.
        ((EditText) view.findViewById(R.id.statCurrentHP)).setText(preferences.getString("current_hp", "0"));
        view.findViewById(R.id.statCurrentHP).setOnKeyListener(new TextOnChangeSaveListener(preferences, "current_hp"));

        // Set onchange listener for bonus hp.
        ((EditText) view.findViewById(R.id.statTemporaryHP)).setText(preferences.getString("temporary_hp", "0"));
        view.findViewById(R.id.statTemporaryHP).setOnKeyListener(new TextOnChangeSaveListener(preferences, "temporary_hp"));

    }

    private void openStatsFragment() {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment fragment = new StatsFragment();
        fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
        fragment.setExitTransition(new Slide(Gravity.TOP));
        ft.replace(R.id.player_info_drawer_layout, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void registerStatViews() {
        totalStrength = view.findViewById(R.id.statStrengthTotal);
        totalConstitution = view.findViewById(R.id.statConstitutionTotal);
        totalDexterity = view.findViewById(R.id.statDexterityTotal);
        totalWisdom = view.findViewById(R.id.statWisdomTotal);
        totalCharisma = view.findViewById(R.id.statCharismaTotal);
        totalIntelligence = view.findViewById(R.id.statIntelligenceTotal);

        gold = view.findViewById(R.id.money_gold);
        silver = view.findViewById(R.id.money_silver);
        copper = view.findViewById(R.id.money_copper);

        strengthMod = view.findViewById(R.id.stStrength);
        constitutionMod = view.findViewById(R.id.stConstitution);
        dexterityMod = view.findViewById(R.id.stDexterity);
        wisdomMod = view.findViewById(R.id.stWisdom);
        charismaMod = view.findViewById(R.id.stCharisma);
        intelligenceMod = view.findViewById(R.id.stIntelligence);

        statArmorClass = view.findViewById(R.id.statArmorClass);
        statMaxHP = view.findViewById(R.id.statMaxHP);
        statLevel = view.findViewById(R.id.level_value);
    }

    public void setStatsFields() {
        PlayerStatsData sd = selectedPlayer.statsData;
        TextView tv;

        // Base stats.
        totalStrength.setText(sd.getStrength());
        totalConstitution.setText(sd.getConstitution());
        totalDexterity.setText(sd.getDexterity());
        totalWisdom.setText(sd.getWisdom());
        totalCharisma.setText(sd.getCharisma());
        totalIntelligence.setText(sd.getIntelligence());

        // Stat modifier.
        strengthMod.setText(sd.getStrengthModifier());
        dexterityMod.setText(sd.getDexterityModifier());
        constitutionMod.setText(sd.getConstitutionModifier());
        wisdomMod.setText(sd.getWisdomModifier());
        intelligenceMod.setText(sd.getIntelligenceModifier());
        charismaMod.setText(sd.getCharismaModifier());

        // All other information
        statArmorClass.setText(sd.getArmorClass());
        statMaxHP.setText(sd.getMaxHP());
        statLevel.setText(sd.getLevel());


        gold.setText(String.valueOf(selectedPlayer.gold));
        silver.setText(String.valueOf(selectedPlayer.silver));
        copper.setText(String.valueOf(selectedPlayer.copper));
    }

    private void uploadPlayerData(View view) {
        try {
            selectedPlayer.upload(new CallBack() {
                @Override
                public void success() {
                    Toast.makeText(view.getContext(), "Uploaded player data.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void error(String errorMessage) {
                    Toast.makeText(view.getContext(), "Failed uploading player data: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            Toast.makeText(view.getContext(), "Failed uploading player data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpdateCurrentPlayer() {
        setStatsFields();
        toolbar.setTitle(selectedPlayer.getName());
    }
}
