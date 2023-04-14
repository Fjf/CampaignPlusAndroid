package com.example.campaignplus.campaign.Fragments;

import static com.example.campaignplus._data.DataCache.playerData;

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

import com.example.campaignplus.campaign.Adapters.SpinnerInitialTextAdapter;
import com.example.campaignplus.R;
import com.example.campaignplus._data.PlayerData;
import com.example.campaignplus._utils.HttpUtils;
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
 * to handle interaction events.
 * Use the {@link SelectPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectPlayerFragment extends Fragment {
    private static final String PARAM_CAMPAIGN_CODE = "campaignCode";
    private String campaignCode;
    private final String TAG = "SelectPlayerFragment2";

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
        assert campaignCode != null;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_player, container, false);

        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        Spinner playerSpinner = view.findViewById(R.id.player_spinner);
        
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

    /**
     * Updates the campaign for the selected character.
     * @param playerId the id of the player character
     */
    private void updatePlayerCampaign(int playerId) throws UnsupportedEncodingException, JSONException {
        final JSONObject data = new JSONObject();
        data.put("campaign_code", campaignCode);
        StringEntity entity = new StringEntity(data.toString());

        String url = String.format(Locale.ENGLISH, "player/%s/campaign", playerId);
        HttpUtils.put(url, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Toast.makeText(getActivity(), "Something went wrong updating your player's campaign: " + response.toString(), Toast.LENGTH_SHORT).show();
                // Go back to parent.
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }
        });
    }
}
