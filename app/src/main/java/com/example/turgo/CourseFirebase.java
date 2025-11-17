package com.example.turgo;

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
    private ArrayList<String> pricesIds;
    private ArrayList<String> dayTimeArrangementIds; //the days available for this course
    private ArrayList<String> studentsIds;
    private ArrayList<String> schedulesIds;
    private ArrayList<String>agendasIds;
    private ArrayList<String>studentsCourseIds;

    public CourseFirebase(){

    }

    @Override
    public void importObjectData(Course from) {
        courseID = from.getCourseID();
        courseName = from.getCourseName();
        courseTypeID = from.getCourseType().getID();
        courseDescription = from.getCourseDescription();
        teacherID = from.getTeacher().getUID();
        baseCost = from.getBaseCost();
        hourlyCost = from.getHourlyCost();
        monthlyDiscountPercentage = from.getMonthlyDiscountPercentage();
        paymentPer = from.getPaymentPer();
        privateGroup = from.getPrivateGroup();
        pricesIds = convertToIdList(from.getPrices());
        dayTimeArrangementIds = convertToIdList(from.getDayTimeArrangement());
        studentsIds = convertToIdList(from.getStudents());
        schedulesIds = convertToIdList(from.getSchedules());
        agendasIds = convertToIdList(from.getAgenda());
        studentsCourseIds = convertToIdList(from.getStudentsCourse());
    }

    @Override
    public String getID() {
        return courseID;
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

    public ArrayList<String> getPricesIds() {
        return pricesIds;
    }

    public void setPricesIds(ArrayList<String> pricesIds) {
        this.pricesIds = pricesIds;
    }

    public ArrayList<String> getDayTimeArrangementIds() {
        return dayTimeArrangementIds;
    }

    public void setDayTimeArrangementIds(ArrayList<String> dayTimeArrangementIds) {
        this.dayTimeArrangementIds = dayTimeArrangementIds;
    }

    public ArrayList<String> getStudentsIds() {
        return studentsIds;
    }

    public void setStudentsIds(ArrayList<String> studentsIds) {
        this.studentsIds = studentsIds;
    }

    public ArrayList<String> getSchedulesIds() {
        return schedulesIds;
    }

    public void setSchedulesIds(ArrayList<String> schedulesIds) {
        this.schedulesIds = schedulesIds;
    }

    public ArrayList<String> getAgendasIds() {
        return agendasIds;
    }

    public void setAgendasIds(ArrayList<String> agendasIds) {
        this.agendasIds = agendasIds;
    }

    public ArrayList<String> getStudentsCourseIds() {
        return studentsCourseIds;
    }

    public void setStudentsCourseIds(ArrayList<String> studentsCourseIds) {
        this.studentsCourseIds = studentsCourseIds;
    }

    public void setMonthlyDiscountPercentage(double monthlyDiscountPercentage) {
        this.monthlyDiscountPercentage = monthlyDiscountPercentage;
    }

    public Course convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Course) constructClass(Course.class, courseID);
    }

}
