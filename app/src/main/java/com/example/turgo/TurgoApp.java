package com.example.turgo;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class TurgoApp extends Application {
    @Override
    public void onCreate(){

        super.onCreate();
        Map<String,String> config = new HashMap<>();
        config.put("cloud_name", "daccry0jr");
        MediaManager.init(this, config);

        FirebaseApp.initializeApp(this);
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }
}
