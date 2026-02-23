package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class StudentCourseRepository implements RepositoryClass<StudentCourse, StudentCourseFirebase>{

    private DatabaseReference studentCourseRef;

    public StudentCourseRepository(String scId) {
        studentCourseRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT_COURSE.getPath())
                .child(scId);
    }

    /* =========================
       Core CRUD
       ========================= */

    @Override
    public DatabaseReference getDbReference() {
        return studentCourseRef;
    }

    @Override
    public Class<StudentCourseFirebase> getFbClass() {
        return StudentCourseFirebase.class;
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

    public void addSchedule(Schedule item) {
        addStringToArray("schedulesOfCourse", item.getID());
    }

    public void removeSchedule(String scheduleId) {
        removeStringFromArray("schedulesOfCourse", scheduleId);
    }

    public void removeScheduleCompletely(Schedule item) {
        removeStringFromArray("schedulesOfCourse", item.getID());

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /* =========================
       ArrayList<Task>
       ========================= */

    public void addTask(Task item) {
        addStringToArray("tasks", item.getID());
    }

    public void removeTask(String taskId) {
        removeStringFromArray("tasks", taskId);
    }

    public void removeTaskCompletely(Task item) {
        removeStringFromArray("tasks", item.getID());

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /* =========================
       ArrayList<Agenda>
       ========================= */

    public void addAgenda(Agenda item) {
        addStringToArray("agendas", item.getID());
    }

    public void removeAgenda(String agendaId) {
        removeStringFromArray("agendas", agendaId);
    }

    public void removeAgendaCompletely(Agenda item) {
        removeStringFromArray("agendas", item.getID());

        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /* =========================
       ArrayList<TimeSlot>
       ========================= */

    public void addTimeSlot(TimeSlot item) {
        addStringToArray("timeSlots", item.getID());
    }

    public void removeTimeSlot(String timeSlotId) {
        removeStringFromArray("timeSlots", timeSlotId);
    }

    public void removeTimeSlotCompletely(TimeSlot item) {
        removeStringFromArray("timeSlots", item.getID());

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
