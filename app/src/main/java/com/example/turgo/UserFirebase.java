package com.example.turgo;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class UserFirebase implements Serializable {
    private String uid;
    private String userType;
    private String fullName;
    private int age;
    private String birthDate;
    private String nickname;
    private String email;
    private String gender;
    private String phoneNumber;
    private String language;
    private String theme;
    private ArrayList<String> inbox;
    private ArrayList<String> outbox;
    private ArrayList<String> drafts;
    private ArrayList<String> notifications;
    private ArrayList<String> inboxIds;
    private ArrayList<String> outboxIds;
    private ArrayList<String> draftsIds;
    private ArrayList<String> notificationsIds;
    private String pfpCloudinary;

    public UserFirebase(String userType) {
        this.userType = userType;
    }

    public UserFirebase() {}

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getID() {
        return uid;
    }

    public void setID(String ID) {
        this.uid = ID;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public ArrayList<String> getDrafts() {
        return drafts;
    }

    public void setDrafts(ArrayList<String> drafts) {
        this.drafts = drafts;
    }

    public ArrayList<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(ArrayList<String> notifications) {
        this.notifications = notifications;
    }

    public ArrayList<String> getInboxIds() {
        return inboxIds;
    }

    public void setInboxIds(ArrayList<String> inboxIds) {
        this.inboxIds = inboxIds;
    }

    public ArrayList<String> getOutboxIds() {
        return outboxIds;
    }

    public void setOutboxIds(ArrayList<String> outboxIds) {
        this.outboxIds = outboxIds;
    }

    public ArrayList<String> getDraftsIds() {
        return draftsIds;
    }

    public void setDraftsIds(ArrayList<String> draftsIds) {
        this.draftsIds = draftsIds;
    }

    public ArrayList<String> getNotificationsIds() {
        return notificationsIds;
    }

    public void setNotificationsIds(ArrayList<String> notificationsIds) {
        this.notificationsIds = notificationsIds;
    }

    public String getPfpCloudinary() {
        return pfpCloudinary;
    }

    public void setPfpCloudinary(String pfpCloudinary) {
        this.pfpCloudinary = pfpCloudinary;
    }
}
