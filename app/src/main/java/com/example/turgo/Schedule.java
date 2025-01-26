package com.example.turgo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

public class Schedule {
    private LocalTime meetingStart;
    private LocalTime meetingEnd;
    private int duration;
    private DayOfWeek day;
    private Room room;

    public Schedule(LocalTime meetingStart, int duration, DayOfWeek day, Room room){
        this.meetingStart = meetingStart;
        this.duration = duration;
        this.day = day;
        this.meetingEnd = meetingStart.plus(Duration.ofMinutes(duration));
        this.room = room;
    }

    public LocalTime getMeetingStart() {
        return meetingStart;
    }

    public void setMeetingStart(LocalTime meetingStart) {
        this.meetingStart = meetingStart;
    }

    public LocalTime getMeetingEnd() {
        return meetingEnd;
    }

    public void setMeetingEnd(LocalTime meetingEnd) {
        this.meetingEnd = meetingEnd;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
