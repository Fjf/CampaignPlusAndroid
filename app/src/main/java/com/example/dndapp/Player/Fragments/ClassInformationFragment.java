package com.example.dndapp.Player.Fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.dndapp.Player.Adapters.ClassAbilityAdapter;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp.R;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.PlayerData;

import static com.example.dndapp.Player.PlayerInfoActivity.selectedPlayer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassInformationFragment extends Fragment {
    private static final String ARG_PLAYER_IDX = "playerIdx";

    private View view;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_class_information, container, false);

        final ListView lv = view.findViewById(R.id.class_info_abilities);

        Toolbar tb = view.findViewById(R.id.toolbar);
        tb.setTitle("Class Abilities");

        final PlayerData playerData = selectedPlayer;
        playerData.updateMainClassInfos(this.getContext(), new FunctionCall() {
            @Override
            public void success() {
                lv.setAdapter(new ClassAbilityAdapter(getActivity(), playerData.getAllAbilities()));
            }

            @Override
            public void error(String errorMessage) {
                // TODO: Maybe something here I currently dont care.
            }
        });

        return view;
    }
}