package com.example.dndapp.player.MainFragments;


import static com.example.dndapp._data.DataCache.selectedPlayer;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.dndapp.R;
import com.example.dndapp._data.classinfo.ClassAbility;
import com.example.dndapp._utils.CallBack;
import com.example.dndapp._utils.eventlisteners.ShortHapticFeedback;
import com.example.dndapp.campaign.Listeners.SwipeDismissListener;
import com.example.dndapp.player.Adapters.ClassAbilityAdapter;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassInformationFragment extends Fragment {
    private static final String ARG_PLAYER_IDX = "playerIdx";
    private Toolbar toolbar;
    private View view;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_class_information, container, false);

        toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Class Abilities");

        abilities = selectedPlayer.getSortedAbilities();
        adapter = new ClassAbilityAdapter(Objects.requireNonNull(getActivity()), abilities);
        final ListView lv = view.findViewById(R.id.class_info_abilities);
        lv.setAdapter(adapter);

        SearchView searchView = view.findViewById(R.id.search_fragment_button);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }



            @Override
            public boolean onQueryTextChange(String s) {
                lv.setAdapter(new ClassAbilityAdapter(Objects.requireNonNull(getActivity()), filterAbilities(s)));
                return false;
            }
        });

        view.setOnTouchListener(new SwipeDismissListener(new CallBack() {

            @Override
            public void success() {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }

            @Override
            public void error(String errorMessage) {

            }
        }));

        return view;
    }

    private ArrayList<ClassAbility> filterAbilities(String s) {
        ArrayList<ClassAbility> d = new ArrayList<>();

        for (ClassAbility ability : abilities) {
            if (ability.getDescription().toLowerCase().contains(s.toLowerCase())) {
                d.add(ability);
            }
        }
        return d;
    }
}
