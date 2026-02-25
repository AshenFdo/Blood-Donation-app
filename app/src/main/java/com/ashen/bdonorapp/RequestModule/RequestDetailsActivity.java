package com.ashen.bdonorapp.RequestModule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ashen.bdonorapp.Controller.RequestCallback;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.ashen.bdonorapp.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView bloodTypeTextView, userNameTextView, userCityTextView, descriptionTextView, urgentTypeTextView , titleTextView;
    private Button acceptButton;

    private TextInputEditText acceptedByEditText, acceptedContactNumber;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String requestId , acceptedUserName,acceptedUserContact; // To store the Firestore document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        // Initialize Firebase Auth and Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        bloodTypeTextView = findViewById(R.id.detail_blood_type);
        userNameTextView = findViewById(R.id.detail_user_name);
        userCityTextView = findViewById(R.id.detail_user_city);
        descriptionTextView = findViewById(R.id.detail_description);
        titleTextView = findViewById(R.id.detail_title);

        // Assign input fields ( for accepting request )
        acceptedByEditText = findViewById(R.id.accepted_user_name_textView);
        acceptedContactNumber =findViewById(R.id.contact_number);


        urgentTypeTextView = findViewById(R.id.detail_urgent_type);
        acceptButton = findViewById(R.id.button_accept_request);

        // Get data from the Intent
        requestId = getIntent().getStringExtra("requestId");


        loadRequestDetails();

        acceptButton.setOnClickListener(v -> {

            String acceptedUserName = Objects.requireNonNull(acceptedByEditText.getText()).toString().trim();
            String acceptedUserContact = Objects.requireNonNull(acceptedContactNumber.getText()).toString().trim();
            // Validate input
            if (acceptedUserName.isEmpty()) {
                acceptedByEditText.setError("Please enter your name");
                return;
            }
            if (acceptedUserContact.isEmpty() || acceptedUserContact.length() < 10) {
                acceptedContactNumber.setError("Please enter your contact number correctly");
                return;
            }
            // Call the acceptRequest method from RequestDataManager

                new RequestDataManager().acceptRequest(requestId,acceptedUserName,acceptedUserContact, new RequestCallback() {
                @Override
                public void onSuccess(String message) {
                 Toast.makeText(RequestDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(RequestDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


    }


    private void loadRequestDetails() {
        FirebaseFirestore.getInstance().collection("bloodRequests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bloodTypeTextView.setText(documentSnapshot.getString("bloodType"));
                        userNameTextView.setText(documentSnapshot.getString("userName"));
                        userCityTextView.setText(documentSnapshot.getString("userCity"));
                        urgentTypeTextView.setText(documentSnapshot.getString("urgentType"));
                        descriptionTextView.setText(documentSnapshot.getString("description"));
                        if (documentSnapshot.getString("title") != null) {
                            titleTextView.setText(documentSnapshot.getString("title"));
                        } else {
                            titleTextView.setText("Blood Request");
                        }
                        Log.d("RequestDetailsActivity", "Request details loaded successfully");

                    } else {
                        Log.d("RequestDetailsActivity", "No such document");
                        Toast.makeText(RequestDetailsActivity.this, "Request not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestDetailsActivity", "Error loading request details: " + e.getMessage());
                    Toast.makeText(RequestDetailsActivity.this, "Error loading request details", Toast.LENGTH_SHORT).show();
                });
    }
}