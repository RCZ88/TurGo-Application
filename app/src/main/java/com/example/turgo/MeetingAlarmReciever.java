package com.example.turgo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MeetingAlarmReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Student student = (Student)intent.getSerializableExtra("Student");
        Meeting meeting = (Meeting)intent.getSerializableExtra("Meeting");
        assert student != null;
        student.completeMeeting(meeting);
    }
}
