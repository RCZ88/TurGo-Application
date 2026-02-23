package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class StudentCourseFirebase implements FirebaseClass<StudentCourse> {
    private String sc_ID;
    private ArrayList<String> schedulesOfCourse;
    private boolean paymentPreferences;
    private boolean privateOrGroup;
    private ArrayList<String>tasks;
    private ArrayList<String>agendas;
    private ArrayList<String> timeSlots;
    private String student;
    private int pricePer;

    public StudentCourseFirebase(){}

    @Override
    public void importObjectData(StudentCourse from) {
        if (from == null) return;

        this.sc_ID = from.getSc_ID();
        this.schedulesOfCourse = Tool.boolOf(from.getSchedulesOfCourse()) ? convertToIdList(from.getSchedulesOfCourse()) : new ArrayList<>();
        this.paymentPreferences = from.isPaymentPreferences();
        this.privateOrGroup = from.isPrivateOrGroup();
        this.tasks = Tool.boolOf(from.getTasks()) ? convertToIdList(from.getTasks()) : new ArrayList<>();
        this.agendas = Tool.boolOf(from.getAgendas()) ? convertToIdList(from.getAgendas()) : new ArrayList<>();
        this.timeSlots = Tool.boolOf(from.getTimeSlots()) ? convertToIdList(from.getTimeSlots()) : new ArrayList<>();
        this.pricePer = from.getPricePer();
        this.student = from.getStudentId();
    }

    @Override
    public String getID() {
        return sc_ID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<StudentCourse> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(StudentCourse.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((StudentCourse) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getSc_ID() {
        return sc_ID;
    }

    public void setSc_ID(String sc_ID) {
        this.sc_ID = sc_ID;
    }


    public ArrayList<String> getSchedulesOfCourse() {
        return schedulesOfCourse;
    }

    public void setSchedulesOfCourse(ArrayList<String> schedulesOfCourse) {
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

    public ArrayList<String> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<String> tasks) {
        this.tasks = tasks;
    }

    public ArrayList<String> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<String> agendas) {
        this.agendas = agendas;
    }

    public int getPricePer() {
        return pricePer;
    }

    public void setPricePer(int pricePer) {
        this.pricePer = pricePer;
    }

    public ArrayList<String> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(ArrayList<String> timeSlots) {
        this.timeSlots = timeSlots;
    }


}
