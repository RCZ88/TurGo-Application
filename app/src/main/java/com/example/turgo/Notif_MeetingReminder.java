package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

public class Notif_MeetingReminder extends Notification<Course>{
    public Schedule schedule;
    public Notif_MeetingReminder(Schedule schedule, LocalDateTime timeSent, Course from, User to, int earlyDelay) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        super(from.getCourseName() + " Meeting Reminder",
                "Your " + schedule.getDay() + "'s " + from.getCourseName() + " is about to start in " + earlyDelay + " minutes",
                timeSent, from, to);
    }
}
