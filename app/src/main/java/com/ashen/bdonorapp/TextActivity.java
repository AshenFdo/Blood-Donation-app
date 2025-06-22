package com.ashen.bdonorapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.ashen.bdonorapp.Controller.OnUserDataLoadedListener;
import com.ashen.bdonorapp.Controller.UserDataManager;
import com.google.android.material.button.MaterialButton;

public class TextActivity extends AppCompatActivity {


    private static final String TAG = "TextActivity";

    // Global variables (Activity fields) to store the user data
    private String currentUserName;
    private String currentUserEmail;
    private String currentUserBloodType;
    private String currentUserCity;

    MaterialButton btnTest02;

    private UserDataManager userDataManager;

    // Example UI elements (if you want to display the data)
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private TextView bloodTypeTextView;

    private TextView userCityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text);

        userNameTextView = findViewById(R.id.name);
        userEmailTextView = findViewById(R.id.email);
        bloodTypeTextView = findViewById(R.id.bloodType);
        userCityTextView = findViewById(R.id.city);



        // Initialize the UserDataManager
        userDataManager = new UserDataManager();

        // Call the function to fetch user data
        fetchAndDisplayUserData();

        btnTest02 = findViewById(R.id.test_02);
        btnTest02.setOnClickListener(v -> {
            // Navigate to TestActivity02 when the button is clicked
            startActivity(new Intent(TextActivity.this, TestActivity02.class));
        });

    }

    private void fetchAndDisplayUserData() {
        userDataManager.fetchUserData(new OnUserDataLoadedListener() {
            @Override
            public void onUserDataLoaded(String userName, String userEmail, String bloodType, String city) {
                // This method runs when the data is successfully fetched

                // 1. Store the values in the Activity's global variables
                currentUserName = userName;
                currentUserEmail = userEmail;
                currentUserBloodType = bloodType;
                currentUserCity = city;

                // Now you can use these variables throughout this Activity
                Log.d(TAG, "User data loaded!");
                Log.d(TAG, "Name: " + currentUserName);
                Log.d(TAG, "Email: " + currentUserEmail);
                Log.d(TAG, "Blood Type: " + currentUserBloodType);

                // 2. Optionally, update UI elements with the data
                if (userNameTextView != null) {
                    userNameTextView.setText("Name: " + (currentUserName != null ? currentUserName : "Not Set"));
                }
                if (userEmailTextView != null) {
                    userEmailTextView.setText("Email: " + (currentUserEmail != null ? currentUserEmail : "Not Set"));
                }
                if (bloodTypeTextView != null) {
                    bloodTypeTextView.setText("Blood Type: " + (currentUserBloodType != null ? currentUserBloodType : "Not Set"));
                }
                if (userCityTextView != null) {
                    userCityTextView.setText("Blood Type: " + (currentUserCity != null ? currentUserCity : "Not Set"));
                }
            }

            @Override
            public void onUserDataLoadFailed(String errorMessage) {
                // This method runs if there was an error or no user signed in
                Log.e(TAG, "Failed to load user data: " + errorMessage);
                // Handle the error, e.g., show a message to the user, redirect to login
                if (userNameTextView != null) userNameTextView.setText("Error loading data.");
                if (userEmailTextView != null) userEmailTextView.setText("");
                if (bloodTypeTextView != null) bloodTypeTextView.setText("");
            }
        });
    }



}