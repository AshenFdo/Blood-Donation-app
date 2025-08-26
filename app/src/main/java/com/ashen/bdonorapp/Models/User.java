package com.ashen.bdonorapp.Models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class User {
    private String email;
    private String userName;

    private String gender;
    private String password;
    private String bloodType;
    private String userId; // The user's unique ID from Firebase Authentication
    private String city;
    private String profileImage;


    private final ArrayList<String> friendsList = new ArrayList<>();


    public User(String email, String name, String bloodType,String uID, String profileImageUrl, String gender) {
        this.email = email;
        this.userName = name;
        this.bloodType = bloodType;
        this.userId = uID;
        this.profileImage = profileImageUrl;
        this.gender = gender;
    }

    // Default constructor for Firestore
    public User(){}

    // Getters and Setters for all fields...


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCity() {
        return city != null ? city : "Not specified";
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ArrayList<String> getFriendsList() {
        return friendsList;
    }
}