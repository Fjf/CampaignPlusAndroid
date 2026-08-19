package com.example.campaignplus.player.MainFragments;

import static android.content.Context.MODE_PRIVATE;
import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.transition.Slide;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campaignplus.R;
import com.example.campaignplus._data.Counter;
import com.example.campaignplus._data.PlayerStatsData;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.PlayerInfoFragment;
import com.example.campaignplus._utils.TextProcessor;
import com.example.campaignplus.player.Adapters.CounterAdapter;
import com.example.campaignplus.player.Fragments.LevelFragment;
import com.example.campaignplus.player.Fragments.StatsFragment;
import com.example.campaignplus.player.Listeners.TextOnChangeSaveListener;
import com.example.campaignplus.player.Adapters.SpellSlotItemAdapter;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.classinfo.MainClassInfo;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

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
    private TextView itemsSummary;
    private SharedPreferences preferences;
    
    private RecyclerView countersRecyclerView;
    private CounterAdapter countersAdapter;
    private ArrayList<Counter> countersList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_player_overview, container, false);

        toolbar = getActivity().findViewById(R.id.toolbar);
        String playerName = selectedPlayer.getName();
        toolbar.setTitle(playerName != null ? playerName : "Character Sheet");
        toolbar.setNavigationIcon(R.drawable.ic_menu_primary_24dp);

        int playerId = selectedPlayer.getId();
        preferences = view.getContext().getSharedPreferences("PlayerData_" + (playerId != -1 ? playerId : "default"), MODE_PRIVATE);

        registerStatViews();
        setStatsFields();
        setEventHandlers();
        
        // Fetch items to update the summary
        fetchItemsSummary();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCounters();
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
        view.findViewById(R.id.StatsLayout).setOnClickListener(view -> openFragment(
                new StatsFragment(new StatsFragment.OnCompleteCallback() {
                    @Override
                    public void success() {
                        setStatsFields();
                    }
                })));

        // Update the armorclass/level if we are done with updating the fields
        LevelFragment.OnCompleteCallback statsFieldUpdateCallback = new LevelFragment.OnCompleteCallback() {
            @Override
            public void success() {
                setStatsFields();
            }
        };
        view.findViewById(R.id.levelGroup).setOnClickListener(view -> openFragment(new LevelFragment(statsFieldUpdateCallback)));
        view.findViewById(R.id.statArmorClass).setOnClickListener(view -> openFragment(new LevelFragment(statsFieldUpdateCallback)));

        gold.setOnKeyListener(sharedTextWatcher);
        silver.setOnKeyListener(sharedTextWatcher);
        copper.setOnKeyListener(sharedTextWatcher);

        view.findViewById(R.id.statCurrentHP).setOnKeyListener(new TextOnChangeSaveListener(preferences, "current_hp"));
        view.findViewById(R.id.statTemporaryHP).setOnKeyListener(new TextOnChangeSaveListener(preferences, "temporary_hp"));
    }
    
    private void fetchItemsSummary() {
        // Fetch items to update the summary
        if (selectedPlayer != null && selectedPlayer.getId() != -1) {
            selectedPlayer.getEquipment(new CallBack() {
                @Override
                public void success() {
                    if (isAdded() && getActivity() != null && view != null && itemsSummary != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded() && itemsSummary != null) {
                                itemsSummary.setText(String.format(getString(R.string.items_count), 
                                    selectedPlayer.equipment.size()));
                            }
                        });
                    }
                }
                
                @Override
                public void error(String errorMessage) {
                    // Log error but don't crash
                    Log.d(TAG, "Error fetching items: " + errorMessage);
                }
            });
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
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
        itemsSummary = view.findViewById(R.id.items_summary);
    }

    public void setStatsFields() {
        PlayerStatsData sd = selectedPlayer.statsData;

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

        // Update items summary
        if (itemsSummary != null) {
            itemsSummary.setText(String.format(getString(R.string.items_count), 
                selectedPlayer.equipment != null ? selectedPlayer.equipment.size() : 0));
        }

        gold.setText(String.valueOf(selectedPlayer.gold));
        silver.setText(String.valueOf(selectedPlayer.silver));
        copper.setText(String.valueOf(selectedPlayer.copper));

        ((EditText) view.findViewById(R.id.statCurrentHP)).setText(preferences.getString("current_hp", "0"));
        ((EditText) view.findViewById(R.id.statTemporaryHP)).setText(preferences.getString("temporary_hp", "0"));

    }

    private void uploadPlayerData(View view) {
        selectedPlayer.upload(new CallBack() {
            @Override
            public void success() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        HttpUtils.showToast(view.getContext(), "Uploaded player data.", Toast.LENGTH_SHORT);
                    });
                }
            }

            @Override
            public void error(String errorMessage) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        HttpUtils.showToast(view.getContext(), "Failed uploading player data: " + errorMessage, Toast.LENGTH_LONG);
                    });
                }
            }
        });
    }

    @Override
    public void onUpdateCurrentPlayer() {
        // Ensure UI updates happen on the main thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Update the shared preferences to be updated for this player
                preferences = view.getContext().getSharedPreferences("PlayerData_" + selectedPlayer.getId(), MODE_PRIVATE);
                setStatsFields();
                toolbar.setTitle(selectedPlayer.getName());
                // Reload counters when player changes
                initCounters();
            });
        }
    }
    
    private void initCounters() {
        if (view == null) return;
        
        countersRecyclerView = view.findViewById(R.id.counters_recycler);
        countersRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        
        countersList = loadCounters();
        
        countersAdapter = new CounterAdapter(view.getContext(), countersList, (name, value) -> {
            saveCounterValue(name, value);
        }, (name) -> {
            deleteCounter(name);
        }, () -> {
            showAddCounterDialog();
        });
        countersRecyclerView.setAdapter(countersAdapter);
    }
    
    private ArrayList<Counter> loadCounters() {
        ArrayList<Counter> counters = new ArrayList<>();
        
        // First, add spell slot counters from table info
        counters.addAll(getSpellSlotCounters());
        
        // Then, add custom counters from SharedPreferences
        counters.addAll(loadCustomCounters());
        
        return counters;
    }
    
    private ArrayList<Counter> getSpellSlotCounters() {
        ArrayList<Counter> counters = new ArrayList<>();
        int level = Integer.parseInt(selectedPlayer.statsData.getLevel());
        
        for (int classId : selectedPlayer.mainClassIds) {
            MainClassInfo classInfo = DataCache.availableClasses.get(classId);
            if (classInfo == null) continue;
            
            ArrayList<String> keys = classInfo.getTableKeys();
            ArrayList<ArrayList<String>> values = classInfo.getTableValues();
            
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String maxValueStr = values.get(i).get(Math.min(level - 1, values.get(i).size() - 1));
                
                try {
                    int maxValue = Integer.parseInt(maxValueStr);
                    int currentValue = maxValue; // Default to max value
                    
                    // Try to get the value, handling both String and Integer storage formats
                    try {
                        // First try getting as String (SpellSlotItemAdapter format)
                        String storedValueStr = preferences.getString(key, String.valueOf(maxValue));
                        currentValue = Integer.parseInt(storedValueStr);
                    } catch (ClassCastException e) {
                        // Value was stored as Integer, try getting as Integer
                        currentValue = preferences.getInt(key, maxValue);
                    }
                    
                    counters.add(new Counter(key, maxValue, currentValue));
                } catch (NumberFormatException e) {
                    // Not a numeric counter, skip
                }
            }
        }
        
        return counters;
    }
    
    private ArrayList<Counter> loadCustomCounters() {
        ArrayList<Counter> counters = new ArrayList<>();
        String jsonStr = preferences.getString("custom_counters", "");
        
        if (jsonStr.isEmpty()) return counters;
        
        try {
            JSONArray jsonArr = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArr.length(); i++) {
                Counter counter = new Counter(jsonArr.getJSONObject(i));
                counter.setCustom(true);
                counters.add(counter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return counters;
    }
    
    private void saveCustomCounters() {
        JSONArray jsonArr = new JSONArray();
        for (Counter counter : countersList) {
            if (!isSpellSlotCounter(counter.getName())) {
                try {
                    jsonArr.put(counter.toJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        preferences.edit().putString("custom_counters", jsonArr.toString()).apply();
    }
    
    private boolean isSpellSlotCounter(String name) {
        // Check if this counter name matches any spell slot key
        for (int classId : selectedPlayer.mainClassIds) {
            MainClassInfo classInfo = DataCache.availableClasses.get(classId);
            if (classInfo == null) continue;
            if (classInfo.getTableKeys().contains(name)) {
                return true;
            }
        }
        return false;
    }
    
    private void saveCounterValue(String name, int value) {
        if (isSpellSlotCounter(name)) {
            // Save to preferences with the counter name as key
            preferences.edit().putInt(name, value).apply();
        } else {
            // Find and update custom counter
            for (Counter counter : countersList) {
                if (counter.getName().equals(name)) {
                    counter.setCurrentValue(value);
                    break;
                }
            }
            saveCustomCounters();
        }
    }
    
    private void showAddCounterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_counter, null);
        EditText nameInput = dialogView.findViewById(R.id.counter_name_input);
        EditText maxValueInput = dialogView.findViewById(R.id.counter_max_value_input);
        
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.add_counter)
            .setView(dialogView)
            .setPositiveButton("Add", (dialog, which) -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a counter name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                int maxValue = 0;
                try {
                    String maxStr = maxValueInput.getText().toString().trim();
                    if (!maxStr.isEmpty()) {
                        maxValue = Integer.parseInt(maxStr);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid max value", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Add counter
                Counter newCounter = new Counter(name, maxValue, maxValue > 0 ? maxValue : 1);
                newCounter.setCustom(true);
                countersList.add(newCounter);
                countersAdapter.notifyDataSetChanged();
                saveCustomCounters();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteCounter(String name) {
        countersList.removeIf(counter -> counter.getName().equals(name));
        countersAdapter.notifyDataSetChanged();
        saveCustomCounters();
    }
}
