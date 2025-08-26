package com.ashen.bdonorapp.Models;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;




    public class BloodRequest {
        private String userName;
        private String postedByUserId;
        private String acceptedByUserId;
        private String title;
        private String userCity;
        private String bloodType;
        private String description;
        private String urgentType; // e.g., "Urgent", "Normal"
        private String status;// Unique identifier for the request

        private Date acceptedAt;


        @ServerTimestamp // Automatically sets the timestamp when created
        private Date timestamp;// To link the request to a specific user

        public  BloodRequest(){}


        public BloodRequest(
                String userName,
                String userCity,
                String bloodType,
                String title,
                String postedByUserId,
                String description,
                String urgentType)
        {

            this.userName = userName;
            this.userCity = userCity;
            this.bloodType = bloodType;
            this.description = description;
            this.urgentType = urgentType;
            this.postedByUserId = postedByUserId;
            this.title = title;
            this.status = null;
            this.acceptedByUserId = null;
            this.acceptedAt = null;

        }

        // Getters and setters
        public String getBloodType() { return bloodType; }
        public void setBloodType(String bloodType) { this.bloodType = bloodType; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getUserCity() { return userCity; }
        public void setUserCity(String userCity) { this.userCity = userCity; }

        public String getUrgentType() { return urgentType; }

        public  String getDescription(){
            return description;
        }

        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
        public void setUrgentType(String urgentType) { this.urgentType = urgentType; }
        public String getPostedByUserId() { return postedByUserId; }
        public void setPostedByUserId(String postedByUserId) { this.postedByUserId = postedByUserId; }

        public String getAcceptedByUserId() { return acceptedByUserId; }
        public void setAcceptedByUserId(String acceptedByUserId) { this.acceptedByUserId = acceptedByUserId; }
    }

