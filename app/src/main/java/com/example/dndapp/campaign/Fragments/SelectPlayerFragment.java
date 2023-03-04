package com.example.dndapp.campaign.Fragments;

import static com.example.dndapp._data.DataCache.playerData;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dndapp.campaign.Adapters.SpinnerInitialTextAdapter;
import com.example.dndapp.R;
import com.example.dndapp._data.MyPlayerCharacterList;
import com.example.dndapp._data.PlayerData;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SelectPlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SelectPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectPlayerFragment extends Fragment {
    private static final String PARAM_CAMPAIGN_CODE = "campaignCode";
    private OnFragmentInteractionListener mListener;
    private Toolbar tb;
    private Spinner playerSpinner;
    private String campaignCode;
    private String TAG = "SelectPlayerFragment2";

    public SelectPlayerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectPlayerFragment.
     */
    public static SelectPlayerFragment newInstance(String campaignCode) {
        SelectPlayerFragment fragment = new SelectPlayerFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_CAMPAIGN_CODE, campaignCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            campaignCode = getArguments().getString(PARAM_CAMPAIGN_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_player, container, false);

        tb = view.findViewById(R.id.fragment_toolbar);
        playerSpinner = view.findViewById(R.id.player_spinner);
        
        tb.setTitle("Campaign Character");

        ArrayAdapter<PlayerData> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, playerData);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final SpinnerInitialTextAdapter adapter = new SpinnerInitialTextAdapter(arrayAdapter, R.layout.player_spinner_nothing_selected_row, view.getContext());
        playerSpinner.setPrompt("Select your PC");
        playerSpinner.setAdapter(adapter);
        playerSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PlayerData item = (PlayerData) adapter.getItem(position);
                if (item == null)
                    return;

                try {
                    updatePlayerCampaign(item.getId());
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Updates the campaign for the selected character.
     * @param playerId the id of the player character
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */
    private void updatePlayerCampaign(int playerId) throws UnsupportedEncodingException, JSONException {
        final JSONObject data = new JSONObject();
        data.put("campaign_code", campaignCode);
        StringEntity entity = new StringEntity(data.toString());

        String url = String.format(Locale.ENGLISH, "player/%s/campaign", playerId);
        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!response.getBoolean("success")) {
                        Toast.makeText(getActivity(), "Something went wrong updating your player's campaign.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Go back to parent.
                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(getActivity(), "Something went wrong updating your player's campaign.", Toast.LENGTH_SHORT).show();
                // Go back to parent.
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }
        });
    }
}
