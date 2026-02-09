package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ScheduleRepository implements RepositoryClass<Schedule, ScheduleFirebase>{

    private DatabaseReference scheduleRef;


    public ScheduleRepository(String scheduleId) {
        scheduleRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.SCHEDULE.getPath())
                .child(scheduleId);
    }

    /* =========================
       Core CRUD
       ========================= */


    public void delete(Schedule object) {
        scheduleRef.child(object.getID()).removeValue();
    }

    @Override
    public DatabaseReference getDbReference() {
        return scheduleRef;
    }

    @Override
    public Class<ScheduleFirebase> getFbClass() {
        return ScheduleFirebase.class;
    }

    /* =========================
       Primitive / Boolean Fields
       ========================= */

    public void updateNumberOfStudents(int newNumberOfStudents) {
        scheduleRef.child("numberOfStudents").setValue(newNumberOfStudents);
    }

    public void incrementNumberOfStudents(int amount) {
        scheduleRef.child("numberOfStudents").setValue(ServerValue.increment(amount));
    }

    public void decrementNumberOfStudents(int amount) {
        scheduleRef.child("numberOfStudents").setValue(ServerValue.increment(-amount));
    }

    public void updateIsPrivate(boolean newIsPrivate) {
        scheduleRef.child("isPrivate").setValue(newIsPrivate);
    }

    public void updateHasScheduled(boolean newHasScheduled) {
        scheduleRef.child("hasScheduled").setValue(newHasScheduled);
    }

    public void updateDuration(int newDuration) {
        scheduleRef.child("duration").setValue(newDuration);
    }

    /* =========================
       Time / Date Fields
       ========================= */

    public void updateMeetingStart(LocalTime newMeetingStart) {
        scheduleRef.child("meetingStart").setValue(newMeetingStart.toString());
    }

    public void updateMeetingEnd(LocalTime newMeetingEnd) {
        scheduleRef.child("meetingEnd").setValue(newMeetingEnd.toString());
    }

    public void updateDay(DayOfWeek newDay) {
        scheduleRef.child("day").setValue(newDay.toString());
    }

    /* =========================
       Custom Object: User scheduler
       ========================= */

    /**
     * Sets the scheduler for this Schedule.
     * Saves the full User object to its Firebase path and stores only the ID reference here.
     */
    public <U extends RequireUpdate> void setScheduler(U user) throws IllegalAccessException, InstantiationException {
        UserFirebase userFirebase = null;

        if(user instanceof Student){
            StudentFirebase studentFirebase = ((Student)user).getFirebaseClass().newInstance();
            studentFirebase.importObjectData((Student)user);
            userFirebase = studentFirebase;
        }else if(user instanceof Teacher){
            TeacherFirebase teacherFirebase = ((Teacher)user).getFirebaseClass().newInstance();
            teacherFirebase.importObjectData((Teacher)user);
            userFirebase = teacherFirebase;
        }

        if(userFirebase == null){
            return;
        }
        FirebaseDatabase.getInstance()
                .getReference(user.getFirebaseNode().getPath())
                .child(user.getID())
                .setValue(userFirebase);

        scheduleRef.child("scheduler").setValue(user.getID());
    }

    public void removeScheduler() {
        scheduleRef.child("scheduler").removeValue();
    }

    /* =========================
       Batch Update
       ========================= */

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        scheduleRef.updateChildren(childUpdates);
    }
}
