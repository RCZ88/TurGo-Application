package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationRepository implements RepositoryClass<Notification<?>, NotificationFirebase>{
    private DatabaseReference notifRepo;

    public NotificationRepository(String notifId){
        notifRepo = FirebaseDatabase.getInstance().getReference(FirebaseNode.NOTIFICATION.getPath()).child(notifId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return null;
    }

    @Override
    public Class<NotificationFirebase> getFbClass() {
        return null;
    }

    public DatabaseReference getNotifRepo() {
        return notifRepo;
    }

    public void setNotifRepo(DatabaseReference notifRepo) {
        this.notifRepo = notifRepo;
    }
}
