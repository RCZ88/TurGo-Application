package com.example.turgo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Task implements Serializable {
    private String taskID;
    private static final String FIREBASE_DB_REFERENCE = "Tasks";
    public static String SERIALIZE_KEY_CODE = "taskObj";
    private RTDBManager<Task> rtdbManager;
    private ArrayList<Student>studentsAssigned;
    private String title;
    private String description;
    private LocalDateTime submissionDate;
    private Course taskOfCourse;
    private Schedule fromSchedule;
    private LocalDate dateAssigned;
    private final Dropbox dropbox;
    private Teacher publisher;

    public Task(String title, ArrayList<Student>studentsAssigned, String description, LocalDateTime submissionDate, Course taskOfCourse, Schedule fromSchedule, Teacher publisher){
        this.taskID = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.submissionDate = submissionDate;
        this.taskOfCourse = taskOfCourse;
        this.fromSchedule = fromSchedule;
        this.studentsAssigned = studentsAssigned;
        this.dateAssigned = LocalDate.now();
        this.publisher = publisher;
        this.dropbox = new Dropbox(this);
        rtdbManager = new RTDBManager<>();
    }

    public void updateDB(Task task){
        rtdbManager.storeData(FIREBASE_DB_REFERENCE, taskID, task, "Task", "Task");
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

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
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

    public ArrayList<Student> getStudentsAssigned() {
        return studentsAssigned;
    }

    public void setStudentsAssigned(ArrayList<Student> studentsAssigned) {
        this.studentsAssigned = studentsAssigned;
    }

    public Dropbox getDropbox() {
        return dropbox;
    }
}
