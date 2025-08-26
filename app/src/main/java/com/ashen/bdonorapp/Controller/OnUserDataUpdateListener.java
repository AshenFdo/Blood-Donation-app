package com.ashen.bdonorapp.Controller;

public interface OnUserDataUpdateListener {
    /**
     * Called when user profile is successfully updated.
     *
     * @param message Success message
     */
    void onSuccess(String message);

    /**
     * Called when there is an error updating user profile.
     *
     * @param errorMessage Error message describing what went wrong
     */
    void onFailure(String errorMessage);
}
