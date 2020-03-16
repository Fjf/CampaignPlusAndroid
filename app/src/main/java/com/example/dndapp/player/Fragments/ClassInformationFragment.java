package com.example.dndapp.player.Fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.dndapp._data.classinfo.ClassAbility;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;
import com.example.dndapp.player.Adapters.ClassAbilityAdapter;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp.R;
import com.example.dndapp._data.PlayerData;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.dndapp.player.PlayerInfoActivity.selectedPlayer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassInformationFragment extends Fragment {
    private static final String ARG_PLAYER_IDX = "playerIdx";

    private ArrayList<ClassAbility> abilities;
    private ClassAbilityAdapter adapter;

    public ClassInformationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param playerIdx The player's idx for which to show information.
     * @return A new instance of fragment ClassInformationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClassInformationFragment newInstance(int playerIdx) {
        ClassInformationFragment fragment = new ClassInformationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYER_IDX, playerIdx);
        fragment.setArguments(args);
        return fragment;
    }

    private void registerExitFragmentButton(Toolbar tb) {
        View btn = tb.findViewById(R.id.close_fragment_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove current fragment
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_class_information, container, false);
        final ListView lv = view.findViewById(R.id.class_info_abilities);

        Toolbar tb = view.findViewById(R.id.toolbar);
        tb.setTitle("Class Abilities");
        registerExitFragmentButton(tb);

        final PlayerData playerData = selectedPlayer;

        abilities = new ArrayList<>();
        adapter = new ClassAbilityAdapter(Objects.requireNonNull(getActivity()), abilities);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView ability = view.findViewById(R.id.class_ability);
                // Toggle info visibility
                int current = ability.getVisibility();
                if (current == View.GONE) {
                    ability.setVisibility(View.VISIBLE);
                } else {
                    ability.setVisibility(View.GONE);
                }
            }
        });

        playerData.updateMainClassInfos(this.getContext(), new FunctionCall() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void success() {
                abilities.clear();
                abilities.addAll(playerData.getSortedAbilities());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void error(String errorMessage) {
                // TODO: Maybe something here I currently don't care.
            }
        });

        SearchView searchView = view.findViewById(R.id.search_fragment_button);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onQueryTextChange(String s) {

                lv.setAdapter(new ClassAbilityAdapter(getActivity(), filterAbilities(s)));
                return false;
            }
        });

        return view;
    }

    private ArrayList<ClassAbility> filterAbilities(String s) {
        ArrayList<ClassAbility> d = new ArrayList<>();

        for (ClassAbility ability : abilities) {
            if (ability.getInfo().toLowerCase().contains(s.toLowerCase()) ||
                ability.getName().toLowerCase().contains(s.toLowerCase())) {
                d.add(ability);
            }
        }
        return d;
    }
}
