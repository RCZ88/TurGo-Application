package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RichBodyRepository implements RepositoryClass<RichBody, RichBodyFirebase> {
    DatabaseReference databaseReference;
    public RichBodyRepository(String id){
        databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseNode.RICH_BODY.getPath()).child(id);
    }
    @Override
    public DatabaseReference getDbReference() {
        return databaseReference;
    }

    @Override
    public Class<RichBodyFirebase> getFbClass() {
        return RichBodyFirebase.class;
    }
}
