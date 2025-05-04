package com.example.campaignplus.login;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import cz.msebera.android.httpclient.entity.StringEntity;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText email;
    private Button submit;
    private TextView info;
    private String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        submit = (Button) findViewById(R.id.fwSubmit);
        email = (EditText) findViewById(R.id.fwEmail);
        info = (TextView) findViewById(R.id.fwInfo);
    }

    public void validateSubmitButton(View view) {
        try {
            validate(email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void validate(String email) throws JSONException, UnsupportedEncodingException {
        // Disable button
        submit.setEnabled(false);

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("email", email);

        HttpUtils.post("forgot_password", data.toString(), new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    info.setText("Internal server error.");
                    submit.setEnabled(true);
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                runOnUiThread(() -> submit.setEnabled(true));
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject serverResp = new JSONObject(responseBody);
                        if (serverResp.getBoolean("success")) {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errStr = serverResp.get("error").toString();
                            runOnUiThread(() -> {
                                info.setText(errStr);
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> info.setText("Server response error."));
                    }
                } else {
                    runOnUiThread(() -> info.setText("Internal server error."));
                }
            }
        });

    }
}
