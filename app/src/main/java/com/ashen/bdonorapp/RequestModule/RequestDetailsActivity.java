package com.ashen.bdonorapp.RequestModule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ashen.bdonorapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

        acceptButton.setOnClickListener(v -> {
            if (requestId != null) {
                acceptRequest();
                Toast.makeText(RequestDetailsActivity.this, "Request Accepted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RequestDetailsActivity.this, "Error accepting request", Toast.LENGTH_SHORT).show();
            }
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

    private void acceptRequest() {
        // Get the requester ID first, then call the proper method
        FirebaseFirestore.getInstance().collection("bloodRequests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String requesterId = documentSnapshot.getString("postedByUserId");
                        if (requesterId != null) {
                            // Call the method that handles notifications
                            acceptBloodRequestWithNotification(requestId, requesterId);
                        } else {
                            Toast.makeText(RequestDetailsActivity.this, "Error: Requester not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestDetailsActivity.this, "Error loading request", Toast.LENGTH_SHORT).show();
                });
    }

    private void acceptBloodRequestWithNotification(String requestId, String requesterId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update the blood request status
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("acceptedByUserId", currentUserId);
        updates.put("acceptedAt", System.currentTimeMillis());

        db.collection("bloodRequests").document(requestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Get current user's name and send notification
                    db.collection("users").document(currentUserId)
                            .get()
                            .addOnSuccessListener(document -> {
                                String donorName = document.getString("userName");
                                if (donorName == null) donorName = "A donor";

                                // Send notification to requester

                                // Create a chat room for communication
                                createChatRoom(requesterId, currentUserId);

                                Toast.makeText(RequestDetailsActivity.this, "Request Accepted! Notification sent.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RequestDetailsActivity.this, "Request accepted but notification failed", Toast.LENGTH_SHORT).show();
                            });
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RequestDetailsActivity.this, "Failed to accept request", Toast.LENGTH_SHORT).show();
                });
    }

    private void createChatRoom(String requesterId, String donorId) {
        String chatRoomId = requesterId + "_" + donorId;

        Map<String, Object> chatRoom = new HashMap<>();
        chatRoom.put("participants", Arrays.asList(requesterId, donorId));
        chatRoom.put("createdAt", System.currentTimeMillis());
        chatRoom.put("lastMessage", "Blood request accepted! You can now communicate.");

        FirebaseFirestore.getInstance()
                .collection("chatRooms")
                .document(chatRoomId)
                .set(chatRoom);
    }







}