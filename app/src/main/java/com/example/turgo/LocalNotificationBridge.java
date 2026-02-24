package com.example.turgo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public final class LocalNotificationBridge {
    private static final String CHANNEL_ID = "turgo_general_channel";
    private static final String CHANNEL_NAME = "TurGo Notifications";
    private static final String CHANNEL_DESC = "Task, agenda, and submission notifications";
    private static final String PREF = "turgo_local_notif_seen";
    private static final String KEY_PREFIX = "notif_seen_";

    private LocalNotificationBridge() {
    }

    public static void notifyIfNew(Context context, NotificationFirebase notification) {
        if (context == null || notification == null || !Tool.boolOf(notification.getID())) {
            return;
        }
        String notificationId = notification.getID();
        if (!claimIfFirst(context, notificationId)) {
            return;
        }
        boolean shown = show(context, notification.getTitle(), notification.getContent(), notificationId.hashCode());
        if (!shown) {
            releaseClaim(context, notificationId);
        }
    }

    public static synchronized boolean claimIfFirst(Context context, String notificationId) {
        if (context == null || !Tool.boolOf(notificationId)) {
            return false;
        }
        SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String key = KEY_PREFIX + notificationId;
        if (pref.getBoolean(key, false)) {
            return false;
        }
        // commit() to reduce race window between foreground listeners and FCM callback
        return pref.edit().putBoolean(key, true).commit();
    }

    public static synchronized void releaseClaim(Context context, String notificationId) {
        if (context == null || !Tool.boolOf(notificationId)) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_PREFIX + notificationId).apply();
    }

    private static boolean show(Context context, String title, String body, int notifyId) {
        String safeTitle = Tool.boolOf(title) ? title : "TurGo";
        String safeBody = Tool.boolOf(body) ? body : "You have a new notification.";
        createChannelIfNeeded(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        Intent openIntent = new Intent(context, ActivityLauncher.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                Math.max(0, notifyId),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.green_turgo_logo)
                .setContentTitle(safeTitle)
                .setContentText(safeBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(safeBody))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent);

        NotificationManagerCompat.from(context).notify(notifyId, builder.build());
        return true;
    }

    private static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESC);
        manager.createNotificationChannel(channel);
    }
}
