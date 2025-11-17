package com.example.turgo;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Agenda implements Serializable, RequireUpdate<Agenda, AgendaFirebase> {
    private final String agenda_ID;

    private static final FirebaseNode fbn = FirebaseNode.AGENDA;
    private static final Class<AgendaFirebase> fbc = AgendaFirebase.class;
    private RTDBManager<AgendaFirebase>agendaRTDB;
    private file agendaImage;
    private LocalDate date;
    private String contents;
    private Meeting ofMeeting;
    private Teacher teacher;
    private Student student;
    private Course ofCourse;

    public Agenda(String contents, LocalDate date, Meeting ofMeeting, Teacher teacher, Student student, Course ofCourse){
        this.contents = contents;
        this.date = date;
        this.ofMeeting = ofMeeting;
        this.teacher = teacher;
        this.student = student;
        this.ofCourse = ofCourse;
        agenda_ID = UUID.randomUUID().toString();
    }

    public Agenda(file file, LocalDate date, Meeting ofMeeting, Teacher teacher, Student student, Course ofCourse){
        agendaImage = file;
        this.date = date;
        this.ofMeeting = ofMeeting;
        this.teacher = teacher;
        this.student = student;
        this.ofCourse = ofCourse;
        agenda_ID = UUID.randomUUID().toString();
    }



    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Meeting getOfMeeting() {
        return ofMeeting;
    }

    public void setOfMeeting(Meeting ofMeeting) {
        this.ofMeeting = ofMeeting;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getOfCourse() {
        return ofCourse;
    }

    public void setOfCourse(Course ofCourse) {
        this.ofCourse = ofCourse;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }


    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<AgendaFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public String getID() {
        return agenda_ID;
    }
}
