package com.example.dndapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerName;
    private EditText registerPassword;
    private EditText registerEmail;
    private EditText registerReenterPassword;
    private TextView registerInfo;
    private Button registerButton;
    private final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerName = (EditText)findViewById(R.id.registerName);
        registerPassword = (EditText)findViewById(R.id.registerPassword);
        registerReenterPassword = (EditText)findViewById(R.id.registerReenterPassword);
        registerEmail = (EditText)findViewById(R.id.registerEmail);
        registerInfo = (TextView)findViewById(R.id.registerInfo);
        registerButton = (Button)findViewById(R.id.registerButton);

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/dungeon.TTF");
        registerInfo.setTypeface(font);
    }

    public void validateRegisterButton(View view){
        try {

            if(registerPassword.getText().toString().equals(registerReenterPassword.getText().toString())) {
                validate(registerName.getText().toString(), registerPassword.getText().toString(), registerEmail.getText().toString());
            } else {
                registerInfo.setText("Passwords did not match");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void validate(String name, String password, String email) throws JSONException, UnsupportedEncodingException {
        // Disable button
        registerButton.setEnabled(false);

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("password", password);
        data.put("email", email);
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("register", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // TODO: This might be redundant as the function returns a JSONobject.
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    if (serverResp.getBoolean("success")) {
                        Intent intent = new Intent(RegisterActivity.this, SecondActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        registerInfo.setText(serverResp.get("error").toString());
                    }

                } catch (JSONException e) {
                    registerInfo.setText("Server response error.");
                    Log.d(TAG, "Invalid response: " + response.toString());
                }

                // Re enable button after login response.
                registerButton.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                registerInfo.setText("Internal server error.");
                Log.d(TAG, "Invalid response: " + response);

                // Re enable button after login response.
                registerInfo.setEnabled(true);
            }
        });
    }
}
