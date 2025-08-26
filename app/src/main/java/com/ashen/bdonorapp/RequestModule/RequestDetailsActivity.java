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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bloodTypeTextView = findViewById(R.id.detail_blood_type);
        userNameTextView = findViewById(R.id.detail_user_name);
        userCityTextView = findViewById(R.id.detail_user_city);
        descriptionTextView = findViewById(R.id.detail_description);
        titleTextView = findViewById(R.id.detail_title);


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

//    private void acceptRequest() {
//        String currentUserId = mAuth.getCurrentUser().getUid();
//        // Get the requester ID first, then call the proper method
//
//      db.collection("bloodRequests").document(requestId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        String requesterId = documentSnapshot.getString("postedByUserId");
//                        if(requesterId == currentUserId){
//
//                        }
//                        if (requesterId != null && !requesterId.isEmpty()) {
//
//                            acceptBloodRequestWithNotification(requestId, requesterId);
//                        } else {
//                            Toast.makeText(RequestDetailsActivity.this, "Error: Requester not found", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(RequestDetailsActivity.this, "Error loading request", Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void acceptBloodRequestWithNotification(String requestId, String requesterId) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // Update the blood request status
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("status", "accepted");
//        updates.put("acceptedByUserId", currentUserId);
//        updates.put("acceptedAt", System.currentTimeMillis());
//
//        db.collection("bloodRequests").document(requestId)
//                .update(updates)
//                .addOnSuccessListener(aVoid -> {
//                    // Get current user's name and send notification
//                    db.collection("users").document(currentUserId)
//                            .get()
//                            .addOnSuccessListener(document -> {
//                                String donorName = document.getString("userName");
//                                if (donorName == null) donorName = "A donor";
//
//                                // Send notification to requester
//
//                                // Create a chat room for communication
//                                createChatRoom(requesterId, currentUserId);
//
//                                Toast.makeText(RequestDetailsActivity.this, "Request Accepted! Notification sent.", Toast.LENGTH_SHORT).show();
//                            })
//                            .addOnFailureListener(e -> {
//                                Toast.makeText(RequestDetailsActivity.this, "Request accepted but notification failed", Toast.LENGTH_SHORT).show();
//                            });
//                    finish();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(RequestDetailsActivity.this, "Failed to accept request", Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void createChatRoom(String requesterId, String donorId) {
//        String chatRoomId = requesterId + "_" + donorId;
//
//        Map<String, Object> chatRoom = new HashMap<>();
//        chatRoom.put("participants", Arrays.asList(requesterId, donorId));
//        chatRoom.put("createdAt", System.currentTimeMillis());
//        chatRoom.put("lastMessage", "Blood request accepted! You can now communicate.");
//
//        FirebaseFirestore.getInstance()
//                .collection("chatRooms")
//                .document(chatRoomId)
//                .set(chatRoom);
//    }







}