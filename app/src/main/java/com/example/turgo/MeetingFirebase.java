package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MeetingFirebase implements FirebaseClass<Meeting> {
    private String meetingID;
    private String preScheduledBy;
    private String preScheduledByType;
    private String dateOfMeeting; // Format: "yyyy-MM-dd"
    private String startTimeChange; // Optional
    private String endTimeChange;   // Optional
    private String roomChange;
    private boolean completed;
    private Map<String, String> studentsAttended; // studentID -> time string
    private ArrayList<String> usersRelated;
    private boolean alarmAssigned;
    private String alarmAssignedAt;
    private String ofSchedule;
    public MeetingFirebase(){}


    @Override
    public void importObjectData(Meeting from) {
        this.meetingID = from.getMeetingID();

        // ✅ Tool.boolOf() null-proof EVERYTHING
        this.preScheduledBy = Tool.boolOf(from.getPreScheduledBy()) ? from.getPreScheduledBy() : "";
        this.preScheduledByType = Tool.boolOf(from.getPreScheduledByType()) ? from.getPreScheduledByType() : "";
        this.dateOfMeeting = Tool.boolOf(from.getDateOfMeeting()) ? from.getDateOfMeeting().toString() : "";

        this.startTimeChange = Tool.boolOf(from.getStartTimeChange()) ? from.getStartTimeChange().toString() : "";
        this.endTimeChange = Tool.boolOf(from.getEndTimeChange()) ? from.getEndTimeChange().toString() : "";
        this.usersRelated = Tool.boolOf(from.getUsersRelated()) ? from.getUsersRelated() : new ArrayList<>();

        this.roomChange = Tool.boolOf(from.getRoomChange()) ? from.getRoomChange().getID() : "";
        this.completed = Tool.boolOf(from.isCompleted());

        // ✅ Safe Map copy
        this.studentsAttended = new HashMap<>();
        if (Tool.boolOf(from.getStudentsAttended())) {
            for (Map.Entry<Student, LocalTime> entry : from.getStudentsAttended().entrySet()) {
                String studentId = Tool.boolOf(entry.getKey()) ? entry.getKey().getID() : "";
                String attendTime = Tool.boolOf(entry.getValue()) ? entry.getValue().toString() : "";
                this.studentsAttended.put(studentId, attendTime);
            }
        }

        this.alarmAssigned = from.isAlarmAssigned();
        this.alarmAssignedAt = alarmAssigned ? from.getAlarmAssignedAt().toString() : "";

        this.ofSchedule = Tool.boolOf(from.getOfSchedule()) ? from.getOfSchedule() : "";
    }

    public ArrayList<String> getUsersRelated() {
        return usersRelated;
    }

    public void setUsersRelated(ArrayList<String> usersRelated) {
        this.usersRelated = usersRelated;
    }

    @Override
    public String getID() {
        return meetingID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Meeting> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Meeting.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Meeting) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getOfSchedule() {
        return ofSchedule;
    }

    public void setOfSchedule(String ofSchedule) {
        this.ofSchedule = ofSchedule;
    }

    public boolean isAlarmAssigned() {
        return alarmAssigned;
    }

    public void setAlarmAssigned(boolean alarmAssigned) {
        this.alarmAssigned = alarmAssigned;
    }

    public String getAlarmAssignedAt() {
        return alarmAssignedAt;
    }

    public void setAlarmAssignedAt(String alarmAssignedAt) {
        this.alarmAssignedAt = alarmAssignedAt;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public String getPreScheduledBy() {
        return preScheduledBy;
    }

    public void setPreScheduledBy(String preScheduledBy) {
        this.preScheduledBy = preScheduledBy;
    }

    public String getPreScheduledByType() {
        return preScheduledByType;
    }

    public void setPreScheduledByType(String preScheduledByType) {
        this.preScheduledByType = preScheduledByType;
    }

    public String getDateOfMeeting() {
        return dateOfMeeting;
    }

    public void setDateOfMeeting(String dateOfMeeting) {
        this.dateOfMeeting = dateOfMeeting;
    }

    public String getStartTimeChange() {
        return startTimeChange;
    }

    public void setStartTimeChange(String startTimeChange) {
        this.startTimeChange = startTimeChange;
    }

    public String getEndTimeChange() {
        return endTimeChange;
    }

    public void setEndTimeChange(String endTimeChange) {
        this.endTimeChange = endTimeChange;
    }

    public String getRoomChange() {
        return roomChange;
    }

    public void setRoomChange(String roomChange) {
        this.roomChange = roomChange;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Map<String, String> getStudentsAttended() {
        return studentsAttended;
    }

    public void setStudentsAttended(Map<String, String> studentsAttended) {
        this.studentsAttended = studentsAttended;
    }
}

