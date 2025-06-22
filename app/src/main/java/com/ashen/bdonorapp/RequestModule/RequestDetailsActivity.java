package com.ashen.bdonorapp.RequestModule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestDetailsActivity extends AppCompatActivity {

    private TextView bloodTypeTextView, userNameTextView, userCityTextView, descriptionTextView, urgentTypeTextView;
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
        urgentTypeTextView = findViewById(R.id.detail_urgent_type);
        acceptButton = findViewById(R.id.button_accept_request);

        // Get data from the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String bloodType = extras.getString("bloodType");
            String userName = extras.getString("userName");
            String userCity = extras.getString("userCity");
            String description = extras.getString("description");
            String urgentType = extras.getString("urgentType");
            requestId = extras.getString("requestId"); // Get the document ID

            bloodTypeTextView.setText(bloodType);
            userNameTextView.setText(userName);
            userCityTextView.setText(userCity);
            descriptionTextView.setText(description);
            urgentTypeTextView.setText(urgentType);
        }

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptBloodRequest();
            }
        });
    }

    private void acceptBloodRequest() {
        RequestDataManager requestManager = new RequestDataManager();

        requestManager.acceptBloodRequest(requestId, new RequestDataManager.RequestCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(RequestDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                finish();
                // Close activity and return to previous screen
                // Optionally, you can also refresh the request list in the previous activity
                // For example, if you have a static method to refresh the list in RequestPageFragment:

            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(RequestDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }



}