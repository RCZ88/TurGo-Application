package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;

public class AgendaFirebase implements FirebaseClass<Agenda> {
    // Firebase-compatible fields
    private String agendaID;
    private String date; // Store LocalDate as String in ISO format (yyyy-MM-dd)
    private String contents;
    private String ofMeetingID; // Store Meeting ID instead of Meeting object
    private String teacherID; // Store Teacher ID instead of Teacher object
    private String studentID; // Store Student ID instead of Student object
    private String ofCourseID; // Store Course ID instead of Course object

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
            ofMeetingID = from.getOfMeeting().getID();
        }

        if (from.getTeacher() != null) {
            teacherID = from.getTeacher().getID();
        }

        if (from.getStudent() != null) {
            studentID = from.getStudent().getID();
        }

        if (from.getOfCourse() != null) {
            ofCourseID = from.getOfCourse().getID();
        }
    }

    @Override
    public String getID() {
        return agendaID;
    }

    @Override
    public Agenda convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Agenda) constructClass(Agenda.class, getID());
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

    public String getOfMeetingID() {
        return ofMeetingID;
    }

    public void setOfMeetingID(String ofMeetingID) {
        this.ofMeetingID = ofMeetingID;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getOfCourseID() {
        return ofCourseID;
    }

    public void setOfCourseID(String ofCourseID) {
        this.ofCourseID = ofCourseID;
    }
}