package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class StudentCourseRepository {

    private static StudentCourseRepository instance;
    private DatabaseReference studentCourseRef;

    public static StudentCourseRepository getInstance(String scId) {
        if (instance == null) {
            instance = new StudentCourseRepository(scId);
        }
        return instance;
    }

    private StudentCourseRepository(String scId) {
        studentCourseRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENTCOURSE.getPath())
                .child(scId);
    }

    /* =========================
       Core CRUD
       ========================= */

    public void save(StudentCourse object) throws IllegalAccessException, InstantiationException {
        StudentCourseFirebase firebaseObj = object.getFirebaseClass().newInstance();
        firebaseObj.importObjectData(object);
        studentCourseRef.setValue(firebaseObj);
    }

    public void delete() {
        studentCourseRef.removeValue();
    }

    /* =========================
       Primitive / Boolean Fields
       ========================= */

    public void updatePaymentPreferences(boolean newPaymentPreferences) {
        studentCourseRef.child("paymentPreferences").setValue(newPaymentPreferences);
    }

    public void updatePrivateOrGroup(boolean newPrivateOrGroup) {
        studentCourseRef.child("privateOrGroup").setValue(newPrivateOrGroup);
    }

    public void updatePricePer(int newPricePer) {
        studentCourseRef.child("pricePer").setValue(newPricePer);
    }

    public void incrementPricePer(int amount) {
        studentCourseRef.child("pricePer").setValue(ServerValue.increment(amount));
    }

    public void decrementPricePer(int amount) {
        studentCourseRef.child("pricePer").setValue(ServerValue.increment(-amount));
    }

    /* =========================
       ArrayList<Schedule>
       ========================= */

    /**
     * Adds a Schedule to this StudentCourse.
     * Saves the full Schedule object to its Firebase path and stores only the ID reference here.
     */
    public void addSchedule(Schedule item) throws IllegalAccessException, InstantiationException {
        ScheduleFirebase firebaseObj = item.getFirebaseClass().newInstance();
        firebaseObj.importObjectData(item);

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .setValue(firebaseObj);

        studentCourseRef.child("schedulesOfCourse").push().setValue(item.getID());
    }

    public void removeSchedule(String scheduleId) {
        studentCourseRef.child("schedulesOfCourse").child(scheduleId).removeValue();
    }

    public void removeScheduleCompletely(Schedule item) {
        studentCourseRef.child("schedulesOfCourse").child(item.getID()).removeValue();

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /* =========================
       ArrayList<Task>
       ========================= */

    /**
     * Adds a Task to this StudentCourse.
     * Saves the full Task object to its Firebase path and stores only the ID reference here.
     */
    public void addTask(Task item) throws IllegalAccessException, InstantiationException {
        TaskFirebase firebaseObj = item.getFirebaseClass().newInstance();
        firebaseObj.importObjectData(item);

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .setValue(firebaseObj);

        studentCourseRef.child("tasks").push().setValue(item.getID());
    }

    public void removeTask(String taskId) {
        studentCourseRef.child("tasks").child(taskId).removeValue();
    }

    public void removeTaskCompletely(Task item) {
        studentCourseRef.child("tasks").child(item.getID()).removeValue();

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /* =========================
       ArrayList<Agenda>
       ========================= */

    /**
     * Adds an Agenda to this StudentCourse.
     * Saves the full Agenda object to its Firebase path and stores only the ID reference here.
     */
    public void addAgenda(Agenda item) throws IllegalAccessException, InstantiationException {
        AgendaFirebase firebaseObj = item.getFirebaseClass().newInstance();
        firebaseObj.importObjectData(item);

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .setValue(firebaseObj);

        studentCourseRef.child("agendas").push().setValue(item.getID());
    }

    public void removeAgenda(String agendaId) {
        studentCourseRef.child("agendas").child(agendaId).removeValue();
    }

    public void removeAgendaCompletely(Agenda item) {
        studentCourseRef.child("agendas").child(item.getID()).removeValue();

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /* =========================
       ArrayList<TimeSlot>
       ========================= */

    /**
     * Adds a TimeSlot to this StudentCourse.
     * Saves the full TimeSlot object to its Firebase path and stores only the ID reference here.
     */
    public void addTimeSlot(TimeSlot item) throws IllegalAccessException, InstantiationException {
        TimeSlotFirebase firebaseObj = item.getFirebaseClass().newInstance();
        firebaseObj.importObjectData(item);

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .setValue(firebaseObj);

        studentCourseRef.child("timeSlots").push().setValue(item.getID());
    }

    public void removeTimeSlot(String timeSlotId) {
        studentCourseRef.child("timeSlots").child(timeSlotId).removeValue();
    }

    public void removeTimeSlotCompletely(TimeSlot item) {
        studentCourseRef.child("timeSlots").child(item.getID()).removeValue();

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
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
        studentCourseRef.updateChildren(childUpdates);
    }
}
