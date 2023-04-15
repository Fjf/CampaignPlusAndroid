package com.example.campaignplus.player.MainFragments;


import static com.example.campaignplus._data.DataCache.selectedPlayer;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.campaignplus.R;
import com.example.campaignplus._data.DataCache;
import com.example.campaignplus._data.classinfo.ClassAbility;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus.campaign.Listeners.SwipeDismissListener;
import com.example.campaignplus.player.Adapters.ClassAbilityAdapter;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassInformationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassInformationFragment extends Fragment {
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
     * @param mainClassIds the mainClass ids for which to show abilities
     * @param subClassIds the subClass ids for which to show abilities
     * @return A new instance of fragment ClassInformationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClassInformationFragment newInstance(ArrayList<Integer> mainClassIds, ArrayList<Integer> subClassIds) {
        ClassInformationFragment fragment = new ClassInformationFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList("main_class_ids", mainClassIds);
        args.putIntegerArrayList("sub_class_ids", subClassIds);
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

        Bundle args = getArguments();
        assert args != null;
        ArrayList<Integer> mainClassIds = args.getIntegerArrayList("main_class_ids");
        ArrayList<Integer> subclassIds = args.getIntegerArrayList("sub_class_ids");

        abilities = DataCache.getSortedAbilities(mainClassIds, subclassIds);
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
