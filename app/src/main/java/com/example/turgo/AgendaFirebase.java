package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;

public class AgendaFirebase implements FirebaseClass<Agenda> {
    // Firebase-compatible fields
    private String agendaID;
    private String date; // Store LocalDate as String in ISO format (yyyy-MM-dd)
    private String contents;
    private String ofMeeting; // Store Meeting ID instead of Meeting object
    private String teacher; // Store Teacher ID instead of Teacher object
    private String student; // Store Student ID instead of Student object
    private String ofCourse; // Store Course ID instead of Course object

    // Default constructor required for Firebase
    public AgendaFirebase() {
    }

    @Override
    public void importObjectData(Agenda from) {
        // Copy agenda ID
        agendaID = from.getID();

        // Convert LocalDate to String
        if (from.getDate() != null) {
            date = from.getDate().toString(); // ISO format: yyyy-MM-dd
        }

        // Copy contents directly
        contents = from.getContents();

        // Convert object references to IDs
        if (from.getOfMeeting() != null) {
            ofMeeting = from.getOfMeeting().getID();
        }

        if (from.getTeacher() != null) {
            teacher = from.getTeacher().getID();
        }

        if (from.getStudent() != null) {
            student = from.getStudent().getID();
        }

        if (from.getOfCourse() != null) {
            ofCourse = from.getOfCourse().getID();
        }
    }

    @Override
    public String getID() {
        return agendaID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Agenda> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Agenda.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Agenda) object);

            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }


    public void setAgendaID(String agendaID) {
        this.agendaID = agendaID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getOfMeeting() {
        return ofMeeting;
    }

    public void setOfMeeting(String ofMeeting) {
        this.ofMeeting = ofMeeting;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getOfCourse() {
        return ofCourse;
    }

    public void setOfCourse(String ofCourse) {
        this.ofCourse = ofCourse;
    }
}