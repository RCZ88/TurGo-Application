package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class SubmissionRepository implements RepositoryClass<Submission, SubmissionFirebase>{

    private final DatabaseReference submissionRef;

    public SubmissionRepository(String submissionId) {
        submissionRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.SUBMISSION.getPath())
                .child(submissionId);
    }

    // -------------------- CORE CRUD --------------------

    @Override
    public DatabaseReference getDbReference() {
        return submissionRef;
    }

    @Override
    public Class<SubmissionFirebase> getFbClass() {
        return SubmissionFirebase.class;
    }


    public void delete() {
        submissionRef.removeValue();
    }

    // -------------------- FIELD UPDATES --------------------

    public void setCompleted(boolean completed) {
        submissionRef.child("completed").setValue(completed);
    }

    // -------------------- FILE MANAGEMENT --------------------

    /**
     * Adds a file entry with lateness flag.
     */
    public void addFile(file fileObj, boolean isLate) {
        addStringToArray("files", fileObj.getID());
    }

    public void removeFile(String fileId) {
        removeStringFromArray("files", fileId);
    }

    public void removeFileCompletely(file fileObj) {
        removeStringFromArray("files", fileObj.getID());
        FirebaseDatabase.getInstance()
                .getReference(fileObj.getFirebaseNode().getPath())
                .child(fileObj.getID())
                .removeValue();
    }

    // -------------------- STUDENT LINKING --------------------

    /**
     * Stores only the Student ID here.
     */
    public void setStudent(Student student) {
        submissionRef.child("of").setValue(student.getID());
    }

    public void clearStudent() {
        submissionRef.child("of").removeValue();
    }

    // -------------------- BULK UPDATE --------------------

    public void updateMultipleFields(Map<String, Object> updates) {
        Map<String, Object> childUpdates = new HashMap<>();
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            childUpdates.put(entry.getKey(), entry.getValue());
        }
        childUpdates.put("lastModified", ServerValue.TIMESTAMP);
        submissionRef.updateChildren(childUpdates);
    }
}
