package com.example.campaignplus.player.Fragments;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.view.HapticFeedbackConstants;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Objects;

import static com.example.campaignplus._data.DataCache.selectedPlayer;
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

    abstract public static class OnCompleteCallback {
        abstract public void success(int itemId);

        abstract public void cancel();
    }

    private OnCompleteCallback callback;

    public ItemInfoFragment(OnCompleteCallback cb) {
        this.callback = cb;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_item, container, false);

        registerSwipeRightExit(view.findViewById(R.id.fragment_player_item));

        Toolbar tb = view.findViewById(R.id.fragment_toolbar);
        tb.setTitle("Player Item Overview");
        registerExitFragmentButton(tb);

        // Update item on pressing save button
        tb.findViewById(R.id.save_fragment_button).setOnClickListener(view -> {
            try {
                updateItem();
            } catch (JSONException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        });

        amount = view.findViewById(R.id.item_info_amount);
        information = view.findViewById(R.id.item_info_description);

        // Update item
        TextView.OnEditorActionListener listener = (textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEND) {
                amount.clearFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(amount.getWindowToken(), 0);
            }
            return true;
        };

        amount.setOnEditorActionListener(listener);

        getItems();

        return view;
    }

    private void updateItem() throws JSONException, UnsupportedEncodingException {
        EquipmentItem item = selectedPlayer.equipment.get(selectedItemId);

        item.setAmount(Integer.parseInt(String.valueOf(amount.getText())));
        item.setDescription(String.valueOf(information.getText()));

        String url = String.format(Locale.ENGLISH, "player/%d/item/%d", selectedPlayer.getId(), item.getInstanceId());
        HttpUtils.put(url, item.toJSON().toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Updated item.", Toast.LENGTH_SHORT).show();
                        Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
                    });
                    callback.success(selectedItemId);
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show());
                }
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
                        callback.cancel();
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
        btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        btn.setOnClickListener(view -> {
            callback.cancel();
            // Remove current fragment
            Objects.requireNonNull(getActivity()).getSupportFragmentManager().popBackStackImmediate();
        });
        btn.setOnTouchListener(new ShortHapticFeedback());
    }

    private void getItems() {
        // Default item information is the first entry
        if (selectedItemId < 0 || selectedItemId >= selectedPlayer.equipment.size()) {
            selectedItemId = 0;
        }
        EquipmentItem current = selectedPlayer.equipment.get(selectedItemId);
        fillItemData(current);
        createItemDropdown();
    }

    private void createItemDropdown() {
        String[] itemNames = new String[selectedPlayer.equipment.size()];
        for (int i = 0; i < selectedPlayer.equipment.size(); i++) {
            itemNames[i] = selectedPlayer.equipment.get(i).getItem().getName();
        }

        Spinner spin = view.findViewById(R.id.name);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), R.layout.spinner_big_item, itemNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setSelection(selectedItemId);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fillItemData(selectedPlayer.equipment.get(position));
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
