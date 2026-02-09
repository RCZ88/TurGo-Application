package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ParentRepository implements RepositoryClass<Parent, ParentFirebase> {

    private DatabaseReference parentRef;

    public ParentRepository(String parentId) {
        parentRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.PARENT.getPath())
                .child(parentId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return parentRef;
    }

    @Override
    public Class<ParentFirebase> getFbClass() {
        return ParentFirebase.class;
    }

    /* =======================
       CHILD MANAGEMENT
       ======================= */

    public void addChild(Student student) {
        addStringToArray("children", student.getID());
    }

    public void removeChild(String studentId) {
        removeStringFromArray("children", studentId);
    }
}
