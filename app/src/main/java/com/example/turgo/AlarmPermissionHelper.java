package com.example.turgo;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

public class AlarmPermissionHelper {

    /**
     * Check if the app can schedule exact alarms
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true; // Below Android 12, no permission needed
    }

    /**
     * Request exact alarm permission (opens system settings)
     */
    public static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms()) {
                // Show dialog explaining why permission is needed
                new AlertDialog.Builder(context)
                        .setTitle("Permission Required")
                        .setMessage("Turgo needs permission to schedule exact meeting alarms. " +
                                "This ensures you receive notifications at the exact meeting time.")
                        .setPositiveButton("Grant Permission", (dialog, which) -> {
                            // Open system settings for exact alarm permission
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    /**
     * Request permission with callback (for activities)
     */
    public static void requestExactAlarmPermissionWithCallback(
            Context context,
            Runnable onGranted,
            Runnable onDenied) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager.canScheduleExactAlarms()) {
                onGranted.run();
            } else {
                new AlertDialog.Builder(context)
                        .setTitle("Permission Required")
                        .setMessage("Turgo needs permission to schedule exact meeting alarms.")
                        .setPositiveButton("Grant Permission", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            if (onDenied != null) {
                                onDenied.run();
                            }
                        })
                        .show();
            }
        } else {
            onGranted.run(); // Below Android 12, proceed without permission
        }
    }
}