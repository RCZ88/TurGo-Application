package com.example.turgo;

import android.app.Application;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TurgoApp extends Application {
    @Override
    public void onCreate(){

        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", "qHMUfR5ZZQjqJIldJqtViW7YDWw");
        Log.d("TurgoApp", "Api Secret: " +  config.get("api_secret"));

        MediaManager.init(this, config);

        FirebaseApp.initializeApp(this);
//        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//

    }
}
