package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DTAFirebase implements FirebaseClass<DayTimeArrangement>{ //day tim arrangement
    // Firebase-compatible fields
    private String DTA_ID;
    private String ofTeacherID; // Store Teacher ID instead of Teacher object
    private String atCourseID; // Store Course ID instead of Course object
    private String day; // Store DayOfWeek as String
    private String start; // Store LocalTime as String in "HH:mm" format
    private String end; // Store LocalTime as String in "HH:mm" format
    private ArrayList<String> occupiedIDs; // Store Schedule IDs instead of Schedule objects
    private int maxMeeting;

    // Default constructor required for Firebase
    public DTAFirebase() {
        occupiedIDs = new ArrayList<>();
    }

    @Override
    public void importObjectData(DayTimeArrangement from) {
        // Copy DTA ID
        DTA_ID = from.getID();

        // Convert object references to IDs
        if (from.getOfTeacher() != null) {
            ofTeacherID = from.getOfTeacher().getID();
        }

        if (from.getAtCourse() != null) {
            atCourseID = from.getAtCourse().getID();
        }

        // Convert DayOfWeek to String
        if (from.getDay() != null) {
            day = from.getDay().toString();
        }

        // Convert LocalTime to String in "HH:mm" format
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if (from.getStart() != null) {
            start = from.getStart().format(timeFormatter);
        }

        if (from.getEnd() != null) {
            end = from.getEnd().format(timeFormatter);
        }

        // Convert ArrayList<Schedule> to ArrayList<String> of IDs
        if (from.getOccupied() != null) {
            occupiedIDs = convertToIdList(from.getOccupied());
        }

        // Copy primitive field directly
        maxMeeting = from.getMaxMeeting();
    }

    @Override
    public String getID() {
        return DTA_ID;
    }

    @Override
    public DayTimeArrangement convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (DayTimeArrangement) constructClass(DayTimeArrangement.class, getID());
    }

    public String getDTA_ID() {
        return DTA_ID;
    }

    public void setDTA_ID(String DTA_ID) {
        this.DTA_ID = DTA_ID;
    }

    public String getOfTeacherID() {
        return ofTeacherID;
    }

    public void setOfTeacherID(String ofTeacherID) {
        this.ofTeacherID = ofTeacherID;
    }

    public String getAtCourseID() {
        return atCourseID;
    }

    public void setAtCourseID(String atCourseID) {
        this.atCourseID = atCourseID;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public ArrayList<String> getOccupiedIDs() {
        return occupiedIDs;
    }

    public void setOccupiedIDs(ArrayList<String> occupiedIDs) {
        this.occupiedIDs = occupiedIDs;
    }

    public int getMaxMeeting() {
        return maxMeeting;
    }

    public void setMaxMeeting(int maxMeeting) {
        this.maxMeeting = maxMeeting;
    }
}
