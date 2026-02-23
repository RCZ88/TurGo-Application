package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class DropboxFirebase implements FirebaseClass<Dropbox>{
    private ArrayList<String>submissions;
    private String UID;
    private String ofTask;
    private String dropboxID; // legacy alias
    public DropboxFirebase(){}

    @Override
    public void importObjectData(Dropbox from) {
        if (from == null) return;

        // Convert list of Submission objects to their IDs
        if (from.getSubmissions() != null) {
            this.submissions = convertToIdList(from.getSubmissions());
        }

        this.UID = from.getID();
        this.dropboxID = this.UID;

        this.ofTask = (from.getOfTask() != null) ? from.getOfTask().getID() : null;
    }

    @Override
    public String getID() {
        return Tool.boolOf(UID) ? UID : dropboxID;
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
        this.UID = dropboxID;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
        this.dropboxID = UID;
    }

    public String getOfTask() {
        return ofTask;
    }

    public void setOfTask(String ofTask) {
        this.ofTask = ofTask;
    }
}
