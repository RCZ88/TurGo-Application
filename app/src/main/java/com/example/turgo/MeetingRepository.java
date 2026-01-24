package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class MeetingRepository {
    private static MeetingRepository instance;
    private DatabaseReference meetingRef;

    public static MeetingRepository getInstance(String meetingId) {
        if (instance == null) {
            instance = new MeetingRepository(meetingId);
        }
        return instance;
    }

    private MeetingRepository(String meetingId) {
        meetingRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath())
                .child(meetingId);
    }

    public void save(Meeting object) {
        try {
            MeetingFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            meetingRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save Meeting", e);
        }
    }


    public void delete() {
        meetingRef.removeValue();
    }

    /**
     * Adds a Student to this Meeting.
     * Saves the full Student object to its Firebase path and stores only the ID reference here.
     */
    public void addStudentAttended(Student student, LocalTime attendedTime) {
        try {
            StudentFirebase firebaseObj = student.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(student);

            FirebaseDatabase.getInstance()
                    .getReference(student.getFirebaseNode().getPath())
                    .child(student.getID())
                    .setValue(firebaseObj);

            Map<String, Object> attendanceData = new HashMap<>();
            attendanceData.put("studentId", student.getID());
            attendanceData.put("attendedTime", attendedTime.toString());

            meetingRef.child("studentsAttended")
                    .child(student.getID())
                    .setValue(attendanceData);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add student attendance", e);
        }
    }

    public void removeStudentAttended(String studentId) {
        meetingRef.child("studentsAttended").child(studentId).removeValue();
    }

    public void removeStudentAttendedCompletely(Student student) {
        meetingRef.child("studentsAttended").child(student.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(student.getFirebaseNode().getPath())
                .child(student.getID())
                .removeValue();
    }

    /**
     * Updates the user who pre-scheduled this meeting.
     * Saves the full User object to its Firebase path and stores only the ID reference here.
     */


    public void updateDateOfMeeting(LocalDate newDate) {
        meetingRef.child("dateOfMeeting").setValue(newDate.toString());
    }

    public void updateStartTimeChange(LocalTime newStartTimeChange) {
        meetingRef.child("startTimeChange").setValue(newStartTimeChange.toString());
    }

    public void updateEndTimeChange(LocalTime newEndTimeChange) {
        meetingRef.child("endTimeChange").setValue(newEndTimeChange.toString());
    }

    /**
     * Updates the room for this meeting.
     * Saves the full Room object to its Firebase path and stores only the ID reference here.
     */
    public void updateRoomChange(Room room) {
        try {
            RoomFirebase firebaseObj = room.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(room);

            FirebaseDatabase.getInstance()
                    .getReference(room.getFirebaseNode().getPath())
                    .child(room.getID())
                    .setValue(firebaseObj);

            meetingRef.child("roomChange").setValue(room.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to update roomChange", e);
        }
    }

    public void updateCompleted(boolean newCompleted) {
        meetingRef.child("completed").setValue(newCompleted);
    }

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        meetingRef.updateChildren(childUpdates);
    }
}
