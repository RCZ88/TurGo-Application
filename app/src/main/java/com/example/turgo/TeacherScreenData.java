package com.example.turgo;

import java.util.ArrayList;

/**
 * Lightweight data holder for the TeacherActivity (TeacherScreen).
 */
public class TeacherScreenData {
    public String uid;
    public String userType;
    public String fullName;
    public String pfpCloudinary;
    
    /** IDs for the mail dropdown. */
    public ArrayList<String> inboxIds;
    
    /** Consolidated notification IDs. */
    public ArrayList<String> notificationIds;

    public TeacherScreenData() {
        inboxIds = new ArrayList<>();
        notificationIds = new ArrayList<>();
    }
}
