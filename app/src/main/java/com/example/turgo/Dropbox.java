package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

public class Dropbox implements RequireUpdate<Dropbox, DropboxFirebase>{
    private final FirebaseNode fbn = FirebaseNode.DROPBOX;
    private final Class<DropboxFirebase> fbc = DropboxFirebase.class;
    private ArrayList<Submission>submissions;
    private final String UID;
    private Task ofTask;
    public Dropbox(Task ofTask){
        submissions = new ArrayList<>();
        this.ofTask = ofTask;
        setupSubmissions(ofTask.getStudentsAssigned());
        UID = UUID.randomUUID().toString();
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

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<DropboxFirebase> getFirebaseClass() {
        return fbc;
    }

    public String getID() {
        return UID;
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
