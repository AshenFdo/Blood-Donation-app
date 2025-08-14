package com.ashen.bdonorapp.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.LinkedList;

public class User {
    private String email;
    private String userName;
    private String password;
    private String bloodType;
    private String uid; // The user's unique ID from Firebase Authentication
    private String city;
    private String profileImageUrl;

    private Date lastDonationDate;

    private LinkedList<User> friendsList = new LinkedList<>();


    public User(String email, String userName, String bloodType,String uID) {
        this.email = email;
        this.userName = userName;
        this.bloodType = bloodType;
        this.uid = uID;
    }

    // Default constructor for Firestore
    public User(){}

    // Getters and Setters for all fields...
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return userName; }
    public void setName(String name) { this.userName = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public Date getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(Date lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public LinkedList<User> setAddFriend(User friend) {
        if (friend != null && !friendsList.contains(friend)) {
            friendsList.add(friend);
        }
        return friendsList;
    }
    public LinkedList<User> getFriendsList() {
        return friendsList;
    }



}