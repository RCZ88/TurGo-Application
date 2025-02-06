package com.example.turgo;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimeChecker {
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void addTimer(int hour, int minute, Meeting meeting){
        if(hour < 0 || hour > 24 || minute > 59 || minute < 0){
            return;
        }
        LocalTime targetTime = LocalTime.of(hour, minute);

        Runnable task = () -> {
            LocalTime currentTime = LocalTime.now();
            System.out.println("Checking time: " + currentTime);

            if (currentTime.equals(targetTime)) {
                for(Student student : meeting.getStudentsAttended().keySet()){
                    student.addMeetingCompleted(meeting);
                }
                meeting.setCompleted(true);
            }
        };
        scheduler.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);

    }

}
