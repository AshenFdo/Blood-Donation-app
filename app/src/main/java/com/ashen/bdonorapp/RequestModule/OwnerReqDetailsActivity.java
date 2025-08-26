package com.ashen.bdonorapp.RequestModule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ashen.bdonorapp.Controller.RequestCallback;
import com.ashen.bdonorapp.Controller.RequestDataManager;
import com.ashen.bdonorapp.MainActivity;
import com.ashen.bdonorapp.R;
import com.ashen.bdonorapp.authenticationModule.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerReqDetailsActivity extends AppCompatActivity {

    private TextView bloodTypeTextView, userNameTextView, userCityTextView, descriptionTextView, urgentTypeTextView , titleTextView
            , statusTextView, acceptedByTextView, acceptedContactTextView;

    private Button button_delete_request;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String requestId;

    private LinearLayout statusLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_owner_req_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bloodTypeTextView = findViewById(R.id.detail_blood_type);
        userNameTextView = findViewById(R.id.detail_user_name);
        userCityTextView = findViewById(R.id.detail_user_city);
        descriptionTextView = findViewById(R.id.detail_description);
        titleTextView = findViewById(R.id.detail_title);
        statusTextView = findViewById(R.id.Accept_status);
        statusLayout = findViewById(R.id.accepted_user_details_layout);
        acceptedByTextView = findViewById(R.id.request_accepted_by);
        acceptedContactTextView = findViewById(R.id.accepted_user_number);
        urgentTypeTextView = findViewById(R.id.detail_urgent_type);
        button_delete_request = findViewById(R.id.button_delete_request);

        // Get data from the Intent
        requestId = getIntent().getStringExtra("requestId");

      button_delete_request.setOnClickListener(v -> {
          RequestDataManager requestDataManager = new RequestDataManager();
          requestDataManager.deleteBloodRequest(requestId, new RequestCallback() {
              @Override
              public void onSuccess(String message) {
                  Toast.makeText(OwnerReqDetailsActivity.this, "Request deleted successfully", Toast.LENGTH_SHORT).show();
                  finish();
              }

              @Override
              public void onFailure(String errorMessage) {
                  Toast.makeText(OwnerReqDetailsActivity.this, "Failed to delete request: " + errorMessage, Toast.LENGTH_SHORT).show();
              }
          });
      });

        loadRequestDetails();
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

                        if(documentSnapshot.getString("acceptedByUserId") != null){
                            statusLayout.setVisibility(LinearLayout.VISIBLE);
                            acceptedByTextView.setText(documentSnapshot.getString("acceptedByUserName"));
                            acceptedContactTextView.setText(documentSnapshot.getString("acceptedByUserContact"));
                            statusTextView.setText("Accepted");


                        } else {
                            statusLayout.setVisibility(LinearLayout.GONE);
                            statusTextView.setText("Pending");
                        }


                        if (documentSnapshot.getString("title") != null) {
                            titleTextView.setText(documentSnapshot.getString("title"));
                        } else {
                            titleTextView.setText("Blood Request");
                        }
                        Log.d("RequestDetailsActivity", "Request details loaded successfully");

                    } else {
                        Log.d("RequestDetailsActivity", "No such document");
                        Toast.makeText(OwnerReqDetailsActivity.this, "Request not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RequestDetailsActivity", "Error loading request details: " + e.getMessage());
                    Toast.makeText(OwnerReqDetailsActivity.this, "Error loading request details", Toast.LENGTH_SHORT).show();
                });
    }

}