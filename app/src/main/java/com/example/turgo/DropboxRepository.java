package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
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

    @Override
    public com.google.android.gms.tasks.Task<Dropbox> loadAsNormal() {
        TaskCompletionSource<Dropbox> taskSource = new TaskCompletionSource<>();
        String dbKey = dropboxRef.getKey();
        if (!Tool.boolOf(dbKey)) {
            taskSource.setResult(null);
            return taskSource.getTask();
        }

        dropboxRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                taskSource.setResult(null);
                return;
            }

            DropboxFirebase firebaseObject = snapshot.getValue(DropboxFirebase.class);
            if (firebaseObject == null) {
                taskSource.setResult(null);
                return;
            }

            String resolvedId = Tool.boolOf(firebaseObject.getID()) ? firebaseObject.getID() : dbKey;
            try {
                firebaseObject.constructClass(Dropbox.class, resolvedId, new ConstructClassCallback() {
                    @Override
                    public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                        Dropbox dropbox = (Dropbox) object;
                        dropbox.setUID(resolvedId);
                        taskSource.setResult(dropbox);
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        taskSource.setException(error.toException());
                    }
                });
            } catch (Exception e) {
                taskSource.setException(e);
            }
        }).addOnFailureListener(taskSource::setException);

        return taskSource.getTask();
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
