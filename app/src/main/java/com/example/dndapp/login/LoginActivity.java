package com.example.dndapp.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.example.dndapp.login.UserService.UserService;
import com.example.dndapp._utils.FunctionCall;
import com.example.dndapp.R;
import com.example.dndapp.player.PlayerInfoActivity;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.json.*;

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

        // Load previously stored username. (if exists)
        SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
        name.setText(preferences.getString("username", ""));

        Typeface font = Typeface.createFromAsset(getApplicationContext().getAssets(), "font/dungeon.TTF");
        info.setTypeface(font);

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

        UserService.login(userName, userPassword, new FunctionCall() {
            @Override
            public void success() {
                // Store username/pw in SharedPreferences for next login to be automatic.
                SharedPreferences preferences = getSharedPreferences("LoginData", MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();
                edit.putString("username", userName);
                edit.putString("password", userPassword);
                edit.apply();

                Intent intent = new Intent(LoginActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
                finish();

                login.setEnabled(true);
            }

            @Override
            public void error(String errorMessage) {
                info.setText(errorMessage);
                login.setEnabled(true);
            }
        });
    }
}
