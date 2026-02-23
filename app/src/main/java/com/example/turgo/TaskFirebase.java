package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class TaskFirebase implements FirebaseClass<Task> {
    private String taskID;
    private ArrayList<String> studentsAssign;
    private String title;
    private String description;
    private String dueDate;
    private String taskOfCourse;
    private String fromSchedule;
    private String dateAssigned;
    private String dropbox;
    private boolean manualCompletionRequired;
    private String publisher;

    public TaskFirebase(){}

    @Override
    public void importObjectData(Task from) {
        if (from == null) return;
        this.taskID = from.getTaskID();
        this.studentsAssign = from.getStudentsAssign() != null ? from.getStudentsAssign() : new ArrayList<>();

        this.title = from.getTitle();
        this.description = from.getDescription();

        this.dueDate = from.getDueDate() != null ? from.getDueDate().toString() : null;
        this.dateAssigned = from.getDateAssigned() != null ? from.getDateAssigned().toString() : null;

        this.taskOfCourse = from.getTaskOfCourse();
        this.fromSchedule = from.getFromSchedule();
        this.dropbox = from.getDropbox();
        this.manualCompletionRequired = from.isManualCompletionRequired();
        this.publisher = from.getPublisher();
    }

    @Override
    public String getID() {
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

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
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

    public boolean isManualCompletionRequired() {
        return manualCompletionRequired;
    }

    public void setManualCompletionRequired(boolean manualCompletionRequired) {
        this.manualCompletionRequired = manualCompletionRequired;
    }

    public ArrayList<String> getStudentsAssign() {
        return studentsAssign;
    }

    public void setStudentsAssign(ArrayList<String> studentsAssign) {
        this.studentsAssign = studentsAssign;
    }
}
