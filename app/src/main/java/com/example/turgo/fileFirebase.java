package com.example.turgo;

import android.util.Log;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDateTime;

public class fileFirebase implements FirebaseClass<file>{
    private String fileID;
    private String secureURL;
    private String uploader;
    private String fileCreateDate;
    private String submitTime;
    private String ofTask;

    @Override
    public void importObjectData(file from) {
        if (from == null) {
            Log.w("fileFirebase", "importObjectData: source file is null");
            return;
        }

        fileID = from.getID();
        secureURL = from.getSecureURL();
        uploader = (from.getUploader() != null) ? from.getUploader().getUid() : "";

        // Use current time as fallback for dates
        fileCreateDate = (from.getFileCreateDate() != null) ?
                from.getFileCreateDate().toString() : LocalDateTime.now().toString();
        submitTime = (from.getSubmitTime() != null) ?
                from.getSubmitTime().toString() : null;

        ofTask = (from.getOfTask() != null) ? from.getOfTask().getID() : "";
    }

    public fileFirebase(){}


    @Override
    public String getID() {
        return fileID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<file> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(file.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((file) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getSecureURL() {
        return secureURL;
    }

    public void setSecureURL(String secureURL) {
        this.secureURL = secureURL;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getFileCreateDate() {
        return fileCreateDate;
    }

    public void setFileCreateDate(String fileCreateDate) {
        this.fileCreateDate = fileCreateDate;
    }

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public String getOfTask() {
        return ofTask;
    }

    public void setOfTask(String ofTask) {
        this.ofTask = ofTask;
    }
}
