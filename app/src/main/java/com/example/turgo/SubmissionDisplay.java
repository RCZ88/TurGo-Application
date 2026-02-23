package com.example.turgo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubmissionDisplay implements Serializable, RequireUpdate<SubmissionDisplay, SubmissionDisplay, SubmissionDisplayRepository>, FirebaseClass<SubmissionDisplay>{
    private static final long serialVersionUID = 1L;
    private String id;
    private String name, task, course, submittedTimeDate;
    private boolean isLate;
    private boolean hasStatus;
    private ArrayList<String> filesSubmitted;
    public SubmissionDisplay(String name, String task, String course, String submittedTimeDate, ArrayList<String> filesSubmitted, boolean isLate) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.task = task;
        this.course = course;
        this.isLate = isLate;
        hasStatus = true;
        this.submittedTimeDate = submittedTimeDate;
        this.filesSubmitted = filesSubmitted;
    }
    public SubmissionDisplay(String name, String task, String course, String submittedTimeDate, ArrayList<String> filesSubmitted){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.task = task;
        this.course = course;
        hasStatus = false;
        this.submittedTimeDate = submittedTimeDate;
        this.filesSubmitted = filesSubmitted;
    }
    public SubmissionDisplay(){this.id = UUID.randomUUID().toString();}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public boolean isLate() {
        return isLate;
    }

    @Exclude
    public void setLate(boolean late) {
        isLate = late;
    }

    @PropertyName("isLate")
    public boolean getIsLate() {
        return isLate;
    }

    @PropertyName("isLate")
    public void setIsLate(boolean late) {
        isLate = late;
    }

    public boolean isHasStatus() {
        return hasStatus;
    }

    public void setHasStatus(boolean hasStatus) {
        this.hasStatus = hasStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getSubmittedTimeDate() {
        return submittedTimeDate;
    }

    public void setSubmittedTimeDate(String submittedTimeDate) {
        this.submittedTimeDate = submittedTimeDate;
    }

    public ArrayList<String> getFilesSubmitted() {
        return filesSubmitted;
    }

    public void setFilesSubmitted(ArrayList<String> filesSubmitted) {
        this.filesSubmitted = filesSubmitted;
    }

    @Override
    public void importObjectData(SubmissionDisplay from) {
        if (from == null) {
            return;
        }
        this.id = from.id;
        this.name = from.name;
        this.task = from.task;
        this.course = from.course;
        this.submittedTimeDate = from.submittedTimeDate;
        this.isLate = from.isLate;
        this.hasStatus = from.hasStatus;
        this.filesSubmitted = from.filesSubmitted != null ? new ArrayList<>(from.filesSubmitted) : new ArrayList<>();
    }

    @Override
    public void convertToNormal(ObjectCallBack<SubmissionDisplay> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        objectCallBack.onObjectRetrieved(this);
    }

    @Override
    @Exclude
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.SUBMISSION_DISPLAY;
    }

    @Override
    @Exclude
    public Class<SubmissionDisplayRepository> getRepositoryClass() {
        return SubmissionDisplayRepository.class;
    }

    @Override
    @Exclude
    public Class<SubmissionDisplay> getFirebaseClass() {
        return SubmissionDisplay.class;
    }

    @Override
    @Exclude
    public String getID() {
        return id;
    }

    @Override
    @Exclude
    public SubmissionDisplayRepository getRepositoryInstance() {
        return RequireUpdate.super.getRepositoryInstance();
    }

    @Override
    @Exclude
    public Task<List<SubmissionDisplay>> getAllObjects() {
        return RequireUpdate.super.getAllObjects();
    }
}
