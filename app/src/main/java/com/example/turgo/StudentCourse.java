package com.example.turgo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class StudentCourse implements Serializable, RequireUpdate<StudentCourse, StudentCourseFirebase> {
    private final FirebaseNode fbn = FirebaseNode.STUDENTCOURSE;
    private final Class<StudentCourseFirebase>fbc = StudentCourseFirebase.class;
    private final String sc_ID;
    private Student student;
    private Course ofCourse;
    private ArrayList<Schedule> schedulesOfCourse;
    private boolean paymentPreferences; //false: week | true: month
    private boolean privateOrGroup;
    private ArrayList<Task> tasks;
    private ArrayList<Agenda> agendas;
    private int pricePer;
    public StudentCourse(Student student, Course ofCourse, boolean paymentPreferences, boolean privateOrGroup, int pricePer){
        this.student = student;
        this.ofCourse = ofCourse;
        schedulesOfCourse = new ArrayList<>();
        this.paymentPreferences = paymentPreferences;
        this.privateOrGroup = privateOrGroup;
        tasks = new ArrayList<>();
        agendas = new ArrayList<>();
        this.pricePer = pricePer;
        sc_ID = UUID.randomUUID().toString();
    }

    public String getSc_ID() {
        return sc_ID;
    }

    public void addSchedule(Schedule schedule){
        schedulesOfCourse.add(schedule);
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

    public ArrayList<Schedule> getSchedulesOfCourse() {
        return schedulesOfCourse;
    }

    public void setSchedulesOfCourse(ArrayList<Schedule> schedulesOfCourse) {
        this.schedulesOfCourse = schedulesOfCourse;
    }

    public boolean isPaymentPreferences() {
        return paymentPreferences;
    }

    public void setPaymentPreferences(boolean paymentPreferences) {
        this.paymentPreferences = paymentPreferences;
    }

    public boolean isPrivateOrGroup() {
        return privateOrGroup;
    }

    public void setPrivateOrGroup(boolean privateOrGroup) {
        this.privateOrGroup = privateOrGroup;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<Agenda> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<Agenda> agendas) {
        this.agendas = agendas;
    }

    public int getPricePer() {
        return pricePer;
    }

    public void setPricePer(int pricePer) {
        this.pricePer = pricePer;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<StudentCourseFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public String getID() {
        return sc_ID;
    }
}
