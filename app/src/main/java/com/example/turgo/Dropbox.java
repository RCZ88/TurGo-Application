package com.example.turgo;

import java.util.ArrayList;

public class Dropbox {
    private ArrayList<Submission>submissions;
    private Task ofTask;
    public Dropbox(Task ofTask){
        submissions = new ArrayList<>();
        this.ofTask = ofTask;
        setupSubmissions(ofTask.getStudentsAssigned());
    }
    public Submission getSubmissionSlot(Student student){
        for(Submission submission : submissions){
            if(submission.getOf() == student){
                return submission;
            }
        }
        return null;
    }
    public void setupSubmissions(ArrayList<Student> students){
        for(Student student : students){
            submissions.add(new Submission(this, student));
        }
    }


    public ArrayList<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(ArrayList<Submission> submissions) {
        this.submissions = submissions;
    }

    public Task getOfTask() {
        return ofTask;
    }

    public void setOfTask(Task ofTask) {
        this.ofTask = ofTask;
    }
}
