package com.example.campaignplus.login;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.campaignplus._utils.HttpUtils;
import com.example.campaignplus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText email;
    private Button submit;
    private TextView info;
    private String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        submit = (Button)findViewById(R.id.fwSubmit);
        email = (EditText)findViewById(R.id.fwEmail);
        info = (TextView)findViewById(R.id.fwInfo);
    }

    public void validateSubmitButton(View view){
        try {
            validate(email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void validate(String email) throws JSONException {
        // Disable button
        submit.setEnabled(false);

        // Store all parameters in json object.
        JSONObject data = new JSONObject();
        data.put("email", email);
        String jsonStr = data.toString();

        HttpUtils.post("forgot_password", jsonStr, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                info.setText("Internal server error.");
                Log.d(TAG, "Request failed: " + e.getMessage());
                submit.setEnabled(true);
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String jsonResponse = response.body().string();
                            JSONObject serverResp = new JSONObject(jsonResponse);
                            Log.d(TAG, serverResp.toString());
                            if (serverResp.getBoolean("success")) {
                                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                info.setText(serverResp.optString("error", "Unknown error"));
                            }
                        } catch (Exception e) {
                            info.setText("Server response error.");
                            Log.d(TAG, "Invalid response: " + e.getMessage());
                        }
                    } else {
                        info.setText("Internal server error.");
                        Log.d(TAG, "HTTP error: " + response.code());
                    }
                    // Re enable button after login response.
                    submit.setEnabled(true);
                });
            }
        });
    }
}
