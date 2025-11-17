package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class fileFirebase implements FirebaseClass<file>{
    private String fileID;
    private String secureURL;
    private String uploaderID;
    private String fileCreatedDate;
    private String submitTime;
    private String ofTaskID;

    @Override
    public void importObjectData(file from) {
        fileID = from.getID();
        secureURL = from.getSecureURL();
        uploaderID = from.getUploader().getUID();
        fileCreatedDate = from.getFileCreateDate().toString();
        submitTime = from.getSubmitTime().toString();
        ofTaskID = from.getOfTask().getID();
    }


    @Override
    public String getID() {
        return fileID;
    }

    @Override
    public file convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (file) constructClass(file.class, getID());
    }
}
