package com.example.turgo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class TimeSlot implements Serializable {
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    private Duration time;
    private ArrayList<Schedule>schedules;
    private int minuteIncrement;

    public TimeSlot(LocalTime start, LocalTime end){
        this.start = start;
        this.end = end;
    }
    public TimeSlot(DayOfWeek day, LocalTime start, LocalTime end,  int minuteIncrement){
        this.day = day;
        this.start = start;
        this.end = end;
        this.time = Duration.ofMinutes(Math.abs(end.minusMinutes(start.getMinute()).getMinute()));
        Log.d("TimeSlot", "Duration: "+ this.time);
        this.minuteIncrement = minuteIncrement;
    }
    public TimeSlot(DayOfWeek day, LocalTime start, Duration time){
        this.day = day;
        this.start = start;
        this.time = time;
        this.end = start.plus(time);
    }
    public static ArrayList<TimeSlot> filterTimesOfIncrement(ArrayList<TimeSlot> timeSlots, int increment){
        Log.d("filterTimeSlot", "(USING INCREMENT OF: " + increment + " MINUTES)");
        Log.d("FilterTimesSlot", "Size of TS {before} Filter: " + timeSlots.size());
        ArrayList<TimeSlot>afterFilter  = new ArrayList<>();
        for(TimeSlot ts : timeSlots){
            Log.d("filterTimeSlot", "offset: "+ ts.getMinuteIncrement() );
            if(ts.getMinuteIncrement() == increment){
                afterFilter.add(ts);
            }
        }

        Log.d("filterTimesSlot", "Size of TS {AFTER} Filter: " + afterFilter.size());
        return afterFilter;
    }
    public int getMinuteIncrement() {
        return minuteIncrement;
    }

    public ArrayList<Schedule> getSchedules() {
        if(schedules == null){
            schedules = new ArrayList<>();
        }
        return schedules;
    }
    public Pair<Integer, Integer> getPersonCount(){
        if(schedules.isEmpty()){
            return new Pair<>(1, 1);
        }

        int maxPersonCount = 0;
        int minPersonCount = Integer.MAX_VALUE;
        for(Schedule schedule : schedules){
            if(schedule.getNumberOfStudents()>maxPersonCount){
                maxPersonCount = schedule.getNumberOfStudents();
            }
            if(schedule.getNumberOfStudents()<minPersonCount){
                minPersonCount = schedule.getNumberOfStudents();
            }
        }
        return new Pair<>(maxPersonCount, minPersonCount);
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }

    public void setMinuteIncrement(int minuteIncrement) {
        this.minuteIncrement = minuteIncrement;
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
    @NonNull
    @Override
    public String toString(){
        return start.toString()  + " - "  + end.toString() + " (minute Increment: " + minuteIncrement + "m)";
    }


}
