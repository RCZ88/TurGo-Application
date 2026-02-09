package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class DropboxFirebase implements FirebaseClass<Dropbox>{
    private ArrayList<String>submissions;
    private String dropboxID;
    private String ofTaskID;
    public DropboxFirebase(){}

    @Override
    public void importObjectData(Dropbox from) {
        if (from == null) return;

        // Convert list of Submission objects to their IDs
        if (from.getSubmissions() != null) {
            this.submissions = convertToIdList(from.getSubmissions());
        }

        // Directly map the UID to dropboxID
        this.dropboxID = from.getID();

        // Convert Task object to its ID
        this.ofTaskID = (from.getOfTask() != null) ? from.getOfTask().getID() : null;
    }

    @Override
    public String getID() {
        return dropboxID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Dropbox> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Dropbox.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Dropbox) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public ArrayList<String> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(ArrayList<String> submissions) {
        this.submissions = submissions;
    }

    public String getDropboxID() {
        return dropboxID;
    }

    public void setDropboxID(String dropboxID) {
        this.dropboxID = dropboxID;
    }

    public String getOfTaskID() {
        return ofTaskID;
    }

    public void setOfTaskID(String ofTaskID) {
        this.ofTaskID = ofTaskID;
    }
}
