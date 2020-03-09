package com.example.dndapp.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.example.dndapp.Playthrough.PlaythroughOverviewActivity;
import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.json.*;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText name;
    private TextInputEditText password;
    private TextView info;
    private Button login;
    private SharedPreferences sharedPreferences;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.usernameField);
        password = findViewById(R.id.passwordField);
        info = findViewById(R.id.attemptsField);
        login = findViewById(R.id.loginButton);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/dungeon.TTF");
        info.setTypeface(font);

        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip_address", getString(R.string.default_ip));
        String userName = sharedPreferences.getString("user_name", "");
        name.setText(userName);

        HttpUtils.setIp(ip);

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    validateLoginButton(login);
                    handled = true;
                }
                return handled;
            }
        });

        tryAutomaticLogin();
    }

    private void tryAutomaticLogin() {
        SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        if (username == null || password == null)
            return;

        try {
            validate(username, password);
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
            // We don't really care about the result of this, as it's just to try automatic login.
        }
    }

    public void validateRegisterTransition(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    public void settingsTransition(View view) {
        Intent intent = new Intent(LoginActivity.this, ServerSettingsActivity.class);
        startActivity(intent);
    }

    public void validateLoginButton(View view) {
        try {
            validate(Objects.requireNonNull(name.getText()).toString(), Objects.requireNonNull(password.getText()).toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void validateForgotPasswordButton(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void validate(final String userName, final String userPassword) throws JSONException, UnsupportedEncodingException {
        // Disable button
        login.setEnabled(false);

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("name", userName);
        data.put("password", userPassword);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("login", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    if (serverResp.getBoolean("success")) {
                        // Store user name in shared preferences for rest of the app.
                        SharedPreferences preferences = getSharedPreferences("PlayerData", MODE_PRIVATE);
                        SharedPreferences.Editor edit = preferences.edit();
                        edit.putString("name", userName);
                        edit.apply();

                        // Store username/pw in SharedPreferences for next login to be automatic.
                        preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
                        edit = preferences.edit();
                        edit.putString("username", userName);
                        edit.putString("password", userPassword);
                        edit.apply();

                        Intent intent = new Intent(LoginActivity.this, PlaythroughOverviewActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        info.setText(serverResp.get("error").toString());
                    }

                } catch (JSONException e) {
                    info.setText("Server response error.");
                    Log.d(TAG, "Invalid response: " + response.toString());
                }

                // Re enable button after login response.
                login.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                info.setText("Failed to connect to the server.");
                Log.d(TAG, "Invalid response: " + response);

                // Re enable button after login response.
                login.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                String response = errorResponse == null ? null : errorResponse.toString();
                onFailure(statusCode, headers, response, throwable);
            }

        });
    }
}
