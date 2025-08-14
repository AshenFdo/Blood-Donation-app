package com.ashen.bdonorapp.Controller;

// Interface for request operations callbacks
public interface RequestCallback {
    void onSuccess(String message);
    void onFailure(String errorMessage);
}