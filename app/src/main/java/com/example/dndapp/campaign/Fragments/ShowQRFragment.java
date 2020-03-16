package com.example.dndapp.campaign.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp.campaign.Listeners.SwipeDismissListener;
import com.example.dndapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShowQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowQRFragment extends Fragment {
    private static final String ARG_CODE = "code";
    private static final String TAG = "ShowQRFragment2";

    private String code;
    private View view;

    public ShowQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param code Parameter 1.
     * @return A new instance of fragment ShowQRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowQRFragment newInstance(String code) {
        ShowQRFragment fragment = new ShowQRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CODE, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            code = getArguments().getString(ARG_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_show_qr, container, false);
        view.setOnTouchListener(new SwipeDismissListener(new FunctionCall() {
            @Override
            public void success() {
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
            }

            @Override
            public void error(String errorMessage) {
                // TODO: maybe user feedback idk now.
            }
        }));
        return view;
    }

    @Override
    public void onStart() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String port = sharedPreferences.getString("port", getString(R.string.default_port));
        String ip = sharedPreferences.getString("ip_address", getString(R.string.default_ip));

        String url = String.format(Locale.ENGLISH, "http://%s:%s/static/images/qr_codes/%s.png", ip, port, code);

        Log.d(TAG, url);
        ImageView imageView = view.findViewById(R.id.qr_code_image);
        Picasso.get().load(url).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Loading went correctly");
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Loading went wrong: " + e.getMessage());
            }
        });

        super.onStart();
    }
}
