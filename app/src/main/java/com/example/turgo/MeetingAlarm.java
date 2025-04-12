package com.example.turgo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class MeetingAlarm {
    //Meeting alarm notification
    @SuppressLint("ScheduleExactAlarm")
    public static void setMeetingAlarm(Context context, LocalDateTime dateTime, Course course, Student student){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        LocalDateTime notifyAt = dateTime.minus(student.getNotificationEarlyDuration());
        long millis = notifyAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Intent intent = new Intent(context, CourseNotification.class);
        intent.putExtra(Course.SERIALIZE_KEY_CODE, course);
        intent.putExtra(Student.SERIALIZE_KEY_CODE, student);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(alarmManager != null){
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
        }
    }
}
