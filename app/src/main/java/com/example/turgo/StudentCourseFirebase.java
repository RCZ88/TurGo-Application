package com.example.turgo;

import java.util.ArrayList;

public class StudentCourseFirebase implements FirebaseClass<StudentCourse> {
    private String StudentCourseID;
    private  String studentID;
    private String ofCourseID;
    private ArrayList<String> schedulesID;
    private boolean paymentPreferences;
    private boolean privateOrGroup;
    private ArrayList<String>taskIDs;
    private ArrayList<String>agendaIDs;
    private int pricePer;


    @Override
    public void importObjectData(StudentCourse from) {
        if (from == null) return;

        this.studentID = from.getStudent().getID();
        this.ofCourseID = from.getOfCourse().getID();
        this.schedulesID = convertToIdList(from.getSchedulesOfCourse());
        this.paymentPreferences = from.isPaymentPreferences();
        this.privateOrGroup = from.isPrivateOrGroup();
        this.taskIDs =
        this.agendaIDs = convertToIdList(from.getAgendas());
        this.pricePer = from.getPricePer();
    }

    @Override
    public String getID() {
        return "";
    }
}
