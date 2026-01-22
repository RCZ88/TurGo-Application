package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class CourseFirebase implements FirebaseClass<Course>{
    private String courseID;
    private String courseName;
    private String courseDescription;
    private String logoCloudinary;
    private String backgroundCloudinary;
    private String courseType;
    private double baseCost;
    private double hourlyCost;
    private double monthlyDiscountPercentage;
    private boolean autoAcceptStudent;
    private int maxStudentPerMeeting;


    private ArrayList<Integer> paymentPer; //accept per month or and per meeting
    private ArrayList<Integer> privateGroup; //accept private and or group?
    private ArrayList<String> dayTimeArrangement; //the days available for this course
    private ArrayList<String> schedules;
    private ArrayList<String>agendas;
    private ArrayList<String>studentsCourse;
    private ArrayList<String> imagesCloudinary;

    public CourseFirebase(){

    }

    @Override
    public void importObjectData(Course from) {
        courseID = from.getCourseID();
        courseName = from.getCourseName();
        courseType = from.getCourseType().getID();
        baseCost = from.getBaseCost();
        hourlyCost = from.getHourlyCost();
        monthlyDiscountPercentage = from.getMonthlyDiscountPercentage();
        paymentPer = convertBooleanToInt(from.getPaymentPer());
        privateGroup = convertBooleanToInt(from.getPrivateGroup());
        dayTimeArrangement = convertToIdList(from.getDayTimeArrangement());
        schedules = convertToIdList(from.getSchedules());
        agendas = convertToIdList(from.getAgenda());
        studentsCourse = convertToIdList(from.getStudentsCourse());
        backgroundCloudinary = from.getBackgroundCloudinary();
        logoCloudinary = from.getLogoCloudinary();
        maxStudentPerMeeting = from.getMaxStudentPerMeeting();
        autoAcceptStudent = from.isAutoAcceptStudent();
        imagesCloudinary = from.getImagesCloudinary();
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

    public ArrayList<String> getImagesCloudinary() {
        return imagesCloudinary;
    }

    public void setImagesCloudinary(ArrayList<String> imagesCloudinary) {
        this.imagesCloudinary = imagesCloudinary;
    }

    public int getMaxStudentPerMeeting() {
        return maxStudentPerMeeting;
    }

    public void setMaxStudentPerMeeting(int maxStudentPerMeeting) {
        this.maxStudentPerMeeting = maxStudentPerMeeting;
    }

    public boolean isAutoAcceptStudent() {
        return autoAcceptStudent;
    }

    public void setAutoAcceptStudent(boolean autoAcceptStudent) {
        this.autoAcceptStudent = autoAcceptStudent;
    }

    public String getLogoCloudinary() {
        return logoCloudinary;
    }

    public void setLogoCloudinary(String logoCloudinary) {
        this.logoCloudinary = logoCloudinary;
    }

    public String getBackgroundCloudinary() {
        return backgroundCloudinary;
    }

    public void setBackgroundCloudinary(String backgroundCloudinary) {
        this.backgroundCloudinary = backgroundCloudinary;
    }

    public ArrayList<Integer> getPaymentPer() {
        return paymentPer;
    }

    public void setPaymentPer(ArrayList<Integer> paymentPer) {
        this.paymentPer = paymentPer;
    }

    public ArrayList<Integer> getPrivateGroup() {
        return privateGroup;
    }

    public void setPrivateGroup(ArrayList<Integer> privateGroup) {
        this.privateGroup = privateGroup;
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

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

//    public boolean[] getPaymentPer() {
//        return paymentPer;
//    }
//
//    public void setPaymentPer(boolean[] paymentPer) {
//        this.paymentPer = paymentPer;
//    }
//
//    public boolean[] getPrivateGroup() {
//        return privateGroup;
//    }
//
//    public void setPrivateGroup(boolean[] privateGroup) {
//        this.privateGroup = privateGroup;
//    }


    public ArrayList<String> getDayTimeArrangement() {
        return dayTimeArrangement;
    }

    public void setDayTimeArrangement(ArrayList<String> dayTimeArrangement) {
        this.dayTimeArrangement = dayTimeArrangement;
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
