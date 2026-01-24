package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DTARepository {
    private static DTARepository instance;
    private DatabaseReference dtaRef;

    public static DTARepository getInstance(String dtaId) {
        if (instance == null) {
            instance = new DTARepository(dtaId);
        }
        return instance;
    }

    private DTARepository(String dtaId) {
        dtaRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.DTA.getPath())
                .child(dtaId);
    }

    public void save(DayTimeArrangement object) {
        try {
            DTAFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            dtaRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save DTA", e);
        }
    }


    public void delete() {
        dtaRef.removeValue();
    }

    public void updateDay(DayOfWeek newDay) {
        dtaRef.child("day").setValue(newDay);
    }

    public void updateStart(LocalTime newStart) {
        dtaRef.child("start").setValue(newStart);
    }

    public void updateEnd(LocalTime newEnd) {
        dtaRef.child("end").setValue(newEnd);
    }

    /**
     * Adds a Schedule to this DTA.
     * Saves the full Schedule object to its Firebase path and stores only the ID reference here.
     */
    public void addOccupied(Schedule item) {
        try {
            ScheduleFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            dtaRef.child("occupied").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add occupied Schedule", e);
        }
    }

    public void removeOccupied(String scheduleId) {
        dtaRef.child("occupied").child(scheduleId).removeValue();
    }

    public void removeOccupiedCompletely(Schedule item) {
        dtaRef.child("occupied").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updateMaxMeeting(int newMaxMeeting) {
        dtaRef.child("maxMeeting").setValue(newMaxMeeting);
    }

    public void incrementMaxMeeting(int amount) {
        dtaRef.child("maxMeeting").setValue(ServerValue.increment(amount));
    }

    public void decrementMaxMeeting(int amount) {
        dtaRef.child("maxMeeting").setValue(ServerValue.increment(-amount));
    }

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        dtaRef.updateChildren(childUpdates);
    }
}
