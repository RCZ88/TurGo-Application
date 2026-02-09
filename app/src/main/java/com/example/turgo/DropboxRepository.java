package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.HashMap;
import java.util.Map;

public class DropboxRepository implements RepositoryClass<Dropbox, DropboxFirebase> {
    private DatabaseReference dropboxRef;

    public DropboxRepository(String UID) {
        dropboxRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.DROPBOX.getPath())
                .child(UID);
    }

    @Override
    public DatabaseReference getDbReference() {
        return dropboxRef;
    }

    @Override
    public Class<DropboxFirebase> getFbClass() {
        return DropboxFirebase.class;
    }

    public void delete() {
        dropboxRef.removeValue();
    }

    /**
     * Adds a Submission ID reference to this Dropbox.
     */
    public void addSubmission(Submission item) {
        addStringToArray("submissions", item.getID());
    }

    public void removeSubmission(String submissionId) {
        removeStringFromArray("submissions", submissionId);
    }

    /**
     * Removes the Submission reference from this Dropbox and deletes the Submission object.
     */
    public void removeSubmissionCompletely(Submission item) {
        removeStringFromArray("submissions", item.getID());
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
