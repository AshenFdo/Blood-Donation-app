package com.ashen.bdonorapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.ashen.bdonorapp.authenticationModule.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplachScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splach_screen);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // User is logged in, navigate to Home screen
                startActivity(new Intent(SplachScreen.this, MainActivity.class));
            } else {
                // User is not logged in, navigate to Login screen
                startActivity(new Intent(SplachScreen.this, LoginActivity.class));
            }
            finish(); // Prevent returning to this splash screen
        }, 500); // Delay for 500 milliseconds
    }
}