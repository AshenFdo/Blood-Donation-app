package com.ashen.bdonorapp.RequestModule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ashen.bdonorapp.Adapters.BloodRequestAdapter;
import com.ashen.bdonorapp.Controller.RequestCallback;
import com.ashen.bdonorapp.Models.BloodRequest;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView bloodTypeTextView, userNameTextView, userCityTextView, descriptionTextView, urgentTypeTextView , titleTextView;
    private Button acceptButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String requestId; // To store the Firestore document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bloodTypeTextView = findViewById(R.id.detail_blood_type);
        userNameTextView = findViewById(R.id.detail_user_name);
        userCityTextView = findViewById(R.id.detail_user_city);
        descriptionTextView = findViewById(R.id.detail_description);
        titleTextView = findViewById(R.id.detail_title);

        urgentTypeTextView = findViewById(R.id.detail_urgent_type);
        acceptButton = findViewById(R.id.button_accept_request);

        // Get data from the Intent
        requestId = getIntent().getStringExtra("requestId");

        loadRequestDetails();

        acceptButton.setOnClickListener(v -> acceptRequest());


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
                    }
                });
    }

    private void acceptRequest() {
        FirebaseFirestore.getInstance().collection("bloodRequests").document(requestId)
                .update("acceptedByUserId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addOnSuccessListener(aVoid -> finish());
    }



}