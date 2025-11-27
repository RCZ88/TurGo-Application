package com.example.turgo;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class UserStatus implements Serializable {
    private String userID;
    private boolean online;
    private String lastSeen;

    // Default constructor required for Firebase
    public UserStatus() {
    }

    public UserStatus(String userID, boolean online, String lastSeen) {
        this.userID = userID;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public UserStatus(String userID) {
        this.userID = userID;
    }

    // Getters and Setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    @NonNull
    public String toString(){
        if(online){
            return "Online";
        } else {
            return "Offline - Last seen: " + lastSeen;
        }
    }
}