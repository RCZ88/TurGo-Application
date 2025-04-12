package com.example.turgo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

import javax.annotation.Nullable;

public class TimeSlot {
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    private Duration time;
    private Schedule existingSchedule;

    public TimeSlot(LocalTime start, LocalTime end){
        this.start = start;
        this.end = end;
    }
    public TimeSlot(DayOfWeek day, LocalTime start, LocalTime end, @Nullable Schedule existingSchedule){
        this.day = day;
        this.start = start;
        this.end = end;
        this.time = Duration.between(start, end);
        this.existingSchedule = existingSchedule;
    }
    public TimeSlot(DayOfWeek day, LocalTime start, Duration time){
        this.day = day;
        this.start = start;
        this.time = time;
        this.end = start.plus(time);
    }
    public DayOfWeek getDay(){
        return day;
    }

    public void setDay(DayOfWeek day){
        this.day = day;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public Duration getTime() {
        return time;
    }

    public void setTime(Duration time) {
        this.time = time;
    }

    public String toStr(){
        return start.toString()  + " - "  + end.toString();
    }

    public Schedule getExistingSchedule() {
        return existingSchedule;
    }

    public void setExistingSchedule(Schedule existingSchedule) {
        this.existingSchedule = existingSchedule;
    }
}
