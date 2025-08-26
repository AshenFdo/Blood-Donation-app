package com.ashen.bdonorapp.Controller;

import android.util.Log;

import com.ashen.bdonorapp.Models.BloodRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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
        return db.collection("bloodRequests")
                .whereEqualTo("acceptedByUserId", null);
    }

    // Method to get all requests made by a specific user
    public Query getUserRequestsQuery(String userId) {
        return db.collection("bloodRequests")
                .whereEqualTo("userId", userId);
    }
}