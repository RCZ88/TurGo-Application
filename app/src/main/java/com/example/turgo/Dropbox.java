package com.example.turgo;

import com.google.android.gms.tasks.TaskCompletionSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

public class Dropbox implements RequireUpdate<Dropbox, DropboxFirebase, DropboxRepository>{
    private final FirebaseNode fbn = FirebaseNode.DROPBOX;
    private final Class<DropboxFirebase> fbc = DropboxFirebase.class;
    private ArrayList<Submission>submissions;
    private final String UID;
    private Task ofTask;
    public Dropbox(Task ofTask){
        submissions = new ArrayList<>();
        this.ofTask = ofTask;
        UID = UUID.randomUUID().toString();
    }




    public com.google.android.gms.tasks.Task<Void> setup() {

        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        ofTask.getStudentAssigned()
                .addOnSuccessListener(students -> {
                    setupSubmissions((ArrayList<Student>) students);
                    tcs.setResult(null);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
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
            submissions.add(new Submission(student));
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

    @Override
    public Class<DropboxRepository> getRepositoryClass() {
        return DropboxRepository.class;
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
