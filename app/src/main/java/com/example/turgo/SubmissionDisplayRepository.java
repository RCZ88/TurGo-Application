package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SubmissionDisplayRepository implements RepositoryClass<SubmissionDisplay, SubmissionDisplay> {
    private final DatabaseReference dbRef;

    public SubmissionDisplayRepository(String id) {
        dbRef = FirebaseDatabase.getInstance()
                .getReference("submission-display")
                .child(id);
    }

    @Override
    public DatabaseReference getDbReference() {
        return dbRef;
    }

    @Override
    public Class<SubmissionDisplay> getFbClass() {
        return SubmissionDisplay.class;
    }
}
