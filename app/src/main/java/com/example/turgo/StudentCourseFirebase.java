package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class StudentCourseFirebase implements FirebaseClass<StudentCourse> {
    private String sc_ID;
    private String student;
    private String ofCourse;
    private ArrayList<String> schedulesOfCourse;
    private boolean paymentPreferences;
    private boolean privateOrGroup;
    private ArrayList<String>tasks;
    private ArrayList<String>agendas;
    private int pricePer;


    @Override
    public void importObjectData(StudentCourse from) {
        if (from == null) return;

        this.sc_ID = from.getSc_ID();
        this.student = from.getStudent().getID();
        this.ofCourse = from.getOfCourse().getID();
        this.schedulesOfCourse = convertToIdList(from.getSchedulesOfCourse());
        this.paymentPreferences = from.isPaymentPreferences();
        this.privateOrGroup = from.isPrivateOrGroup();
        this.tasks = convertToIdList(from.getTasks());
        this.agendas = convertToIdList(from.getAgendas());
        this.pricePer = from.getPricePer();
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
}
