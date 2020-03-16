package com.example.dndapp.login;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.dndapp._utils.HttpUtils;
import com.example.dndapp.R;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
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

        submit = (Button)findViewById(R.id.fwSubmit);
        email = (EditText)findViewById(R.id.fwEmail);
        info = (TextView)findViewById(R.id.fwInfo);
    }

    public void validateSubmitButton(View view){
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
        StringEntity entity = new StringEntity(data.toString());

        HttpUtils.post("forgot_password", entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject serverResp = new JSONObject(response.toString());
                    Log.d(TAG, serverResp.toString());
                    if (serverResp.getBoolean("success")) {
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
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
                submit.setEnabled(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                info.setText("Internal server error.");
                Log.d(TAG, "Invalid response: " + response);

                // Re enable button after login response.
                submit.setEnabled(true);
            }
        });
    }
}
