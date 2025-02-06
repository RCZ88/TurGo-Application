package com.example.turgo;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.atomic.AtomicBoolean;

public class RTDBManager<T> {
    private static final String dbLinks = "https://turg0-4pp-default-rtdb.asia-southeast1.firebasedatabase.app";
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
}
