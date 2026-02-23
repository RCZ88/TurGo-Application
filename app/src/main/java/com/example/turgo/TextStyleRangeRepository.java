package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TextStyleRangeRepository implements RepositoryClass<TextStyleRange, TextStyleRange> {
    DatabaseReference dbRef;
    public TextStyleRangeRepository(String id){
        this.dbRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.TEXT_STYLE_RANGE.getPath()).child(id);
    }

    @Override
    public DatabaseReference getDbReference() {
        return dbRef;
    }

    @Override
    public Class<TextStyleRange> getFbClass() {
        return TextStyleRange.class;
    }
}
