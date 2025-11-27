package com.example.turgo;
import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class ScheduleFirebase implements FirebaseClass<Schedule>{
    private String scheduleID;
    private String scheduleOfCourse;
    private int numberOfStudents;
    private ArrayList<String> students;
    private boolean isPrivate;
    private boolean hasScheduled;
    private String scheduler;
    private String meetingStart; // Format: "HH:mm"
    private String meetingEnd;   // Format: "HH:mm"
    private int duration;
    private String day; // e.g., "MONDAY"
    private String room;

    public ScheduleFirebase(String scheduleID, String scheduleOfCourse, int numberOfStudents, ArrayList<String> students, boolean isPrivate, boolean hasScheduled, String scheduler, String meetingStart, String meetingEnd, int duration, String day, String room) {
        this.scheduleID = scheduleID;
        this.scheduleOfCourse = scheduleOfCourse;
        this.numberOfStudents = numberOfStudents;
        this.students = students;
        this.isPrivate = isPrivate;
        this.hasScheduled = hasScheduled;
        this.scheduler = scheduler;
        this.meetingStart = meetingStart;
        this.meetingEnd = meetingEnd;
        this.duration = duration;
        this.day = day;
        this.room = room;
    }



    @Override
    public void importObjectData(Schedule from) {
        scheduleID = from.getScheduleID();
        scheduleOfCourse = from.getScheduleOfCourse().getCourseID();
        numberOfStudents = from.getNumberOfStudents();
        students = convertToIdList(from.getStudents());
        isPrivate = from.isPrivate();
        hasScheduled = from.isHasScheduled();
        scheduler = from.getScheduler().getUid();
        meetingStart = from.getMeetingStart().toString();
        meetingEnd = from.getMeetingEnd().toString();
        duration = from.getDuration();
        day = from.getDay().toString();
        room = from.getRoom().getID();
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

}
