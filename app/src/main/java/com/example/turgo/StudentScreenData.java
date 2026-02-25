package com.example.turgo;

import java.util.ArrayList;

/**
 * Lightweight data holder for the StudentActivity (StudentScreen).
 * Contains only what's needed for the top bar (name, mail/notif counts)
 * and basic navigation state.
 */
public class StudentScreenData {
    public String uid;
    public String userType;
    public String fullName;
    public String pfpCloudinary;
    
    /** IDs for the mail dropdown. Actual objects loaded lazily. */
    public ArrayList<String> inboxIds;
    public ArrayList<String> scheduleCompletedThisWeekIds;
    
    /** 
     * Consolidated notification IDs. Student record historically has 
     * 'notitficationIDs', 'notificationIDs', and 'notifications'.
     */
    public ArrayList<String> notificationIds;
    
    public String completionWeekKey;
    public double percentageCompleted;
    
    /** Total number of schedules, used to calculate percentage. */
    public int allSchedulesCount;

    public StudentScreenData() {
        inboxIds = new ArrayList<>();
        notificationIds = new ArrayList<>();
    }
}
