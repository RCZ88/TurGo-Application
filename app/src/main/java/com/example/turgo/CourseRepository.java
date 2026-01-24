package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class CourseRepository {
    private static CourseRepository instance;
    private DatabaseReference courseRef;

    public static CourseRepository getInstance(String courseId) {
        if (instance == null) {
            instance = new CourseRepository(courseId);
        }
        return instance;
    }

    private CourseRepository(String courseId) {
        courseRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.COURSE.getPath())
                .child(courseId);
    }

    public void save(Course object) {
        try {
            CourseFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            courseRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save Course", e);
        }
    }
    public void delete() {
        courseRef.removeValue();
    }

    public void updateLogoCloudinary(String newLogoCloudinary) {
        courseRef.child("logoCloudinary").setValue(newLogoCloudinary);
    }

    public void updateBackgroundCloudinary(String newBackgroundCloudinary) {
        courseRef.child("backgroundCloudinary").setValue(newBackgroundCloudinary);
    }

    public void updateCourseName(String newCourseName) {
        courseRef.child("courseName").setValue(newCourseName);
    }

    public void updateCourseDescription(String newCourseDescription) {
        courseRef.child("courseDescription").setValue(newCourseDescription);
    }

    public void updateMaxStudentPerMeeting(int newMaxStudentPerMeeting) {
        courseRef.child("maxStudentPerMeeting").setValue(newMaxStudentPerMeeting);
    }

    public void incrementMaxStudentPerMeeting(int amount) {
        courseRef.child("maxStudentPerMeeting").setValue(ServerValue.increment(amount));
    }

    public void decrementMaxStudentPerMeeting(int amount) {
        courseRef.child("maxStudentPerMeeting").setValue(ServerValue.increment(-amount));
    }

    public void updateBaseCost(double newBaseCost) {
        courseRef.child("baseCost").setValue(newBaseCost);
    }

    public void updateHourlyCost(double newHourlyCost) {
        courseRef.child("hourlyCost").setValue(newHourlyCost);
    }

    public void updateMonthlyDiscountPercentage(double newMonthlyDiscountPercentage) {
        courseRef.child("monthlyDiscountPercentage").setValue(newMonthlyDiscountPercentage);
    }

    public void updateAutoAcceptStudent(boolean newAutoAcceptStudent) {
        courseRef.child("autoAcceptStudent").setValue(newAutoAcceptStudent);
    }

    public void addImagesCloudinary(String item) {
        courseRef.child("imagesCloudinary").push().setValue(item);
    }

    public void removeImagesCloudinary(String itemId) {
        courseRef.child("imagesCloudinary").child(itemId).removeValue();
    }

    public void addPaymentPer(boolean item) {
        courseRef.child("paymentPer").push().setValue(item);
    }

    public void updatePaymentPerAtIndex(int index, boolean value) {
        courseRef.child("paymentPer").child(String.valueOf(index)).setValue(value);
    }

    public void addPrivateGroup(boolean item) {
        courseRef.child("privateGroup").push().setValue(item);
    }

    public void updatePrivateGroupAtIndex(int index, boolean value) {
        courseRef.child("privateGroup").child(String.valueOf(index)).setValue(value);
    }

    /**
     * Adds a DayTimeArrangement to this Course.
     * Saves the full DayTimeArrangement object to its Firebase path and stores only the ID reference here.
     */
    public void addDayTimeArrangement(DayTimeArrangement item) {
        try {
            DTAFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            courseRef.child("dayTimeArrangement").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add DayTimeArrangement", e);
        }
    }

    public void removeDayTimeArrangement(String itemId) {
        courseRef.child("dayTimeArrangement").child(itemId).removeValue();
    }

    public void removeDayTimeArrangementCompletely(DayTimeArrangement item) {
        courseRef.child("dayTimeArrangement").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Schedule to this Course.
     * Saves the full Schedule object to its Firebase path and stores only the ID reference here.
     */
    public void addSchedule(Schedule item) {
        try {
            ScheduleFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            courseRef.child("schedules").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add Schedule", e);
        }
    }

    public void removeSchedule(String itemId) {
        courseRef.child("schedules").child(itemId).removeValue();
    }

    public void removeScheduleCompletely(Schedule item) {
        courseRef.child("schedules").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds an Agenda to this Course.
     * Saves the full Agenda object to its Firebase path and stores only the ID reference here.
     */
    public void addAgenda(Agenda item) {
        try {
            AgendaFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            courseRef.child("agendas").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add Agenda", e);
        }
    }

    public void removeAgenda(String itemId) {
        courseRef.child("agendas").child(itemId).removeValue();
    }

    public void removeAgendaCompletely(Agenda item) {
        courseRef.child("agendas").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a StudentCourse to this Course.
     * Saves the full StudentCourse object to its Firebase path and stores only the ID reference here.
     */
    public void addStudentCourse(StudentCourse item) {
        try {
            StudentCourseFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            courseRef.child("studentsCourse").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add StudentCourse", e);
        }
    }

    public void removeStudentCourse(String itemId) {
        courseRef.child("studentsCourse").child(itemId).removeValue();
    }

    public void removeStudentCourseCompletely(StudentCourse item) {
        courseRef.child("studentsCourse").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Updates multiple fields atomically with a lastModified timestamp.
     */
    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        courseRef.updateChildren(childUpdates);
    }
}
