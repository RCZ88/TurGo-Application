package com.example.turgo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public final class NotificationPermissionHelper {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    private NotificationPermissionHelper() {
    }

    public static void requestIfNeeded(@NonNull AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_CODE_POST_NOTIFICATIONS
        );
    }
}
