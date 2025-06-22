package com.ashen.bdonorapp.Controller;

public interface OnUserDataLoadedListener {

    /**
     * Called when user data is successfully loaded.
     *
     * @param userName The user's display name from Firebase Auth. Can be null.
     * @param userEmail The user's email from Firebase Auth. Can be null.
     * @param bloodType The user's blood type from Firestore. Can be null if not set.
     * @param city The user's blood type from Firestore. Can be null if not set.
     */
    void onUserDataLoaded(String userName, String userEmail, String bloodType, String city);

    /**
     * Called when there is an error loading user data, or if no user is signed in.
     *
     * @param errorMessage A description of the error.
     */
    void onUserDataLoadFailed(String errorMessage);

}
