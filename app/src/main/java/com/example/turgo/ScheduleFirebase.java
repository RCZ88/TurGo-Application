package com.example.turgo;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class ScheduleFirebase implements FirebaseClass<Schedule>{
    private String scheduleID;
    private String courseID;
    private int numberOfStudents;
    private ArrayList<String> studentIDs;
    private boolean isPrivate;
    private boolean hasScheduled;
    private String schedulerID;
    private String meetingStart; // Format: "HH:mm"
    private String meetingEnd;   // Format: "HH:mm"
    private int duration;
    private String day; // e.g., "MONDAY"
    private String roomID;

    public ScheduleFirebase(String scheduleID, String courseID, int numberOfStudents, ArrayList<String> studentIDs, boolean isPrivate, boolean hasScheduled, String schedulerID, String meetingStart, String meetingEnd, int duration, String day, String roomID) {
        this.scheduleID = scheduleID;
        this.courseID = courseID;
        this.numberOfStudents = numberOfStudents;
        this.studentIDs = studentIDs;
        this.isPrivate = isPrivate;
        this.hasScheduled = hasScheduled;
        this.schedulerID = schedulerID;
        this.meetingStart = meetingStart;
        this.meetingEnd = meetingEnd;
        this.duration = duration;
        this.day = day;
        this.roomID = roomID;
    }



    @Override
    public void importObjectData(Schedule from) {
        scheduleID = from.getScheduleID();
        courseID = from.getScheduleOfCourse().getCourseID();
        numberOfStudents = from.getNumberOfStudents();
        studentIDs = convertToIdList(from.getStudents());
        isPrivate = from.isPrivate();
        hasScheduled = from.isHasScheduled();
        scheduleID = from.getScheduler().getUID();
        meetingStart = from.getMeetingStart().toString();
        meetingEnd = from.getMeetingEnd().toString();
        duration = from.getDuration();
        day = from.getDay().toString();
        roomID = from.getRoom().getID();
    }

    @Override
    public String getID() {
        return scheduleID;
    }

    @Override
    public Schedule convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Schedule) constructClass(Schedule.class, scheduleID);
    }

}
