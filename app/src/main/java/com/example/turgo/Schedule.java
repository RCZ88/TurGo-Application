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
    public static String SERIALIZE_KEY_CODE = "scheduleObj";
    private Course scheduleOfCourse;
    private int numberOfStudents;
    private ArrayList<Student> students;
    private boolean isPrivate;
    private boolean hasScheduled;
    private User scheduler;
    private LocalTime meetingStart;
    private LocalTime meetingEnd;
    private int duration;
    private DayOfWeek day;
    private Room room;

    public Schedule(Course scheduleOfCourse, LocalTime meetingStart, int duration, DayOfWeek day, Room room, boolean isPrivate){
        this.scheduleID = UUID.randomUUID().toString();
        this.scheduleOfCourse = scheduleOfCourse;
        this.meetingStart = meetingStart;
        this.duration = duration;
        this.day = day;
        this.meetingEnd = meetingStart.plus(Duration.ofMinutes(duration));
        this.room = room;
        this.isPrivate = isPrivate;
        this.students = new ArrayList<>();
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

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Course getScheduleOfCourse() {
        return scheduleOfCourse;
    }

    public void setScheduleOfCourse(Course scheduleOfCourse) {
        this.scheduleOfCourse = scheduleOfCourse;
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

    public ArrayList<Student> getStudents() {
        return students;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void setStudents(ArrayList<Student> students) {
        this.students = students;
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
