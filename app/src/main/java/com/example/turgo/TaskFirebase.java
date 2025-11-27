package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskFirebase implements FirebaseClass<Task> {
    private String taskID;
    private List<String> studentsAssigned;  // Stores Student IDs
    private String title;
    private String description;
    private String dueDate;                // ISO-8601 string representation
    private String taskOfCourse;           // Course ID
    private String fromSchedule;           // Schedule ID
    private String dateAssigned;           // ISO-8601 string representation
    private String dropbox;                // Dropbox ID
    private String publisher;              // Teacher ID

    @Override
    public void importObjectData(Task from) {
        if (from == null) return;

        // Convert list of Student objects to list of IDs
        if (from.getStudentsAssigned() != null) {
            this.studentsAssigned = convertToIdList(from.getStudentsAssigned());
        }

        // Simple String fields
        this.title = from.getTitle();
        this.description = from.getDescription();

        // Date/time conversions (assuming ISO format)
        if (from.getDueDate() != null) {
            this.dueDate = from.getDueDate().toString();
        }
        if (from.getDateAssigned() != null) {
            this.dateAssigned = from.getDateAssigned().toString();
        }

        // Object-to-ID conversions
        this.taskOfCourse = (from.getTaskOfCourse() != null) ?
                from.getTaskOfCourse().getID() : null;
        this.fromSchedule = (from.getFromSchedule() != null) ?
                from.getFromSchedule().getID() : null;
        this.dropbox = (from.getDropbox() != null) ?
                from.getDropbox().getID() : null;
        this.publisher = (from.getPublisher() != null) ?
                from.getPublisher().getID() : null;
    }

    @Override
    public String getID() {
        // Assuming TaskFirebase gets its ID from somewhere
        // You might want to add an ID field if needed
        return taskID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Task> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Task.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Task) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }


    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public List<String> getStudentsAssigned() {
        return studentsAssigned;
    }

    public void setStudentsAssigned(List<String> studentsAssigned) {
        this.studentsAssigned = studentsAssigned;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getTaskOfCourse() {
        return taskOfCourse;
    }

    public void setTaskOfCourse(String taskOfCourse) {
        this.taskOfCourse = taskOfCourse;
    }

    public String getFromSchedule() {
        return fromSchedule;
    }

    public void setFromSchedule(String fromSchedule) {
        this.fromSchedule = fromSchedule;
    }

    public String getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(String dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    public String getDropbox() {
        return dropbox;
    }

    public void setDropbox(String dropbox) {
        this.dropbox = dropbox;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}