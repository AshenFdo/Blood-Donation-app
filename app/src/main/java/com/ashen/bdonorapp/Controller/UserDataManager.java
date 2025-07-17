package com.ashen.bdonorapp.Controller;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDataManager {

    private static final String TAG = "UserDataManager";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public UserDataManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Fetches the current user's data including Auth profile and Firestore details.
     *
     * @param listener The listener to be notified when data is loaded or fails.
     */


    public void fetchUserData(OnUserDataLoadedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            // No user is signed in
            listener.onUserDataLoadFailed("No user is currently signed in.");
            return; // Stop here if no user
        }

        // User is signed in, get basic Auth info
        final String userEmail = user.getEmail();
        final String displayName = user.getDisplayName(); // Display name from Auth
        final String userUid = user.getUid();



        // Now fetch custom data (like blood type) from Firestore using the UID
        db.collection("users")
                .document(userUid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String bloodType = null;
                        String userName= null;
                        String city = null;

                        if (documentSnapshot.exists()) {
                            // Document exists, try to get the blood type field
                            bloodType = documentSnapshot.getString("bloodType");
                            if (bloodType == null) {
                                bloodType = "Not specified"; // Default value if not set
                            }

                            city = documentSnapshot.getString("city");
                            if (city == null) {
                                city = "Not specified"; // Default value if not set
                            }
                            Log.d(TAG, "User document exists in Firestore for UID: " + userUid);
                            Log.d(TAG, "City from Firestore: " + city);
                            Log.d(TAG, "Blood Type from Firestore: " + bloodType);


                            String nameFromFirestore = documentSnapshot.getString("name");
                            if (nameFromFirestore != null) {
                                userName = nameFromFirestore;
                            }


                        } else {
                            Log.d(TAG, "User document does not exist in Firestore for UID: " + userUid);
                            // The document might not exist if the user hasn't completed profile setup
                        }

                        // Data fetched (or not found), now call the success listener
                        listener.onUserDataLoaded(userName, userEmail, bloodType, city);

                }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors during the Firestore fetch
                        Log.w(TAG, "Error fetching user document from Firestore", e);
                        listener.onUserDataLoadFailed("Failed to fetch user data: " + e.getMessage());
                    }
                });

    }

    public void updateUserProfile(String name , String email, String bloodType,String city,  OnUserDataLoadedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseUser eUser = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            listener.onUserDataLoadFailed("No user is currently signed in.");
            return;
        }

            // User is signed in, get their UID
            String userUid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Counter to track completed operations
        final int[] completedOperations = {0};
        final int[] totalOperations = {0};
        final boolean[] hasError = {false};

        // Count total operations needed
        if (name != null && !name.isEmpty()) totalOperations[0]++;
        if (email != null && !email.isEmpty()) totalOperations[0] += 2; // Auth + Firestore
        if (city != null && !city.isEmpty()) totalOperations[0]++;
        if (bloodType != null && !bloodType.isEmpty()) totalOperations[0]++;

        if (totalOperations[0] == 0) {
            listener.onUserDataLoadFailed("No valid data to update");
            return;
        }

        // Helper method to check if all operations are complete
        Runnable checkCompletion = () -> {
            completedOperations[0]++;
            if (completedOperations[0] >= totalOperations[0]) {
                if (!hasError[0]) {
                    listener.onUserDataLoaded(name, email, bloodType, city);
                }
            }
        };

        user.updateEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User email address updated in Auth.");
                        // Update email in Firestore
                        db.collection("users").document(userUid)
                                .update("email", email)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Email updated in Firestore.");
                                    checkCompletion.run();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error updating email in Firestore", e);
                                    if (!hasError[0]) {
                                        hasError[0] = true;
                                        listener.onUserDataLoadFailed("Failed to update email in database: " + e.getMessage());
                                    }
                                });
                    } else {
                        Exception exception = task.getException();
                        Log.w(TAG, "Failed to update user email in Auth.", exception);

                        String errorMessage = "Failed to update email";
                        if (exception instanceof FirebaseAuthRecentLoginRequiredException) {
                            errorMessage = "Please sign out and sign in again before changing your email";
                        } else if (exception != null) {
                            errorMessage = "Failed to update email: " + exception.getMessage();
                        }

                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onUserDataLoadFailed(errorMessage);
                        }
                    }
                });

        // Update name
        if (name != null && !name.isEmpty()) {
            db.collection("users").document(userUid)
                    .update("name", name)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User name updated successfully.");
                        checkCompletion.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating user name", e);
                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onUserDataLoadFailed("Failed to update name: " + e.getMessage());
                        }
                    });
        }

        // Update city
        if (city != null && !city.isEmpty()) {
            db.collection("users").document(userUid)
                    .update("city", city)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "City updated successfully.");
                        checkCompletion.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating city", e);
                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onUserDataLoadFailed("Failed to update city: " + e.getMessage());
                        }
                    });
        }

        // Update blood type
        if (bloodType != null && !bloodType.isEmpty()) {
            db.collection("users").document(userUid)
                    .update("bloodType", bloodType)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Blood type updated successfully.");
                        checkCompletion.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating blood type", e);
                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onUserDataLoadFailed("Failed to update blood type: " + e.getMessage());
                        }
                    });
        }






        }





    //Get current Blood type
    public String getCurrentUserName() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userUid = user.getUid();
            DocumentSnapshot documentSnapshot = db.collection("users").document(userUid).get().getResult();
            if (documentSnapshot != null && documentSnapshot.exists()) {
                return documentSnapshot.getString("Name");
            }
        }
        return null; // Return null if no user or blood type not found
    }

}

