package com.example.turgo;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class MeetingFirebase implements FirebaseClass<Meeting> {
    private String meetingID;
    private String scheduleID;
    private String prescheduledByID;
    private String dateOfMeeting; // Format: "yyyy-MM-dd"
    private String startTimeChange; // Optional
    private String endTimeChange;   // Optional
    private String roomChangeID;
    private boolean completed;
    private Map<String, String> studentsAttendedTime; // studentID -> time string

    public MeetingFirebase(String meetingID, String scheduleID, String prescheduledByID, String dateOfMeeting, String startTimeChange, String endTimeChange, String roomChangeID, boolean completed, Map<String, String> studentsAttendedTime) {
        this.meetingID = meetingID;
        this.scheduleID = scheduleID;
        this.prescheduledByID = prescheduledByID;
        this.dateOfMeeting = dateOfMeeting;
        this.startTimeChange = startTimeChange;
        this.endTimeChange = endTimeChange;
        this.roomChangeID = roomChangeID;
        this.completed = completed;
        this.studentsAttendedTime = studentsAttendedTime;
    }


    @Override
    public void importObjectData(Meeting from) {
        this.meetingID = from.getMeetingID();
        this.scheduleID = from.getMeetingOfSchedule().getID();
        this.prescheduledByID = from.getPreScheduledBy().getID();
        this.dateOfMeeting = from.getDateOfMeeting().toString(); // "yyyy-MM-dd"

        this.startTimeChange = from.getStartTimeChange().toString(); // "HH:mm"
        this.endTimeChange = from.getEndTimeChange().toString();     // "HH:mm"

        this.roomChangeID = from.getRoomChange().getID();
        this.completed = from.isCompleted();

        this.studentsAttendedTime = new HashMap<>();
        for (Map.Entry<Student, LocalTime> entry : from.getStudentsAttended().entrySet()) {
            this.studentsAttendedTime.put(entry.getKey().getID(), entry.getValue().toString()); // "HH:mm"
        }
    }

    @Override
    public String getID() {
        return meetingID;
    }
}

