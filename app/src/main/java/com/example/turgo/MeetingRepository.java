package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class MeetingRepository implements RepositoryClass<Meeting, MeetingFirebase> {
    private DatabaseReference meetingRef;

    public MeetingRepository(String meetingId) {
        meetingRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath())
                .child(meetingId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return meetingRef;
    }

    @Override
    public Class<MeetingFirebase> getFbClass() {
        return MeetingFirebase.class;
    }

    public void delete() {
        meetingRef.removeValue();
    }

    /**
     * Adds a student to this Meeting's studentsAttended array.
     * Stores attendance as a map object inside the array.
     */
    public void addStudentAttended(Student student, LocalTime attendedTime) {
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("studentId", student.getID());
        attendanceData.put("attendedTime", attendedTime.toString());

        meetingRef.child("studentsAttended").push().setValue(attendanceData);
    }

    /**
     * Removes a student from the studentsAttended array by filtering.
     */
    public void removeStudentAttended(String studentId) {
        meetingRef.child("studentsAttended").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                java.util.ArrayList<Map<String, Object>> newList = new java.util.ArrayList<>();

                if (snapshot.exists()) {
                    for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                        Map<String, Object> entry = (Map<String, Object>) child.getValue();
                        if (entry != null && !studentId.equals(entry.get("studentId"))) {
                            newList.add(entry);
                        }
                    }
                }

                meetingRef.child("studentsAttended").setValue(newList);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                android.util.Log.e("MeetingRepository", "Failed to remove student attendance", error.toException());
            }
        });
    }

    /**
     * Removes a student from attendance AND deletes the student object.
     */
    public void removeStudentAttendedCompletely(Student student) {
        removeStudentAttended(student.getID());
        FirebaseDatabase.getInstance()
                .getReference(student.getFirebaseNode().getPath())
                .child(student.getID())
                .removeValue();
    }

    public void updateAlarmAssigned(boolean newAlarmAssigned) {
        meetingRef.child("alarmAssigned").setValue(newAlarmAssigned);
    }

    public void updateAlarmAssignedAt(LocalDateTime alarmTime) {
        meetingRef.child("alarmAssignedAt")
                .setValue(alarmTime == null ? null : alarmTime.toString());
    }

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
     * Updates the room ID for this meeting.
     */
    public void updateRoomChange(Room room) {
        meetingRef.child("roomChange").setValue(room.getID());
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

    /* =========================
       Relationship Methods
       ========================= */

    public Task<Schedule> getSchedule() {
        TaskCompletionSource<Schedule> taskSource = new TaskCompletionSource<>();

        // First get the MeetingFirebase object to get the schedule ID
        meetingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    MeetingFirebase meetingFirebase = snapshot.getValue(MeetingFirebase.class);
                    if (meetingFirebase != null && meetingFirebase.getOfSchedule() != null) {
                        // Load the schedule using the schedule ID
                        ScheduleRepository scheduleRepository = new ScheduleRepository(meetingFirebase.getOfSchedule());
                        scheduleRepository.loadAsNormal().addOnSuccessListener(taskSource::setResult).addOnFailureListener(taskSource::setException);
                    } else {
                        taskSource.setResult(null);
                    }
                } else {
                    taskSource.setResult(null);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                taskSource.setException(error.toException());
            }
        });

        return taskSource.getTask();
    }
}
