package com.example.dndapp.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.R;
import com.example.dndapp.Main.SecondActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import java.io.UnsupportedEncodingException;
import org.json.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LoginActivity extends AppCompatActivity {
    private EditText name;
    private EditText password;
    private TextView info;
    private Button login;
    private SharedPreferences sharedPreferences;
    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (EditText) findViewById(R.id.usernameField);
        password = (EditText) findViewById(R.id.passwordField);
        info = (TextView) findViewById(R.id.attemptsField);
        login = (Button) findViewById(R.id.loginButton);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/dungeon.TTF");
        info.setTypeface(font);

        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip_address", "127.0.0.1");
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
    }



    public void validateRegisterTransition(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void settingsTransition(View view) {
        Intent intent = new Intent(LoginActivity.this, ServerSettingsActivity.class);
        startActivity(intent);
    }

    public void validateLoginButton(View view){
        try {
            validate(name.getText().toString(), password.getText().toString());
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void validateForgotPasswordButton(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void validate(final String userName, String userPassword) throws JSONException, UnsupportedEncodingException {
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

                        // Store username in sharedpreferences for next login.
                        edit = sharedPreferences.edit();
                        edit.putString("user_name", userName);
                        edit.apply();

                        Intent intent = new Intent(LoginActivity.this, SecondActivity.class);
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
