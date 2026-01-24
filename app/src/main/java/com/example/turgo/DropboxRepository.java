package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DropboxRepository {
    private static DropboxRepository instance;
    private DatabaseReference dropboxRef;

    public static DropboxRepository getInstance(String UID) {
        if (instance == null) {
            instance = new DropboxRepository(UID);
        }
        return instance;
    }

    private DropboxRepository(String UID) {
        dropboxRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.DROPBOX.getPath())
                .child(UID);
    }

    public void save(Dropbox object) {
        try {
            DropboxFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            dropboxRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save Dropbox", e);
        }
    }
    
    public void delete() {
        dropboxRef.removeValue();
    }

    /**
     * Adds a Submission to this Dropbox.
     * Saves the full Submission object to its Firebase path and stores only the ID reference here.
     */
    public void addSubmission(Submission item) {
        try {
            SubmissionFirebase firebaseObj = item.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(item);

            // Save full object to its own path
            FirebaseDatabase.getInstance()
                    .getReference(item.getFirebaseNode().getPath())
                    .child(item.getID())
                    .setValue(firebaseObj);

            // Save only the ID to this Dropbox's array
            dropboxRef.child("submissions").push().setValue(item.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to add submission", e);
        }
    }

    public void removeSubmission(String submissionId) {
        dropboxRef.child("submissions").child(submissionId).removeValue();
    }

    public void removeSubmissionCompletely(Submission item) {
        dropboxRef.child("submissions").child(item.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(item.getFirebaseNode().getPath())
                .child(item.getID())
                .removeValue();
    }

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        dropboxRef.updateChildren(childUpdates);
    }
}
