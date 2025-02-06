package com.example.turgo;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Submission {
    private HashMap<file, Boolean>files; //include lateness (true if late false if on time)
    private Dropbox dropbox;
    private Student of;
    private boolean completed;

    public Submission(Dropbox dropbox, Student of){
        files = new HashMap<>();
        this.dropbox = dropbox;
        this.completed = false;
        this.of = of;
    }

    public void addFile(file file){
        LocalDateTime timeOfSubmission = LocalDateTime.now();
        files.put(file, isLate(timeOfSubmission));
        if(!completed){
            completed = true;
        }
    }
    public boolean isLate(file file){
        return Boolean.TRUE.equals(files.get(file));
    }

    public boolean isLate(LocalDateTime time){
        if(time.isAfter(dropbox.getOfTask().getSubmissionDate())){
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
}
