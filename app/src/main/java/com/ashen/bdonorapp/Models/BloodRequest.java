package com.ashen.bdonorapp.Models;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;




    public class BloodRequest {
        private String userName;
        private String userCity;
        private String bloodType;
        private String description;
        private String urgentType; // e.g., "Urgent", "Normal"
        private String userId;
        private String status; // Unique identifier for the request


        @ServerTimestamp // Automatically sets the timestamp when created
        private Date timestamp;// To link the request to a specific user

        public  BloodRequest(){}


        public BloodRequest(String userName, String userCity, String bloodType, String description, String urgentType, String userId,String status) {
            this.userName = userName;
            this.userCity = userCity;
            this.bloodType = bloodType;
            this.description = description;
            this.urgentType = urgentType;
            this.userId = userId;
            this.status = status;
        }

        // Getters and Setters for all fields
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserCity() {
            return userCity;
        }

        public void setUserCity(String userCity) {
            this.userCity = userCity;
        }

        public String getBloodType() {
            return bloodType;
        }

        public void setBloodType(String bloodType) {
            this.bloodType = bloodType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUrgentType() {
            return urgentType;
        }

        public void setUrgentType(String urgentType) {
            this.urgentType = urgentType;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public  String getStatus() {
            return status;
        }

        public void setStatus(String status){
            this.status = status;

        }

    }

