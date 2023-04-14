package com.example.campaignplus.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.example.campaignplus.R;
import com.example.campaignplus._utils.HttpUtils;

public class ServerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String ip = sharedPreferences.getString("ip_address", getString(R.string.default_ip));

        // Set current IP to the stored value, or the default value of 127.0.0.1
        EditText et = findViewById(R.id.ipAddress);
        et.setText(ip);
    }

    public void updateIpAddress(View view) {
        EditText et = findViewById(R.id.ipAddress);
        String newIp = et.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ip_address", newIp);
        editor.apply();

        // Update utils class
        // TODO: refactor this so we dont have to do this manually.
        HttpUtils.setIp(newIp);

        finish();
    }

}
