package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class CourseRepository implements RepositoryClass<Course, CourseFirebase>{
    private DatabaseReference courseRef;

    public CourseRepository(String courseId) {
        courseRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.COURSE.getPath())
                .child(courseId);
    }
    @Override
    public DatabaseReference getDbReference() {

        return courseRef;
    }

    @Override
    public Class<CourseFirebase> getFbClass() {
        return CourseFirebase.class;
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
        addStringToArray("imagesCloudinary", item);
    }

    public void removeImagesCloudinary(String itemId) {
        removeStringFromArray("imagesCloudinary", itemId);
    }
    public void updatePaymentPerAtIndex(int index, boolean value) {
        courseRef.child("paymentPer").child(String.valueOf(index)).setValue(value);
    }

    public void updatePrivateGroupAtIndex(int index, boolean value) {
        courseRef.child("privateGroup").child(String.valueOf(index)).setValue(value);
    }

    /**
     * Adds a DayTimeArrangement ID reference to this Course.
     */
    public void addDayTimeArrangement(DayTimeArrangement item) {
        addStringToArray("dayTimeArrangement", item.getID());
    }

    public void removeDayTimeArrangement(String itemId) {
        removeStringFromArray("dayTimeArrangement", itemId);
    }

    public void removeDayTimeArrangementCompletely(DayTimeArrangement item) {
        courseRef.child("dayTimeArrangement").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a Student ID reference to this Course.
     */
    public void addStudent(Student student) {
        addStringToArray("students", student.getID());
    }

    /**
     * Removes a Student ID reference from this Course.
     */
    public void removeStudent(String studentId) {
        removeStringFromArray("students", studentId);
    }


    /**
     * Adds a Schedule ID reference to this Course.
     */
    public void addSchedule(Schedule item) {
        addStringToArray("schedules", item.scheduleID);
    }

    public void removeSchedule(String itemId) {
        removeStringFromArray("schedules", itemId);
    }

    public void removeScheduleCompletely(Schedule item) {
        courseRef.child("schedules").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds an Agenda ID reference to this Course.
     */
    public void addAgenda(Agenda item) {
        addStringToArray("agendas", item.getID());
    }

    public void removeAgenda(String itemId) {
        removeStringFromArray("agendas", itemId);
    }

    public void removeAgendaCompletely(Agenda item) {
        courseRef.child("agendas").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    /**
     * Adds a StudentCourse ID reference to this Course.
     */
    public void addStudentCourse(StudentCourse item) {
        addStringToArray("studentsCourse", item.getID());
    }

    public void removeStudentCourse(String itemId) {
        removeStringFromArray("studentsCourse", itemId);
    }

    public void removeStudentCourseCompletely(StudentCourse item) {
        courseRef.child("studentsCourse").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public com.google.android.gms.tasks.Task<Course> loadLite() {
        return loadFields("courseName", "logoCloudinary", "backgroundCloudinary", "teacher", "courseDescription")
            .continueWith(task -> {
                java.util.Map<String, Object> m = task.getResult();
                Course c = new Course();
                c.setCourseID(courseRef.getKey());
                c.setCourseName((String) m.get("courseName"));
                c.setLogo((String) m.get("logoCloudinary"));
                c.setBackgroundCloudinary((String) m.get("backgroundCloudinary"));
                c.setTeacher((String) m.get("teacher"));
                c.setCourseDescription((String) m.get("courseDescription"));
                return c;
            });
    }

    /**
     * Updates multiple fields atomically with a lastModified timestamp.
     * Only allows primitive-safe values and strings.
     */
    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String ||
                    value instanceof Integer ||
                    value instanceof Double ||
                    value instanceof Boolean ||
                    value instanceof Long) {

                childUpdates.put(entry.getKey(), value);

            } else {
                throw new IllegalArgumentException(
                        "Invalid value type for Firebase: " + value.getClass().getSimpleName()
                );
            }
        }

        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        courseRef.updateChildren(childUpdates);
    }
}
