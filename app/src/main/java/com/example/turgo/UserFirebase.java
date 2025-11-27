package com.example.turgo;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class UserFirebase implements Serializable{
    private String userType;
    private String uid;
    private String fullName;
    private String nickname;
    private String birthDate;
    private int age;
    private String email;
    private String gender;
    private String phoneNumber;
    private String language;
    private String theme;
    private ArrayList<String> inbox;
    private ArrayList<String> outbox;
    private ArrayList<String> draftMails;
    private ArrayList<String> notifications;
    public UserFirebase(String userType){
        this.userType = userType;
    }
    public UserFirebase(){};

    public ArrayList<String> getInbox() {
        return inbox;
    }

    public void setInbox(ArrayList<String> inbox) {
        this.inbox = inbox;
    }

    public ArrayList<String> getOutbox() {
        return outbox;
    }

    public void setOutbox(ArrayList<String> outbox) {
        this.outbox = outbox;
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<String> notifications) {
        this.notifications = notifications;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public String getID() {
        return uid;
    }

    public void setID(String ID) {
        this.uid = ID;
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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getDraftMails() {
        return draftMails;
    }

    public void setDraftMails(ArrayList<String> draftMails) {
        this.draftMails = draftMails;
    }
}
