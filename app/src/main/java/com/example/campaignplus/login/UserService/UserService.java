package com.example.campaignplus.login.UserService;

import android.util.Log;

import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public final class UserService {
    private UserService() {

    }

    public static void login(String username, String password, final CallBack f) throws JSONException {
        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        String json = data.toString();
        HttpUtils.post("login", json, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                f.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("UserService", "Login was successful.");
                    f.success();
                } else {
                    try {
                        String responseBody = response.body().string();
                        JSONObject errorResponse = new JSONObject(responseBody);
                        f.error(errorResponse.getString("message"));
                    } catch (JSONException e) {
                        f.error("Invalid JSON from server.");
                    }
                }
            }
        });
    }

    public static void logout(final CallBack fn) {
        HttpUtils.post("logout", null, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                fn.error(e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    fn.success();
                } else {
                    fn.error(response.message());
                }
            }
        });
    }
}
