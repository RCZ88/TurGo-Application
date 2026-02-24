package com.example.turgo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class TurgoFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "turgo_general_channel";
    private static final String CHANNEL_NAME = "TurGo Notifications";
    private static final String CHANNEL_DESC = "Task, agenda, and submission notifications";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (Tool.boolOf(uid)) {
            PushTokenManager.syncCurrentUserTokenByRole(uid);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        createChannelIfNeeded();

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        Map<String, String> data = remoteMessage.getData();
        String notifId = data.get("notifId");
        if (!Tool.boolOf(title)) {
            title = data.get("title");
        }
        if (!Tool.boolOf(body)) {
            body = data.get("body");
        }
        if (!Tool.boolOf(title)) {
            title = "TurGo";
        }
        if (!Tool.boolOf(body)) {
            body = "You have a new notification.";
        }

        if (Tool.boolOf(notifId) && !LocalNotificationBridge.claimIfFirst(this, notifId)) {
            return;
        }
        boolean shown = showNotification(title, body, notifId);
        if (!shown && Tool.boolOf(notifId)) {
            LocalNotificationBridge.releaseClaim(this, notifId);
        }
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESC);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private boolean showNotification(String title, String body, String notifId) {
        Intent openIntent = new Intent(this, ActivityLauncher.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.green_turgo_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        int notifyId = Tool.boolOf(notifId)
                ? Math.abs(notifId.hashCode())
                : (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        NotificationManagerCompat.from(this)
                .notify(notifyId, builder.build());
        return true;
    }
}
