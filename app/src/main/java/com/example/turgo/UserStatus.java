package com.example.turgo;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;

public class UserStatus implements RequireUpdate<UserStatus, UserStatus>, FirebaseClass<UserStatus>{
    private String userID;
    private boolean online;
    private LocalDateTime lastSeen;

    public UserStatus(String userID, boolean online, LocalDateTime lastSeen) {
        this.userID = userID;
        this.online = online;
        this.lastSeen = lastSeen;
    }

    public UserStatus(String userID) {
        this.userID = userID;
    }
    public UserStatus(){

    }

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

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }


    @Override
    public void importObjectData(UserStatus from) {

    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.USER_STATUS;
    }

    @Override
    public Class<UserStatus> getFirebaseClass() {
        return UserStatus.class;
    }

    @Override
    public String getID() {
        return this.userID;
    }
    @NonNull
    public String toString(){
        if( online){
            return "Online";
        }else{
            return "Offline - Last seen: " + lastSeen;
        }
    }
}
