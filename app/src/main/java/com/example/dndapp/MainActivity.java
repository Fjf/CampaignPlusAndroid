package com.example.dndapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.*;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {
    private EditText name;
    private EditText password;
    private TextView info;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (EditText)findViewById(R.id.usernameField);
        password = (EditText)findViewById(R.id.passwordField);
        info = (TextView)findViewById(R.id.attemptsField);
        login = (Button)findViewById(R.id.loginButton);

    }

    public void validateButton(View view){
        try {
            validate(name.getText().toString(), password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void validate(String userName, String userPassword) throws JSONException, UnsupportedEncodingException {
//        RequestParams rp = new RequestParams();
        JSONObject login = new JSONObject();
        login.put("name", userName);
        login.put("password", userPassword);
        StringEntity entity = new StringEntity(login.toString());
        HttpUtils.post("login", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asd", "---------------- this is response : " + response);
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    if (serverResp.getBoolean("success")) {
                        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                        startActivity(intent);
                    } else {
//
                        info.setText(serverResp.get("error").toString());
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asd", "---------------- ERROR response: " + response);
                info.setText("NOT SUCCESFUL");

            }
        });
    }
}
