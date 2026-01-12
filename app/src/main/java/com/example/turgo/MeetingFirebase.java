package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class MeetingFirebase implements FirebaseClass<Meeting> {
    private String meetingID;
    private String preScheduledBy;
    private String dateOfMeeting; // Format: "yyyy-MM-dd"
    private String startTimeChange; // Optional
    private String endTimeChange;   // Optional
    private String roomChange;
    private boolean completed;
    private Map<String, String> studentsAttended; // studentID -> time string

    public MeetingFirebase(String meetingID, String meetingOfSchedule, String preScheduledBy, String dateOfMeeting, String startTimeChange, String endTimeChange, String roomChange, boolean completed, Map<String, String> studentsAttended) {
        this.meetingID = meetingID;
        this.preScheduledBy = preScheduledBy;
        this.dateOfMeeting = dateOfMeeting;
        this.startTimeChange = startTimeChange;
        this.endTimeChange = endTimeChange;
        this.roomChange = roomChange;
        this.completed = completed;
        this.studentsAttended = studentsAttended;
    }


    @Override
    public void importObjectData(Meeting from) {
        this.meetingID = from.getMeetingID();
        // Use callback for getMeetingOfSchedule
        
        this.preScheduledBy = from.getPreScheduledBy().getUid();
        this.dateOfMeeting = from.getDateOfMeeting().toString(); // "yyyy-MM-dd"

        this.startTimeChange = from.getStartTimeChange().toString(); // "HH:mm"
        this.endTimeChange = from.getEndTimeChange().toString();     // "HH:mm"

        this.roomChange = from.getRoomChange().getID();
        this.completed = from.isCompleted();

        this.studentsAttended = new HashMap<>();
        for (Map.Entry<Student, LocalTime> entry : from.getStudentsAttended().entrySet()) {
            this.studentsAttended.put(entry.getKey().getID(), entry.getValue().toString()); // "HH:mm"
        }
    }

    @Override
    public String getID() {
        return meetingID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Meeting> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Mail.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Meeting) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }
}

