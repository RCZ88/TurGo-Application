package com.example.turgo;

import java.util.ArrayList;

public class StudentCourse {
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
}
