package com.example.turgo;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class UserFirebase implements Serializable{
    private String userType;
    private String ID;
    private String fullName;
    private String nickname;
    private String birthdate;
    private int age;
    private String email;
    private String gender;
    private String phoneNumber;
    private String languangeID;
    private String theme;
    private ArrayList<String> inboxIDs;
    private ArrayList<String> outboxIDs;
    private ArrayList<String> notificationIDs;
    public UserFirebase(String userType){
        this.userType = userType;
    }

    public ArrayList<String> getInboxIDs() {
        return inboxIDs;
    }

    public void setInboxIDs(ArrayList<String> inboxIDs) {
        this.inboxIDs = inboxIDs;
    }

    public ArrayList<String> getOutboxIDs() {
        return outboxIDs;
    }

    public void setOutboxIDs(ArrayList<String> outboxIDs) {
        this.outboxIDs = outboxIDs;
    }

    public ArrayList<String> getNotificationIDs() {
        return notificationIDs;
    }

    public void setNotificationIDs(ArrayList<String> notificationIDs) {
        this.notificationIDs = notificationIDs;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLanguangeID() {
        return languangeID;
    }

    public void setLanguangeID(String languangeID) {
        this.languangeID = languangeID;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
