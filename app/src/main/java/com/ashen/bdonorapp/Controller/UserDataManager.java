package com.ashen.bdonorapp.Controller;

import android.graphics.Bitmap;
import android.util.Base64;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;

import java.io.ByteArrayOutputStream;

public class UserDataManager {

    private static final String TAG = "UserDataManager";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public UserDataManager() {

        // Initialize Firestore and Auth instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }



    /*
    * Method for fetching user data from Firebase Auth and Firestore.
    * */
    public void fetchUserData(OnUserDataLoadedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            // No user is signed in
            listener.onUserDataLoadFailed("No user is currently signed in.");

            return;
        }

        // User is signed in, get basic Auth info
        final String userEmail = user.getEmail();
        final String displayName = user.getDisplayName(); // Display name from Auth
        final String userUid = user.getUid();
        Source source = Source.DEFAULT;// Use CACHE to get data from local cache if available, otherwise use SERVER


        // fetch custom data  from Firestore using the UID
        db.collection("users")
                .document(userUid)
                .get(source)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String bloodType = null;
                        String userName = null;
                        String city = null;
                        String userProfileUrl = null;
                        String gender = null;


                        if (documentSnapshot.exists()) {
                            // Document exists, try to get the blood type field
                            bloodType = documentSnapshot.getString("bloodType");
                            if (bloodType == null) {
                                bloodType = "Not specified"; // Default value if not set
                            }
                            // Get other fields
                            userProfileUrl = documentSnapshot.getString("profileImage");
                            if (userProfileUrl == null) {
                                userProfileUrl = ""; // Default value if not set
                            }

                            city = documentSnapshot.getString("city");
                            if (city == null) {
                                city = "Not specified"; // Default value if not set
                            }

                            gender = documentSnapshot.getString("gender");
                            if (gender == null){
                                gender = "Not specified";
                            }
                            Log.d(TAG, "User document exists in Firestore for UID: " + userUid);
                            Log.d(TAG, "City from Firestore: " + city);
                            Log.d(TAG, "Blood Type from Firestore: " + bloodType);
                            Log.d(TAG, "Profile Image URL from Firestore: " + userProfileUrl);
                            Log.d(TAG,"Gender from" + gender);



                            String nameFromFirestore = documentSnapshot.getString("userName");
                            if (nameFromFirestore != null) {
                                userName = nameFromFirestore;
                            }


                        } else {
                            Log.d(TAG, "User document does not exist in Firestore for UID: " + userUid);
                        }

                        // Data fetched (or not found), now call the success listener
                        listener.onUserDataLoaded(userName, userEmail, bloodType, city, userProfileUrl,gender);

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


    /*
    * Method for updating user data in Firebase Auth and Firestore.
    *  It updates the email in Auth and other fields in Firestore.
    * */
    public void updateUserProfile(String name, String email, String bloodType, String city, String userProfileUrl,String gender, OnUserDataUpdateListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            listener.onFailure("No user is currently signed in.");
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
        if (userProfileUrl != null && !userProfileUrl.isEmpty()) totalOperations[0]++;
        if(gender != null && !gender.isEmpty()) totalOperations[0]++;

        if (totalOperations[0] == 0) {
            listener.onFailure("No valid data to update");
            return;
        }

        // Helper method to check if all operations are complete
        Runnable checkCompletion = () -> {
            completedOperations[0]++;
            if (completedOperations[0] >= totalOperations[0]) {
                if (!hasError[0]) {
                    listener.onSuccess("User profile updated successfully");
                }
            }
        };


        // Helper method to handle operation completion
        Runnable handleOperationComplete = () -> {
            synchronized (completedOperations) {
                completedOperations[0]++;
                Log.d(TAG, "Operation completed: " + completedOperations[0] + "/" + totalOperations[0]);

                if (completedOperations[0] >= totalOperations[0] && !hasError[0]) {
                    Log.d(TAG, "All operations completed successfully");
                    listener.onSuccess("User profile updated successfully");
                }
            }
        };

        // Update email in Auth
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
                                        listener.onFailure("Failed to update email in database: " + e.getMessage());
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
                            listener.onFailure(errorMessage);
                        }
                    }
                });

        // Update name
        if (name != null && !name.isEmpty()) {
            db.collection("users").document(userUid)
                    .update("userName", name)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User name updated successfully.");
                        checkCompletion.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating user name", e);
                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onFailure("Failed to update name: " + e.getMessage());
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
                            listener.onFailure("Failed to update city: " + e.getMessage());
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
                            listener.onFailure("Failed to update blood type: " + e.getMessage());
                        }
                    });
        }

        // Update profile image URL
        if (userProfileUrl != null && !userProfileUrl.isEmpty()) {
            db.collection("users").document(userUid)
                    .update("profileImage", userProfileUrl)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Profile image URL updated successfully.");
                        checkCompletion.run();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating profile image URL", e);
                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onFailure("Failed to update profile image URL: " + e.getMessage());
                        }
                    });
        }

        //Update Gender
        if(gender != null && !gender.isEmpty()){
            db.collection("users").document(userUid)
                    .update("gender",gender)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Gender updated successfully.");
                    })
                    .addOnFailureListener(e->{
                        Log.w(TAG, "Error updating Gender", e);
                        if (!hasError[0]) {
                            hasError[0] = true;
                            listener.onFailure("Failed to update gender: " + e.getMessage());
                        }
                    });
        }
    }


}


