package com.example.turgo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminRepository implements RepositoryClass<Admin, AdminFirebase>{
    private DatabaseReference adminRef;
    public AdminRepository(String adminId){
        adminRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.ADMIN.getPath()).child(adminId);
    }

    @Override
    public DatabaseReference getDbReference() {
        return adminRef;
    }

    @Override
    public Class<AdminFirebase> getFbClass() {
        return AdminFirebase.class;
    }

}
