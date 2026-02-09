package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Submission implements RequireUpdate<Submission, SubmissionFirebase, SubmissionRepository> {
    private final FirebaseNode fbn = FirebaseNode.SUBMISSION;
    private final Class<SubmissionFirebase> fbc = SubmissionFirebase.class;
    private String submission_ID;
    private HashMap<file, Boolean>files; //include lateness (true if late false if on time)
    private Student of;
    private boolean completed;

    public Submission(Student of){
        files = new HashMap<>();
        this.completed = false;
        this.of = of;
        this.submission_ID = UUID.randomUUID().toString();
    }

    public void addFile(file file){
        LocalDateTime timeOfSubmission = LocalDateTime.now();
        files.put(file, isLate(timeOfSubmission));
        SubmissionRepository submissionRepository = new SubmissionRepository(getID());
        submissionRepository.addFile(file, isLate(timeOfSubmission));
        if(!completed){
            completed = true;
            submissionRepository.setCompleted(true);
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
        Dropbox dropbox = Await.get(this::getDropbox);
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

    public void  getDropbox(ObjectCallBack<Dropbox>callBack) {
        try {
            findAggregatedObject(Dropbox.class, "submissions", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
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
    public Class<SubmissionRepository> getRepositoryClass() {
        return SubmissionRepository.class;
    }

    @Override
    public String getID() {
        return this.submission_ID;
    }
}
