package com.example.campaignplus.player.Fragments;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campaignplus.R;
import com.example.campaignplus._data.items.EquipmentItem;
import com.example.campaignplus._data.items.ItemData;
import com.example.campaignplus._data.items.ItemType;
import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus._utils.eventlisteners.ShortHapticFeedback;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Objects;

import static com.example.campaignplus._data.DataCache.selectedPlayer;
import static com.example.campaignplus.player.MainFragments.ItemViewFragment.pidDataSet;
import static com.example.campaignplus.player.MainFragments.ItemViewFragment.selectedItemId;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ItemInfoFragment extends Fragment {
    private String TAG = "PlayerItemActivity";

    private View view;
    private EditText amount;
    private EditText information;

    public ItemInfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_item, container, false);

        registerSwipeRightExit(view.findViewById(R.id.fragment_player_item));

        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        tb.setTitle("Player Item Overview");
        registerExitFragmentButton(tb);

        // Update item
        TextView.OnEditorActionListener listener = (textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEND) {
                try {
                    updateItem();
                } catch (JSONException | UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                amount.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(amount.getWindowToken(), 0);
            }
            return true;
        };

        amount = view.findViewById(R.id.item_info_amount);
        amount.setOnEditorActionListener(listener);

        information = view.findViewById(R.id.item_info_description);
        information.setOnEditorActionListener(listener);

        getItems();

        return view;
    }

    private void updateItem() throws JSONException, UnsupportedEncodingException {
        EquipmentItem item = pidDataSet[selectedItemId];

        item.setAmount(Integer.parseInt(String.valueOf(amount.getText())));
        item.setDescription(String.valueOf(information.getText()));

        StringEntity data = new StringEntity(item.toJSON().toString());

        String url = String.format(Locale.ENGLISH, "player/%d/item/%d", selectedPlayer.getId(), item.getInstanceId());
        HttpUtils.put(url, data, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                Toast.makeText(getContext(), "Updated item.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getContext(), "Error:" + errorResponse.toString(), Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private void registerSwipeRightExit(View viewById) {
        viewById.setOnTouchListener(new View.OnTouchListener() {
            private float x1;
            private float y1;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    x1 = motionEvent.getX();
                    y1 = motionEvent.getY();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    float x2 = motionEvent.getX();
                    float y2 = motionEvent.getY();

                    // Only activate when not moving down much, but moving a lot horizontally.
                    if (Math.abs(y1 - y2) < 100 &&
                            x2 - x1 > 100) {
                        // Close fragment
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                    }
                }

                return false;
            }
        });
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

    private void getItems() {
        // Default spell information is the first entry
        EquipmentItem current = pidDataSet[selectedItemId];
        createItemDropdown();
        fillItemData(current);
    }

    private void createItemDropdown() {
        String[] itemNames = new String[pidDataSet.length];
        for (int i = 0; i < pidDataSet.length; i++) {
            itemNames[i] = pidDataSet[i].getItem().getName();
        }

        Spinner spin = view.findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_big_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(selectedItemId);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillItemData(pidDataSet[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void fillItemData(EquipmentItem equipmentItem) {
        TextView na = view.findViewById(R.id.item_info_value);
        TextView description = view.findViewById(R.id.item_info_description);
        TextView info = view.findViewById(R.id.item_info_information);

        ItemData itemData = equipmentItem.getItem();

        na.setText(itemData.getNormalValue());
        amount.setText(String.valueOf(equipmentItem.getAmount()));
        description.setText(equipmentItem.getDescription());
        info.setText(equipmentItem.getItem().getDescription());
        if (itemData.getType() == ItemType.WEAPON) {
            hideAllBut(R.id.item_weapon_info);

            TextView da = view.findViewById(R.id.weapon_info_damage);
            TextView dt = view.findViewById(R.id.weapon_info_damage_type);
            TextView ra = view.findViewById(R.id.weapon_info_range);
            TextView tr = view.findViewById(R.id.weapon_info_throw_range);

            da.setText(itemData.getNormalDamage());
            dt.setText(itemData.getDamageType());
            ra.setText(itemData.getNormalRange());
            tr.setText(itemData.getNormalThrowRange());
        } else {
            hideAllBut(0);
        }
    }

    private void hideAllBut(int showId) {
        int[] ids = new int[]{R.id.item_weapon_info};

        for (int id : ids) {
            if (id == showId)
                view.findViewById(id).setVisibility(View.VISIBLE);
            else
                view.findViewById(id).setVisibility(View.GONE);
        }
    }

}
