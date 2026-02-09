package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class RoomRepository implements RepositoryClass<Room, RoomFirebase> {

    private DatabaseReference roomRef;

    public RoomRepository(String roomKey) {
        roomRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.ROOM.getPath())
                .child(roomKey);
    }

    @Override
    public DatabaseReference getDbReference() {
        return roomRef;
    }

    @Override
    public Class<RoomFirebase> getFbClass() {
        return RoomFirebase.class;
    }

    public void delete() {
        roomRef.removeValue();
    }

    // ------------------------
    // Primitive Field Updates
    // ------------------------

    /** Updates the human-readable room name (roomId field in model). */
    public void updateRoomName(String newRoomName) {
        roomRef.child("roomId").setValue(newRoomName);
    }

    public void updateUsed(boolean newUsed) {
        roomRef.child("used").setValue(newUsed);
    }

    // ------------------------
    // Suitable Course Types (store CourseType IDs only)
    // ------------------------

    public void addSuitableCourseType(CourseType courseType) {
        addStringToArray("suitableCourseType", courseType.getID());
    }

    // Removal should be done via value-based removal (not key-based)
    public void removeSuitableCourseType(String courseTypeId) {
        removeStringFromArray("suitableCourseType", courseTypeId);
    }

    // ------------------------
    // Currently Occupied By (Meeting reference by ID)
    // ------------------------

    public void updateCurrentlyOccupiedBy(Meeting meeting) {
        if (meeting == null) {
            roomRef.child("currentlyOccupiedBy").removeValue();
            return;
        }

        roomRef.child("currentlyOccupiedBy").setValue(meeting.getID());
    }

    public void clearCurrentlyOccupiedBy() {
        roomRef.child("currentlyOccupiedBy").removeValue();
    }

    // ------------------------
    // Schedules Using This Room (store Schedule IDs only)
    // ------------------------

    public void addSchedule(Schedule schedule) {
        addStringToArray("schedules", schedule.getID());
    }

    public void removeSchedule(String scheduleId) {
        removeStringFromArray("schedules", scheduleId);
    }

    public void removeScheduleCompletely(Schedule schedule) {
        removeStringFromArray("schedules", schedule.getID());
        FirebaseDatabase.getInstance()
                .getReference(schedule.getFirebaseNode().getPath())
                .child(schedule.getID())
                .removeValue();
    }

    // ------------------------
    // Atomic Multi-field Update
    // ------------------------

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        roomRef.updateChildren(childUpdates);
    }
}
