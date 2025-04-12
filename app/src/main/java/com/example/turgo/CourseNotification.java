package com.example.turgo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CourseNotification extends BroadcastReceiver {
    private static final String CHANNEL_ID = "course_notifications";
    private Course course;
    private Student student;

    @Override
    public void onReceive(Context context, Intent intent) {
        course = (Course) intent.getSerializableExtra(Course.SERIALIZE_KEY_CODE);
        student = (Student) intent.getSerializableExtra(Student.SERIALIZE_KEY_CODE);
        createNotificationChannel(context);
        sendNotification(context);

    }

    private void createNotificationChannel(Context context) {
        CharSequence name = "Course Notifications";
        String description = "Your " + course.getCourseName() + " Course is in " + student.getNotificationEarlyDuration().toString();
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotification(Context context) {
        Intent intent = new Intent(context, StudentScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String description = "Your " + course.getCourseName() + " Course is in " + student.getNotificationEarlyDuration().toString();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.turgo_logo_square)
                .setContentTitle("Meeting Reminder")
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
