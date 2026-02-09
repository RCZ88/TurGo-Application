package com.example.turgo;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class ScheduleFirebase implements FirebaseClass<Schedule>{
    private String scheduleID;
    private int numberOfStudents;
    private boolean isPrivate;
    private boolean hasScheduled;
    private String scheduler;
    private String meetingStart; // Format: "HH:mm"
    private String meetingEnd;   // Format: "HH:mm"
    private int duration;
    private String day; // e.g., "MONDAY"
    private String ofCourse;
    private String room;

    public ScheduleFirebase(String scheduleID, String scheduleOfCourse, int numberOfStudents, ArrayList<String> students, boolean isPrivate, boolean hasScheduled, String scheduler, String meetingStart, String meetingEnd, int duration, String day, String room) {
        this.scheduleID = scheduleID;
        this.numberOfStudents = numberOfStudents;
        this.isPrivate = isPrivate;
        this.hasScheduled = hasScheduled;
        this.scheduler = scheduler;
        this.meetingStart = meetingStart;
        this.meetingEnd = meetingEnd;
        this.duration = duration;
        this.day = day;
    }
    public ScheduleFirebase(){}



    @Override
    public void importObjectData(Schedule from) {
        this.scheduleID = from.getScheduleID();

        // ✅ Tool.boolOf() for ALL booleans
        this.numberOfStudents = Tool.boolOf(from.getNumberOfStudents()) ? 1 : 0;  // Assume non-null=1
        this.isPrivate = Tool.boolOf(from.isPrivate());
        this.hasScheduled = Tool.boolOf(from.isHasScheduled());

        // ✅ Objects: Tool + safe chaining
        this.scheduler = Tool.boolOf(from.getScheduler()) ? from.getScheduler().getUid() : "";

        this.meetingStart = Tool.boolOf(from.getMeetingStart()) ? from.getMeetingStart().toString() : "00:00";
        this.meetingEnd = Tool.boolOf(from.getMeetingEnd()) ? from.getMeetingEnd().toString() : "23:59";

        this.duration = Tool.boolOf(from.getDuration()) ? from.getDuration() : 0;
        this.day = Tool.boolOf(from.getDay()) ? from.getDay().toString() : "MONDAY";
        this.ofCourse = Tool.boolOf(from.getOfCourse()) ? from.getOfCourse() :  "";
        this.room = Tool.boolOf(from.getRoomId()) ? from.getRoomId() : "";
    }


    @Override
    public String getID() {
        return scheduleID;
    }
    @Override
    public void convertToNormal(ObjectCallBack<Schedule> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Schedule.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Schedule) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(String scheduleID) {
        this.scheduleID = scheduleID;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isHasScheduled() {
        return hasScheduled;
    }

    public void setHasScheduled(boolean hasScheduled) {
        this.hasScheduled = hasScheduled;
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public String getMeetingStart() {
        return meetingStart;
    }

    public void setMeetingStart(String meetingStart) {
        this.meetingStart = meetingStart;
    }

    public String getMeetingEnd() {
        return meetingEnd;
    }

    public void setMeetingEnd(String meetingEnd) {
        this.meetingEnd = meetingEnd;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
