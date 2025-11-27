package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class CourseFirebase implements FirebaseClass<Course>{
    private String courseID;
    private String courseName;
    private String courseDescription;
    private String teacherID;
    private String courseTypeID;
    private double baseCost;
    private double hourlyCost;
    private double monthlyDiscountPercentage;

    private boolean [] paymentPer; //accept per month or and per meeting
    private boolean [] privateGroup; //accept private and or group?
    private ArrayList<String> prices;
    private ArrayList<String> dayTimeArrangement; //the days available for this course
    private ArrayList<String> students;
    private ArrayList<String> schedules;
    private ArrayList<String>agendas;
    private ArrayList<String>studentsCourse;

    public CourseFirebase(){

    }

    @Override
    public void importObjectData(Course from) {
        courseID = from.getCourseID();
        courseName = from.getCourseName();
        courseTypeID = from.getCourseType().getID();
        courseDescription = from.getCourseDescription();
        teacherID = from.getTeacher().getUid();
        baseCost = from.getBaseCost();
        hourlyCost = from.getHourlyCost();
        monthlyDiscountPercentage = from.getMonthlyDiscountPercentage();
        paymentPer = from.getPaymentPer();
        privateGroup = from.getPrivateGroup();
        prices = convertToIdList(from.getPrices());
        dayTimeArrangement = convertToIdList(from.getDayTimeArrangement());
        students = convertToIdList(from.getStudents());
        schedules = convertToIdList(from.getSchedules());
        agendas = convertToIdList(from.getAgenda());
        studentsCourse = convertToIdList(from.getStudentsCourse());
    }

    @Override
    public String getID() {
        return courseID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Course> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Course.class, courseID, new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Course) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }


    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public String getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(String teacherID) {
        this.teacherID = teacherID;
    }

    public double getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(double baseCost) {
        this.baseCost = baseCost;
    }

    public double getHourlyCost() {
        return hourlyCost;
    }

    public void setHourlyCost(double hourlyCost) {
        this.hourlyCost = hourlyCost;
    }

    public double getMonthlyDiscountPercentage() {
        return monthlyDiscountPercentage;
    }

    public String getCourseTypeID() {
        return courseTypeID;
    }

    public void setCourseTypeID(String courseTypeID) {
        this.courseTypeID = courseTypeID;
    }

    public boolean[] getPaymentPer() {
        return paymentPer;
    }

    public void setPaymentPer(boolean[] paymentPer) {
        this.paymentPer = paymentPer;
    }

    public boolean[] getPrivateGroup() {
        return privateGroup;
    }

    public void setPrivateGroup(boolean[] privateGroup) {
        this.privateGroup = privateGroup;
    }

    public ArrayList<String> getPrices() {
        return prices;
    }

    public void setPrices(ArrayList<String> prices) {
        this.prices = prices;
    }

    public ArrayList<String> getDayTimeArrangement() {
        return dayTimeArrangement;
    }

    public void setDayTimeArrangement(ArrayList<String> dayTimeArrangement) {
        this.dayTimeArrangement = dayTimeArrangement;
    }

    public ArrayList<String> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<String> students) {
        this.students = students;
    }

    public ArrayList<String> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<String> schedules) {
        this.schedules = schedules;
    }

    public ArrayList<String> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<String> agendas) {
        this.agendas = agendas;
    }

    public ArrayList<String> getStudentsCourse() {
        return studentsCourse;
    }

    public void setStudentsCourse(ArrayList<String> studentsCourse) {
        this.studentsCourse = studentsCourse;
    }

    public void setMonthlyDiscountPercentage(double monthlyDiscountPercentage) {
        this.monthlyDiscountPercentage = monthlyDiscountPercentage;
    }


}
