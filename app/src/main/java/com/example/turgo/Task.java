package com.example.turgo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class Task implements Serializable, RequireUpdate<Task,TaskFirebase> {
    private final FirebaseNode fbn = FirebaseNode.TASK;
    private final Class<TaskFirebase> fbc = TaskFirebase.class;
    private String taskID;
    public static String SERIALIZE_KEY_CODE = "taskObj";
    private static RTDBManager<Task> rtdbManager;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Course taskOfCourse;
    private Schedule fromSchedule;
    private LocalDate dateAssigned;
    private final Dropbox dropbox;
    private Teacher publisher;

    public Task(String title, String description, LocalDateTime submissionDate, Course taskOfCourse, Schedule fromSchedule, Teacher publisher, boolean dropbox){
        this.taskID = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.dueDate = submissionDate;
        this.taskOfCourse = taskOfCourse;
        this.fromSchedule = fromSchedule;
        this.dateAssigned = LocalDate.now();
        this.publisher = publisher;
        if(dropbox){
            this.dropbox = new Dropbox(this);
        }else{
            this.dropbox = null;
        }
        rtdbManager = new RTDBManager<>();
    }

    public Task(){
        this.dropbox = null;
    }

    public void submit(file file, Student student){
        dropbox.getSubmissionSlot(student).addFile(file);
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Course getTaskOfCourse() {
        return taskOfCourse;
    }

    public void setTaskOfCourse(Course taskOfCourse) {
        this.taskOfCourse = taskOfCourse;
    }

    public Schedule getFromSchedule() {
        return fromSchedule;
    }

    public void setFromSchedule(Schedule fromSchedule) {
        this.fromSchedule = fromSchedule;
    }

    public Teacher getPublisher() {
        return publisher;
    }

    public void setPublisher(Teacher publisher) {
        this.publisher = publisher;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public RTDBManager<Task> getRtdbManager() {
        return rtdbManager;
    }

    public void setRtdbManager(RTDBManager<Task> rtdbManager) {
        this.rtdbManager = rtdbManager;
    }

    public LocalDate getDateAssigned() {
        return dateAssigned;
    }

    public void setDateAssigned(LocalDate dateAssigned) {
        this.dateAssigned = dateAssigned;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<TaskFirebase> getFirebaseClass() {
        return fbc;
    }

    public boolean isComplete(Student student){
        for(Submission submission : dropbox.getSubmissions()){
            if(submission.getOf() == student){
                if(submission.isCompleted()){
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public void getStudentsAssigned(ObjectCallBack<ArrayList<Student>>callBack) {
        try{
            findAllAggregatedObjects(Student.class, "allTask", callBack);
        }catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    public Dropbox getDropbox() {
        return dropbox;
    }


    @Override
    public String getID() {
        return this.taskID;
    }
}
