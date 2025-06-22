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

        if (user != null) {
            // User is signed in, get their UID
            String userUid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            //update email

            if(email != null && !email.isEmpty()) {
                eUser.updateEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User email address updated.");
                                    Toast.makeText(null, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                                } else {
                                    // Check if it failed because recent login is required
                                    if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                        Log.w(TAG, "User must re-authenticate before changing email.");
                                        // *** IMPORTANT: Handle this error! ***
                                        // You need to prompt the user to sign in again,
                                        // then call user.reauthenticateWithCredential(),
                                        // and *then* call updateEmail() again.
                                        // Example: navigate to a re-authentication screen.

                                    } else {
                                        Log.w(TAG, "Failed to update user email.", task.getException());
                                        // Handle other errors (e.g., email already in use, invalid email)
                                    }
                                }
                            }

            });
                db.collection("users")
                        .document(userUid)
                        .update("email", email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "User name updated successfully.");
                                Toast.makeText(null, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                listener.onUserDataLoaded(name, email, bloodType,city);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating user name", e);
                                listener.onUserDataLoadFailed("Failed to update user name: " + e.getMessage());
                                Toast.makeText(null, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        });

        } else {
            // No user is signed in
            Log.d(TAG, "No user signed in. Cannot update email.");
            // Maybe prompt the user to sign in
        }



            //Update User Name
            if(name != null && !name.isEmpty()) {
             db.collection("users")
                        .document(userUid)
                        .update("name", name)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "User name updated successfully.");
                                Toast.makeText(null, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                listener.onUserDataLoaded(name, email, bloodType,city);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating user name", e);
                                listener.onUserDataLoadFailed("Failed to update user name: " + e.getMessage());
                                Toast.makeText(null, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            //Update City
            if(city != null && !city.isEmpty()) {
                db.collection("users")
                        .document(userUid)
                        .update("city", city)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "City updated successfully.");
                                Toast.makeText(null, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating city", e);
                                listener.onUserDataLoadFailed("Failed to update city: " + e.getMessage());
                                Toast.makeText(null, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            //Update Blood Type
            if(bloodType != null && !bloodType.isEmpty()) {
                db.collection("users")
                        .document(userUid)
                        .update("bloodType", bloodType)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Blood type updated successfully.");
                                Toast.makeText(null, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating blood type", e);
                                listener.onUserDataLoadFailed("Failed to update blood type: " + e.getMessage());
                                Toast.makeText(null, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            }






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

