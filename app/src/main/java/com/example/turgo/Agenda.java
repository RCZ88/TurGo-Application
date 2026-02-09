package com.example.turgo;


import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Agenda implements Serializable, RequireUpdate<Agenda, AgendaFirebase, AgendaRepository> {
    private final String agenda_ID;
    public static final String SERIALIZE_KEY_CODE = "agendas";

    private static final FirebaseNode fbn = FirebaseNode.AGENDA;
    private static final Class<AgendaFirebase> fbc = AgendaFirebase.class;
    private RTDBManager<AgendaFirebase>agendaRTDB;
    private file agendaImage;
    private LocalDate date;
    private String contents;
    private Meeting ofMeeting;
    private Teacher teacher;
    private Student student;
    private String ofCourse;

    public Agenda(String contents, LocalDate date, Meeting ofMeeting, Teacher teacher, Student student, String ofCourse){
        this.contents = contents;
        this.date = date;
        this.ofMeeting = ofMeeting;
        this.teacher = teacher;
        this.student = student;
        agenda_ID = UUID.randomUUID().toString();
        this.ofCourse = ofCourse;
    }
    public Agenda(){
        agenda_ID = "";
    }

    public Agenda(file file, LocalDate date, Meeting ofMeeting, Teacher teacher, Student student, String ofCourse){
        agendaImage = file;
        this.date = date;
        this.ofMeeting = ofMeeting;
        this.teacher = teacher;
        this.student = student;
        agenda_ID = UUID.randomUUID().toString();
        this.ofCourse = ofCourse;
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

    public void getOfCourse(ObjectCallBack<Course> callBack) {
        try {
            findAggregatedObject(Course.class, "agendas", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
    public Task<Course>getOfCourse(){
        CourseRepository courseRepository = new CourseRepository(ofCourse);
        return courseRepository.loadAsNormal();
    }
    public String getOfCourseId(){
        return ofCourse;
    }

    public void setOfCourse(String ofCourse) {
        this.ofCourse = ofCourse;
    }

    public file getAgendaImage() {
        return agendaImage;
    }

    public void setAgendaImage(file agendaImage) {
        this.agendaImage = agendaImage;
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
    public Class<AgendaRepository> getRepositoryClass() {
        return AgendaRepository.class;
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
