package com.ashen.bdonorapp.Controller;

import android.util.Log;

import com.ashen.bdonorapp.Models.BloodRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.Objects;

public class RequestDataManager {

    private static final String TAG = "RequestDataManager";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public RequestDataManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    // Method to Create the blood Requests
    public void createBloodRequest(BloodRequest request, RequestCallback callback) {
        db.collection("bloodRequests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess("Blood request added successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding request", e);
                    callback.onFailure("Error adding request: " + e.getMessage());
                });
    }

    //Update blood request

    public void updateBloodRequest(String requestId, BloodRequest updatedRequest, RequestCallback callback) {
        if (requestId != null) {
            db.collection("bloodRequests").document(requestId)
                    .set(updatedRequest)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess("Request updated successfully!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating request", e);
                        callback.onFailure("Error updating request: " + e.getMessage());
                    });
        } else {
            callback.onFailure("Request ID not found.");
        }
    }



    //Method to Delete active blood requests
    public void deleteBloodRequest(String requestId, RequestCallback callback) {
        if (requestId != null) {
            db.collection("bloodRequests").document(requestId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess("Request deleted successfully!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting request", e);
                        callback.onFailure("Error deleting request: " + e.getMessage());
                    });
        } else {
            callback.onFailure("Request ID not found.");
        }
    }


    //Method to Get all active blood requests
    public Query getActiveRequestsQuery() {
        try{
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d(TAG, "Active requests query created successfully.");
            return db.collection("bloodRequests")
                    .whereEqualTo("acceptedByUserId", null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

}

    // Method to get all requests made by a specific user
    public Query getUserRequestsQuery(String userId) {
        return db.collection("bloodRequests")
                .whereEqualTo("postedByUserId", userId);
    }



    // Method to force refresh data
    public void refreshActiveRequests() {
        // This can be called after operations to ensure immediate updates
        FirebaseFirestore.getInstance().clearPersistence();
    }

    // Method to accept a request using a transaction
    public void acceptRequest(String requestId, String acceptedName, String acceptedUserContact, RequestCallback callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(db.collection("bloodRequests").document(requestId));

                    if(snapshot.getString("postedByUserId") == currentUserId){
                        try {
                            throw new Exception("Cannot accept your own request");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (snapshot.getString("acceptedByUserId") != null) {
                        try {
                            throw new Exception("Request already accepted");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    transaction.update(
                            db.collection("bloodRequests")
                                    .document(requestId), "acceptedByUserId", currentUserId, "acceptedAt", new java.util.Date(),
                            "acceptedByUserName", acceptedName, "acceptedByUserContact", acceptedUserContact);

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess("Request accepted successfully!"))
                .addOnFailureListener(e -> callback.onFailure("Failed to accept request: " + e.getMessage()));
    }
}