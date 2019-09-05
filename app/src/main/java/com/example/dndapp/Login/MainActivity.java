package com.example.dndapp.Login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.PdfViewerActivity;
import com.example.dndapp.R;
import com.example.dndapp.Main.SecondActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import java.io.UnsupportedEncodingException;
import org.json.*;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {
    private EditText name;
    private EditText password;
    private TextView info;
    private Button login;
    private String TAG = "MainActivity";

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

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip_address", "127.0.0.1");
        Log.d(TAG, ip);
        HttpUtils.setIp(ip);
    }

    public void validateRegisterTransition(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void settingsTransition(View view) {
        Intent intent = new Intent(MainActivity.this, ServerSettingsActivity.class);
        startActivity(intent);
    }

    public void validateLoginButton(View view){
        try {
            validate(name.getText().toString(), password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void validateForgotPasswordButton(View view) {
        Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void validate(String userName, String userPassword) throws JSONException, UnsupportedEncodingException {
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
                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
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
