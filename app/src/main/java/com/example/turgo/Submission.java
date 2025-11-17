package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Submission implements RequireUpdate<Submission, SubmissionFirebase> {
    private final FirebaseNode fbn = FirebaseNode.SUBMISSION;
    private final Class<SubmissionFirebase> fbc = SubmissionFirebase.class;
    private String submission_ID;
    private HashMap<file, Boolean>files; //include lateness (true if late false if on time)
    private Dropbox dropbox;
    private Student of;
    private boolean completed;

    public Submission(Dropbox dropbox, Student of){
        files = new HashMap<>();
        this.dropbox = dropbox;
        this.completed = false;
        this.of = of;
        this.submission_ID = UUID.randomUUID().toString();
    }

    public void addFile(file file){
        LocalDateTime timeOfSubmission = LocalDateTime.now();
        files.put(file, isLate(timeOfSubmission));
        if(!completed){
            completed = true;
        }
    }
    public ArrayList<file> getFilesOnly(){
        ArrayList<file> files = new ArrayList<>();
        for(Map.Entry<file, Boolean>file : this.files.entrySet()){
            files.add(file.getKey());
        }
        return files;
    }

    public boolean isLate(LocalDateTime time){
        if(time.isAfter(dropbox.getOfTask().getDueDate())){
            return true;
        }
        return false;
    }

    public HashMap<file, Boolean> getFiles() {
        return files;
    }

    public void setFiles(HashMap<file, Boolean> files) {
        this.files = files;
    }

    public Dropbox getDropbox() {
        return dropbox;
    }

    public void setDropbox(Dropbox dropbox) {
        this.dropbox = dropbox;
    }

    public void setOf(Student of) {
        this.of = of;
    }

    public Student getOf() {
        return of;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<SubmissionFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return this.submission_ID;
    }
}
