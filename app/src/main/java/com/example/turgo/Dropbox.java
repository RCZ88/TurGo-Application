package com.example.turgo;

import com.google.android.gms.tasks.TaskCompletionSource;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

public class Dropbox implements RequireUpdate<Dropbox, DropboxFirebase, DropboxRepository>{
    private final FirebaseNode fbn = FirebaseNode.DROPBOX;
    private final Class<DropboxFirebase> fbc = DropboxFirebase.class;
    private ArrayList<Submission>submissions;
    private String UID;
    private Task ofTask;
    public Dropbox(Task ofTask){
        submissions = new ArrayList<>();
        this.ofTask = ofTask;
        UID = UUID.randomUUID().toString();
    }
    public Dropbox(){
        UID = UUID.randomUUID().toString();
        submissions = new ArrayList<>();
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
        if (student == null || !Tool.boolOf(student.getID())) {
            return null;
        }
        for(Submission submission : getSubmissions()){
            Student owner = submission.getOf();
            if(owner != null && Tool.boolOf(owner.getID()) && owner.getID().equals(student.getID())){
                return submission;
            }
        }
        return null;
    }
    public void setupSubmissions(ArrayList<Student> students){
        for(Student student : students){
            Submission submissionSlot = new Submission(student, this.getID());
            submissionSlot.setDropbox(getID());
            SubmissionRepository submissionRepository = new SubmissionRepository(submissionSlot.getID());
            submissionRepository.save(submissionSlot);
            submissions.add(submissionSlot);
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

    public void setUID(String UID) {
        this.UID = UID;
    }

    @Override
    public Class<DropboxRepository> getRepositoryClass() {
        return DropboxRepository.class;
    }

    public ArrayList<Submission> getSubmissions() {
        if (submissions == null) {
            submissions = new ArrayList<>();
        }
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
