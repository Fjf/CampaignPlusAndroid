package com.example.campaignplus.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.campaignplus._data.items.AvailableItems;
import com.example.campaignplus.login.UserService.UserService;
import com.example.campaignplus.player.PlayerInfoActivity;
import com.example.campaignplus.R;
import com.example.campaignplus._utils.CallBack;
import com.example.campaignplus._utils.HttpUtils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip_address", getString(R.string.default_ip));

        HttpUtils.setIp(ip);
        HttpUtils.setPreferences(getBaseContext());
        HttpUtils.setCacheEnabled(false);
        HttpUtils.clearCache();

        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                try {
                    tryAutomaticLogin();
                } catch (UnsupportedEncodingException | JSONException e) {
                    Toast.makeText(LandingActivity.this, "Something went wrong attempting to connect to the server.", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000);

    }

    private void tryAutomaticLogin() throws UnsupportedEncodingException, JSONException {
        SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        // Validate credentials exist and are not empty
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        UserService.login(username, password, new CallBack() {
            @Override
            public void success() {
                Intent intent = new Intent(LandingActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void error(String errorMessage) {
                HttpUtils.setOfflineMode(false);
                // When login fails, redirect to login screen instead of PlayerInfoActivity
                Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}
