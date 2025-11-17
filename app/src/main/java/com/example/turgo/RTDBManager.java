package com.example.turgo;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class RTDBManager<T> {
    private static final String dbLinks = "https://turg0-4pp-default-rtdb.asia-southeast1.firebasedatabase.app";
    public void storeUserType(User user){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child(FirebaseNode.USERIDROLES.getPath()).child(user.getUID()).child("role").setValue(user.getUserType().type());
    }
    public boolean storeData(String path, String child, T object, String tag, String msgSuccessful){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference(path).child(child);
        AtomicBoolean successful = new AtomicBoolean(false);
        myRef.setValue(object)
                .addOnSuccessListener(aVoid ->{
                    successful.set(true);
                    Log.d("Firebase " + tag + " Data Saving", msgSuccessful + " Data Saved Successfully!");
                }).addOnFailureListener(e ->{
                    successful.set(false);
                    Log.e(tag, "Error Saving Data" + e.getMessage());
                });
        return successful.get();
    }
    public boolean storeData(String[]paths, T object, String tag, String msgSuccessful){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference(paths[0]);
        for(int i = 1; i< paths.length; i++){
            myRef = myRef.child(paths[i]);
        }
        AtomicBoolean successful = new AtomicBoolean(false);
        myRef.setValue(object)
                .addOnSuccessListener(aVoid ->{
                    successful.set(true);
                    Log.d("Firebase " + tag + " Data Saving", msgSuccessful + " Data Saved Successfully!");
                }).addOnFailureListener(e ->{
                    successful.set(false);
                    Log.e(tag, "Error Saving Data" + e.getMessage());
                });
        return successful.get();
    }
}
