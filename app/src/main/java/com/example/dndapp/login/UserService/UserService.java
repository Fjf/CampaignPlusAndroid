package com.example.dndapp.login.UserService;

import com.example.dndapp._utils.FunctionCall;
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

    public static void login(String username, String password, final FunctionCall f) throws JSONException, UnsupportedEncodingException {
        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("name", username);
        data.put("password", password);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("login", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                System.out.println("___________"+response.toString());

                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    if (serverResp.getBoolean("success")) {
                        f.success();
                    } else {
                        f.error(serverResp.getString("error"));
                    }
                } catch (JSONException e) {
                    f.error("Invalid JSON from server.");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    f.error(errorResponse.getString("error"));
                } catch (JSONException | NullPointerException e) {
                    f.error("Invalid JSON from server.");
                }
            }
        });
    }
}
