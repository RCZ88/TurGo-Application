package com.example.turgo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MeetingAlarmReciever extends BroadcastReceiver {
    //complete meeting
    @Override
    public void onReceive(Context context, Intent intent) {
        String meetingId = intent.getStringExtra("MeetingId");
        if (!Tool.boolOf(meetingId)) {
            Meeting meeting = (Meeting) intent.getSerializableExtra("Meeting");
            if (meeting != null) {
                meetingId = meeting.getID();
            }
        }
        if (!Tool.boolOf(meetingId)) {
            return;
        }
        MeetingCompletionService.completeMeetingIfDue(meetingId);
    }
}
