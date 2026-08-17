package com.example.campaignplus.login.UserService;

import android.util.Log;

import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public final class UserService {
    private UserService() {

    }

    public static void login(String username, String password, final CallBack f) throws JSONException {
        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        String jsonStr = data.toString();

        HttpUtils.post("login", jsonStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("!!!!!!!!!bla", "got errropr!!!!!!!!!!!");
                f.error(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.d("!!!!!!!!!bla", "got response!!!!!!!!!!!: " + responseBody);
                    Log.d("!!!!!!!!!bla", "Response status code: " + response.code());
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        Log.d("!!!!!!!!!bla", "Has 'success' field: " + json.has("success"));
                        Log.d("!!!!!!!!!bla", "Has 'message' field: " + json.has("message"));
                        if (json.has("success")) {
                            Log.d("!!!!!!!!!bla", "'success' value: " + json.getBoolean("success"));
                        }
                        if (json.has("message")) {
                            Log.d("!!!!!!!!!bla", "'message' value: " + json.getString("message"));
                        }
                        // Check if login was successful based on response
                        if (json.has("success") && json.getBoolean("success")) {
                            Log.d("!!!!!!!!!bla", "LOGIN SUCCESSFUL");
                            f.success();
                        } else if (json.has("message")) {
                            Log.d("!!!!!!!!!bla", "LOGIN FAILED: " + json.getString("message"));
                            f.error(json.getString("message"));
                        } else {
                            Log.d("!!!!!!!!!bla", "LOGIN SUCCESSFUL (no error fields)");
                            f.success(); // Assume success if no error message
                        }
                    } catch (JSONException e) {
                        // If not JSON, assume success
                        Log.d("!!!!!!!!!bla", "Response is not JSON, assuming success");
                        f.success();
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e("!!!!!!!!!bla", "Login failed (non-success status): " + errorBody);
                    f.error("Login failed: " + errorBody);
                }
            }
        });
    }

    public static void logout(final CallBack fn) {
        HttpUtils.post("logout", "", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fn.error(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    fn.success();
                } else {
                    fn.error("Logout failed");
                }
            }
        });
    }
}
