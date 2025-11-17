package com.example.turgo;

import java.util.ArrayList;

public class SubmissionDisplay {
    private String name, task, course, submittedTimeDate;
    private ArrayList<String> filesSubmitted;
    public SubmissionDisplay(String name, String task, String course, String submittedTimeDate, ArrayList<String> filesSubmitted) {
        this.name = name;
        this.task = task;
        this.course = course;
        this.submittedTimeDate = submittedTimeDate;
        this.filesSubmitted = filesSubmitted;
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
}
