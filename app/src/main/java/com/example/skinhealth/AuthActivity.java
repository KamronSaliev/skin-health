package com.example.skinhealth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.skinhealth.ui.login.LoginActivity;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Button logInButton = findViewById(R.id.loginbutton);
        logInButton.setOnClickListener(v -> {
            Intent activityLogIn = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(activityLogIn);
            finish();
        });

        Button signUpButton = findViewById(R.id.signupbutton);
        signUpButton.setOnClickListener(v -> {
            // TODO: implement logic for sign up, update intent
            Intent activitySignUp = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(activitySignUp);
            finish();
        });
    }
}