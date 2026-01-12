package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DTAFirebase implements FirebaseClass<DayTimeArrangement>{ //day tim arrangement
    // Firebase-compatible fields
    private String DTA_ID;
    private String ofTeacher; // Store Teacher ID instead of Teacher object
    private String atCourse; // Store Course ID instead of Course object
    private String day; // Store DayOfWeek as String
    private String start; // Store LocalTime as String in "HH:mm" format
    private String end; // Store LocalTime as String in "HH:mm" format
    private ArrayList<String> occupied; // Store Schedule IDs instead of Schedule objects
    private int maxMeeting;

    // Default constructor required for Firebase
    public DTAFirebase() {
        occupied = new ArrayList<>();
    }

    @Override
    public void importObjectData(DayTimeArrangement from) {
        // Copy DTA ID
        DTA_ID = from.getID();

        // Convert object references to IDs
        from.getOfTeacher(new ObjectCallBack<Teacher>() {
            @Override
            public void onObjectRetrieved(Teacher object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                if(object != null){
                    ofTeacher = object.getID();
                }
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
        from.getAtCourse(new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(Course object) {
                if (object != null) {
                    atCourse = object.getID();
                }
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });

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
            occupied = convertToIdList(from.getOccupied());
        }

        // Copy primitive field directly
        maxMeeting = from.getMaxMeeting();
    }

    @Override
    public String getID() {
        return DTA_ID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<DayTimeArrangement> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(DayTimeArrangement.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((DayTimeArrangement) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getDTA_ID() {
        return DTA_ID;
    }

    public void setDTA_ID(String DTA_ID) {
        this.DTA_ID = DTA_ID;
    }

    public String getOfTeacher() {
        return ofTeacher;
    }

    public void setOfTeacher(String ofTeacher) {
        this.ofTeacher = ofTeacher;
    }

    public String getAtCourse() {
        return atCourse;
    }

    public void setAtCourse(String atCourse) {
        this.atCourse = atCourse;
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

    public ArrayList<String> getOccupied() {
        return occupied;
    }

    public void setOccupied(ArrayList<String> occupied) {
        this.occupied = occupied;
    }

    public int getMaxMeeting() {
        return maxMeeting;
    }

    public void setMaxMeeting(int maxMeeting) {
        this.maxMeeting = maxMeeting;
    }
}
