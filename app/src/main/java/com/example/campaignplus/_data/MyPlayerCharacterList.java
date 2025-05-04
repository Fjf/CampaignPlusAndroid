package com.example.campaignplus._data;

import com.example.campaignplus._data.classinfo.MainClassInfo;
import com.example.campaignplus._data.classinfo.SubClassInfo;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

public class MyPlayerCharacterList {
    private static final String TAG = "MyPlayerCharacterList";

    // This variable tracks whether or not the initial request has gone through yet.
    public static boolean hasInitialized = false;

    public MyPlayerCharacterList() { /* Nothing for static class */ }

    public static void initialize(final CallBack fn) {
        if (hasInitialized) {
            fn.success();
            return;
        }

        // Load iteratively
        updateClassData(new CallBack() {
            @Override
            public void success() {
                updatePlayerData(fn);
            }

            @Override
            public void error(String errorMessage) {
                fn.error(errorMessage);
            }
        });
        hasInitialized = true;
    }
public static void updatePlayerData(final CallBack fn) {
    HttpUtils.get("user/players", new okhttp3.Callback() {
        @Override
        public void onFailure(okhttp3.Call call, IOException e) {
            fn.error(e.getMessage());
        }

        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(responseBody);
                    DataCache.setPlayerData(jsonArray);
                    fn.success();
                } catch (JSONException e) {
                    fn.error(e.getMessage());
                }
            } else {
                fn.error(response.message());
            }
        }
    });
}

public static void updateClassData(final CallBack fn) {
    HttpUtils.get("user/classes", new okhttp3.Callback() {
        @Override
        public void onFailure(okhttp3.Call call, IOException e) {
            fn.error(e.getMessage());
        }

        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                try {
                    JSONArray classes = new JSONArray(responseBody);
                    DataCache.availableClasses.clear();
                    for (int i = 0; i < classes.length(); i++) {
                        MainClassInfo obj = new MainClassInfo(classes.getJSONObject(i));
                        DataCache.availableClasses.put(obj.getId(), obj);
                    }
                    updateSubClassData(fn);
                } catch (JSONException e) {
                    fn.error(e.getMessage());
                }
            } else {
                fn.error(response.message());
            }
        }
    });
}

public static void updateSubClassData(final CallBack fn) {
    HttpUtils.get("user/subclasses", new okhttp3.Callback() {
        @Override
        public void onFailure(okhttp3.Call call, IOException e) {
            fn.error(e.getMessage());
        }

        @Override
        public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                try {
                    JSONArray classes = new JSONArray(responseBody);
                    DataCache.availableSubClasses.clear();
                    for (int i = 0; i < classes.length(); i++) {
                        SubClassInfo obj = new SubClassInfo(classes.getJSONObject(i));
                        DataCache.availableSubClasses.put(obj.getId(), obj);
                    }
                    fn.success();
                } catch (JSONException e) {
                    fn.error(e.getMessage());
                }
            } else {
                fn.error(response.message());
            }
        }
    });
}



    public static PlayerData emptyPlayer() {
        return new PlayerData();
    }
}
