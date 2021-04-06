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

        ImageButton logInButton = findViewById(R.id.loginbutton);
        logInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("Log In Button Clicked");
                Intent activityLogIn = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(activityLogIn);
                finish();
            }
        });

        //  //todo: link other login activity class
//        ImageButton signUpButton = findViewById(R.id.signupbutton);
//        signUpButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                System.out.println("Sign Up Button Clicked");
//                Intent activitySignUp = new Intent(getApplicationContext(), LoginActivity.class);
//                startActivity(activitySignUp);
//                finish();
//            }
//        });
    }
}