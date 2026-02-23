package com.example.turgo;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

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
    private String dropbox;
    private Student of;
    private boolean completed;
    private transient Dropbox dropboxObject;

    public Submission(){
        files = new HashMap<>();
        this.completed = false;
        this.dropbox = null;
        this.of = null;
        this.submission_ID = UUID.randomUUID().toString();
    }

    public Submission(Student of, String dropbox){
        files = new HashMap<>();
        this.completed = false;
        this.dropbox = null;
        this.of = of;
        this.submission_ID = UUID.randomUUID().toString();
        this.dropbox = dropbox;
    }

    public void addFile(file file, boolean late){
        if (file == null || !Tool.boolOf(file.getID())) {
            return;
        }
        if (file != null && (file.getOfTask() == null || !Tool.boolOf(file.getOfTask().getID()))) {
            Dropbox cached = dropboxObject;
            if (cached != null && cached.getOfTask() != null) {
                file.setOfTask(cached.getOfTask());
            }
        }
        files.put(file,late);
        SubmissionRepository submissionRepository = new SubmissionRepository(getID());
        submissionRepository.addFile(file, late);
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

    public Task<Boolean> isLate(LocalDateTime time){
        TaskCompletionSource<Boolean>completionSource = new TaskCompletionSource<>();
        getDropboxObject().addOnSuccessListener(dropboxObject ->{
            if (dropboxObject == null || dropboxObject.getOfTask() == null || dropboxObject.getOfTask().getDueDate() == null) {
                completionSource.setResult(false);
            }else{
                completionSource.setResult(time.isAfter(dropboxObject.getOfTask().getDueDate()));
            }
        });
        return  completionSource.getTask();
    }

    public HashMap<file, Boolean> getFiles() {
        return files;
    }

    public void setFiles(HashMap<file, Boolean> files) {
        this.files = files;
    }

    public String getDropbox() {
        return dropbox;
    }

    public void setDropbox(String dropbox) {
        this.dropbox = dropbox;
    }

    public com.google.android.gms.tasks.Task<Dropbox> getDropboxObject() {
        if (dropboxObject != null) {
            return Tasks.forResult(dropboxObject);
        }
        if (!Tool.boolOf(dropbox)) {
            return Tasks.forResult(null);
        }
        TaskCompletionSource<Dropbox> tcs = new TaskCompletionSource<>();
        DropboxRepository repository = new DropboxRepository(dropbox);
        repository.loadAsNormal()
                .addOnSuccessListener(loaded -> {
                    dropboxObject = loaded;
                    tcs.setResult(loaded);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }


    public void setDropboxObject(Dropbox dropboxObject) {
        this.dropboxObject = dropboxObject;
        this.dropbox = dropboxObject != null ? dropboxObject.getID() : null;
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

    @Override
    public String toString() {
        return "Submission{" +
                "submission_ID='" + submission_ID + '\'' +
                ", files=" + files +
                ", dropbox='" + dropbox + '\'' +
                ", of=" + of +
                ", completed=" + completed +
                ", dropboxObject=" + dropboxObject +
                '}';
    }
}
