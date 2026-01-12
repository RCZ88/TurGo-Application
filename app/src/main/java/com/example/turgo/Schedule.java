package com.example.turgo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.UUID;

public class Schedule implements Serializable, RequireUpdate<Schedule, ScheduleFirebase> {
    private final FirebaseNode fbn = FirebaseNode.SCHEDULE;
    private final Class<ScheduleFirebase>fbc = ScheduleFirebase.class;
    public final String scheduleID;
    public static final String SERIALIZE_KEY_CODE = "scheduleObj";
    public static final LocalDate NEVER_SCHEDULED = LocalDate.of(1000, 1, 1);
    private int numberOfStudents;
    private boolean isPrivate;
    private boolean hasScheduled;
    private User scheduler;
    private LocalTime meetingStart;
    private LocalTime meetingEnd;
    private int duration;
    private DayOfWeek day;

    public Schedule(LocalTime meetingStart, int duration, DayOfWeek day,  boolean isPrivate){
        this.scheduleID = UUID.randomUUID().toString();
        this.meetingStart = meetingStart;
        this.duration = duration;
        this.day = day;
        this.meetingEnd = meetingStart.plus(Duration.ofMinutes(duration));
        this.isPrivate = isPrivate;
        this.hasScheduled = false;
        scheduler = null;
    }
    public LocalDate getNextMeetingDate(){
        DayOfWeek day = this.day;
        LocalDate today = LocalDate.now();
        LocalDate nextMeeting = today.with(TemporalAdjusters.nextOrSame(day)); //gets the nearest date of day
        if(nextMeeting.equals(today) && LocalTime.now().isAfter(meetingEnd)){
            nextMeeting = nextMeeting.plusWeeks(1);
        }
        return nextMeeting;
    }
    public Schedule(){scheduleID = "";}
    public boolean isPrivate(){
        return this.isPrivate;
    }
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
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

    public void getRoom(ObjectCallBack<Room>callBack) {
        try {
            findAggregatedObject( Room.class, "schedules", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public void getScheduleOfCourse(ObjectCallBack<Course> callBack) {
        try {
            findAggregatedObject( Course.class, "schedules", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public void getStudents(ObjectCallBack<ArrayList<Student>>callBack) {
        try {
            findAllAggregatedObjects(Student.class, "allSchedules", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }



    public boolean isHasScheduled() {
        return hasScheduled;
    }

    public void setHasScheduled(boolean hasScheduled) {
        this.hasScheduled = hasScheduled;
    }

    public User getScheduler() {
        return scheduler;
    }

    public void setScheduler(User scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<ScheduleFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return this.scheduleID;
    }

}
