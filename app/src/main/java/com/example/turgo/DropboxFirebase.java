package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class DropboxFirebase implements FirebaseClass<Dropbox>{
    private ArrayList<String>submissions;
    private String dropboxID;
    private String ofTaskID;
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
    public Dropbox convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Dropbox) constructClass(Dropbox.class, dropboxID);
    }
}
