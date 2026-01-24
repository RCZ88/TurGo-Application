package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class SubmissionRepository {

    private static SubmissionRepository instance;
    private final DatabaseReference submissionRef;

    public static SubmissionRepository getInstance(String submissionId) {
        if (instance == null) {
            instance = new SubmissionRepository(submissionId);
        }
        return instance;
    }

    private SubmissionRepository(String submissionId) {
        submissionRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.SUBMISSION.getPath())
                .child(submissionId);
    }

    // -------------------- CORE CRUD --------------------

    public void save(Submission object) {
        try {
            SubmissionFirebase firebaseObj = object.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(object);
            submissionRef.setValue(firebaseObj);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to save Submission", e);
        }
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
        submissionRef.child("files")
                .child(fileObj.getID())
                .setValue(isLate);
    }

    public void removeFile(String fileId) {
        submissionRef.child("files").child(fileId).removeValue();
    }

    public void removeFileCompletely(file fileObj) {
        submissionRef.child("files").child(fileObj.getID()).removeValue();
        FirebaseDatabase.getInstance()
                .getReference(fileObj.getFirebaseNode().getPath())
                .child(fileObj.getID())
                .removeValue();
    }

    // -------------------- STUDENT LINKING --------------------

    /**
     * Stores only the Student ID here, full object saved at its own node.
     */
    public void setStudent(Student student) {
        try {
            StudentFirebase firebaseObj = student.getFirebaseClass().newInstance();
            firebaseObj.importObjectData(student);

            // Save full student
            FirebaseDatabase.getInstance()
                    .getReference(student.getFirebaseNode().getPath())
                    .child(student.getID())
                    .setValue(firebaseObj);

            // Save only reference here
            submissionRef.child("of").setValue(student.getID());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Failed to link Student to Submission", e);
        }
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
