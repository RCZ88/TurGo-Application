package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;

public class AgendaFirebase implements FirebaseClass<Agenda> {
    private String agenda_ID;
    private String agendaID; // legacy alias
    private String agendaImage;
    private String date;
    private String contents;
    private String ofMeeting;
    private String teacher;
    private String student;
    private String ofCourse;

    // Default constructor required for Firebase
    public AgendaFirebase() {
    }

    @Override
    public void importObjectData(Agenda from) {
        agenda_ID = from.getID();
        agendaID = agenda_ID;

        if (from.getAgendaImage() != null) {
            agendaImage = from.getAgendaImage().getID();
        } else {
            agendaImage = "";
        }

        if (from.getDate() != null) {
            date = from.getDate().toString();
        }
        contents = from.getContents();
        if (from.getOfMeeting() != null) {
            ofMeeting = from.getOfMeeting().getID();
        }

        if (from.getTeacher() != null) {
            teacher = from.getTeacher().getID();
        }

        if (from.getStudent() != null) {
            student = from.getStudent().getID();
        }
        if(from.getOfCourseId() != null){
            ofCourse = from.getOfCourseId();
        }
    }

    @Override
    public String getID() {
        return Tool.boolOf(agenda_ID) ? agenda_ID : agendaID;
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
        this.agenda_ID = agendaID;
    }

    public String getAgenda_ID() {
        return agenda_ID;
    }

    public void setAgenda_ID(String agenda_ID) {
        this.agenda_ID = agenda_ID;
        this.agendaID = agenda_ID;
    }

    public String getAgendaImage() {
        return agendaImage;
    }

    public void setAgendaImage(String agendaImage) {
        this.agendaImage = agendaImage;
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
