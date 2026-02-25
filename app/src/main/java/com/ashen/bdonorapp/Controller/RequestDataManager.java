package com.ashen.bdonorapp.Controller;

import android.util.Log;
import com.ashen.bdonorapp.Models.BloodRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

public class RequestDataManager {

    private static final String TAG = "BloodRequest";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public RequestDataManager() {
        // Initialize Firestore and Auth instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

   /*
   * Logic to create a new blood request in Firestore.
   * It takes a BloodRequest object and a callback to handle success or failure.
   * */
    public void createBloodRequest(BloodRequest request, RequestCallback callback) {
        db.collection("bloodRequests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess("Blood request added successfully!");
                    Log.d(TAG, "Blood request added with ID: " + documentReference.getId());
                    Log.d(TAG, "Blood request details: " + request.getBloodRequest());

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding request", e);
                    callback.onFailure("Error adding request: " + e.getMessage());
                });
    }






    /*
    * Logic to delete a blood request from Firestore.
    * */
    public void deleteBloodRequest(String requestId, RequestCallback callback) {
        if (requestId != null) {
            db.collection("bloodRequests").document(requestId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess("Request deleted successfully!");
                        Log.d(TAG, "Blood request deleted with ID: " + requestId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error deleting request", e);
                        callback.onFailure("Error deleting request: " + e.getMessage());
                    });
        } else {
            callback.onFailure("Request ID not found.");
        }
    }


    /*
    * Logic to get all active blood requests from Firestore.
    * Active requests are those that have not been accepted by any user yet (acceptedByUserId is null).
    * */
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

    /*
    * Logic to get all blood requests posted by a specific user from Firestore.
    * */
    public Query getUserRequestsQuery(String userId) {
        Log.d(TAG,"User requests query created successfully for userId: " + userId);
        return db.collection("bloodRequests")
                .whereEqualTo("postedByUserId", userId);

    }

    /*
    * Logic to accept a blood request in Firestore.
    *  It uses a transaction to ensure that the request is not accepted by multiple users at the same time.
    * */
    public void acceptRequest(String requestId, String acceptedName, String acceptedUserContact, RequestCallback callback) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(db.collection("bloodRequests").document(requestId));

                    // Check if the request is current user request
                    if(snapshot.getString("postedByUserId") == currentUserId){
                        try {
                            Log.d(TAG, "Cannot accept your own request");
                            throw new Exception("Cannot accept your own request");

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Check if the request is already accepted by another user
                    if (snapshot.getString("acceptedByUserId") != null) {
                        try {
                            throw new Exception("Request already accepted");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Update the request with the accepting user's information
                    transaction.update(
                            db.collection("bloodRequests")
                                    .document(requestId), "acceptedByUserId", currentUserId, "acceptedAt", new java.util.Date(),
                            "acceptedByUserName", acceptedName, "acceptedByUserContact", acceptedUserContact);

                    Log.d(TAG,"Request accepted successfully by user: " + currentUserId);

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess("Request accepted successfully!"))
                .addOnFailureListener(e -> callback.onFailure("Failed to accept request: " + e.getMessage()));
    }

    public void updateBloodRequest(String requestId, BloodRequest updatedRequest, RequestCallback callback) {
        if (requestId != null) {
            db.collection("bloodRequests").document(requestId)
                    .set(updatedRequest)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess("Request updated successfully!");
                        Log.d(TAG, "Blood request updated with ID: " + requestId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating request", e);
                        callback.onFailure("Error updating request: " + e.getMessage());
                    });
        } else {
            callback.onFailure("Request ID not found.");
        }
    }

}