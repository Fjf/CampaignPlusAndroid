package com.example.dndapp.login.UserService;

import com.example.dndapp._utils.CallBack;
import com.example.dndapp._utils.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public final class UserService {
    private UserService() {

    }

    public static void login(String username, String password, final CallBack f) throws JSONException, UnsupportedEncodingException {
        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("password", password);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("login", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                f.success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                f.error(responseString);
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    f.error(errorResponse.getString("message"));
                } catch (JSONException | NullPointerException e) {
                    f.error("Invalid JSON from server.");
                }
            }
        });
    }

    public static void logout(final CallBack fn) {
        HttpUtils.post("logout", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                fn.success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                fn.error("");
            }
        });
    }
}
