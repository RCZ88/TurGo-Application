package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MailRepository implements RepositoryClass<Mail, MailFirebase>{
    private DatabaseReference mailRef;

    public MailRepository(String mailId) {
        mailRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MAIL.getPath())
                .child(mailId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return mailRef;
    }

    @Override
    public Class<MailFirebase> getFbClass() {
        return MailFirebase.class;
    }

    public void delete() {
        mailRef.removeValue();
    }
}