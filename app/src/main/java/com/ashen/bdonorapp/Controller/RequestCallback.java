package com.ashen.bdonorapp.Controller;

public interface RequestCallback {

    void onSuccess(String message);
    void onFailure(String errorMessage);
}
