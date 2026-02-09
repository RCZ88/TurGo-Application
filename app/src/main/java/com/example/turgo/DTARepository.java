package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DTARepository implements RepositoryClass<DayTimeArrangement, DTAFirebase> {
    private DatabaseReference dtaRef;

    public DTARepository(String dtaId) {
        dtaRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.DTA.getPath())
                .child(dtaId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return dtaRef;
    }

    @Override
    public Class<DTAFirebase> getFbClass() {
        return DTAFirebase.class;
    }

    public void delete() {
        dtaRef.removeValue();
    }

    public void updateDay(DayOfWeek newDay) {
        dtaRef.child("day").setValue(newDay == null ? null : newDay.toString());
    }

    public void updateStart(LocalTime newStart) {
        dtaRef.child("start").setValue(newStart == null ? null : newStart.toString());
    }

    public void updateEnd(LocalTime newEnd) {
        dtaRef.child("end").setValue(newEnd == null ? null : newEnd.toString());
    }

    /**
     * Adds a Schedule ID reference to this DTA's occupied array.
     */
    public void addOccupied(Schedule item) {
        addStringToArray("occupied", item.getID());
    }

    public void removeOccupied(String scheduleId) {
        removeStringFromArray("occupied", scheduleId);
    }

    /**
     * Removes the Schedule reference from this DTA and deletes the Schedule object.
     */
    public void removeOccupiedCompletely(Schedule item) {
        removeStringFromArray("occupied", item.getID());
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
