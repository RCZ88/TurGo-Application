package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class SubmissionFirebase implements FirebaseClass<Submission>{
    private String submission_ID;
    private HashMap<String, Boolean> files;  // Stores file IDs instead of file objects
    private String dropbox;                 // Stores Dropbox ID
    private String of;                      // Stores Student ID
    private boolean completed;

    public SubmissionFirebase(){}
    @Override
    public void importObjectData(Submission from) {
        if (from == null) return;

        // Use getter methods for all private fields
        this.submission_ID = from.getID();
        this.completed = from.isCompleted();  // or getCompleted() depending on actual method name

        if (from.getFiles() != null) {
            this.files = new HashMap<>();
            for (Map.Entry<file, Boolean> entry : from.getFiles().entrySet()) {
                file f = entry.getKey();
                if (f != null) {
                    this.files.put(f.getID(), entry.getValue());
                }
            }
        }

        // Assuming Dropbox and Student have getID() methods
        Dropbox dropbox = Await.get(from::getDropbox);
        this.dropbox = (dropbox != null) ? dropbox.getID() : null;
        this.of = (from.getOf() != null) ? from.getOf().getID() : null;
    }

    @Override
    public String getID() {
        return submission_ID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Submission> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Submission.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Submission) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getSubmission_ID() {
        return submission_ID;
    }

    public void setSubmission_ID(String submission_ID) {
        this.submission_ID = submission_ID;
    }

    public HashMap<String, Boolean> getFiles() {
        return files;
    }

    public void setFiles(HashMap<String, Boolean> files) {
        this.files = files;
    }

    public String getDropbox() {
        return dropbox;
    }

    public void setDropbox(String dropbox) {
        this.dropbox = dropbox;
    }

    public String getOf() {
        return of;
    }

    public void setOf(String of) {
        this.of = of;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
