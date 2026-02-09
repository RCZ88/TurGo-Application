package com.example.turgo;

import android.app.Application;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TurgoApp extends Application {
    @Override
    public void onCreate(){

        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", "qHMUfR5ZZQjqJIldJqtViW7YDWw");

        MediaManager.init(this, config);

        FirebaseApp.initializeApp(this);
//        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
//
        scheduleMeetingProcessor();
    }

    private void scheduleMeetingProcessor() {
        // Run every day at 00:00 WIB
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest meetingWork = new PeriodicWorkRequest.Builder(
                MeetingProcessorWorker.class,
                1, TimeUnit.DAYS  // Run once per day
        )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MeetingProcessor",
                ExistingPeriodicWorkPolicy.KEEP,  // Don't restart if already scheduled
                meetingWork
        );
    }

    private long calculateInitialDelay() {
        // Calculate delay to next 00:00 WIB
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(
                java.time.ZoneId.of("Asia/Jakarta")
        );
        java.time.ZonedDateTime nextRun = now.toLocalDate().plusDays(1)
                .atStartOfDay(java.time.ZoneId.of("Asia/Jakarta"));

        return java.time.Duration.between(now, nextRun).toMillis();
    }
}
