package com.example.turgo;

import com.google.firebase.database.DatabaseReference;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class MailApplyCourse extends Mail implements RequireUpdate<Mail, MailFirebase>{
    private ArrayList<Schedule> schedules;
    private Course course;
    private String reasonForJoining;
    private String school;
    private String grade;
    public MailApplyCourse(User from, User to, ArrayList<Schedule>schedules, Course course, String reasonForJoining, String school, String grade) {
        super(from, to);
        this.schedules = schedules;
        this.course = course;
        this.reasonForJoining = reasonForJoining;
        this.school = school;
        this.grade = grade;
        super.setHeader("Course Application Request - " + from);
        String body = "Dear " + course.getTeacher().getNickname() +", \nI hope this message finds you well.\nMy name is" + from.getNickname() + " and I am currently a student at" + school + ", in " + grade + ". I am interested in joining your course and would like to express my enthusiasm for the opportunity.";
        super.setBody(body);
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getReasonForJoining() {
        return reasonForJoining;
    }

    public void setReasonForJoining(String reasonForJoining) {
        this.reasonForJoining = reasonForJoining;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public DatabaseReference getDBRef(String ID) {
        return super.getDBRef(ID);
    }

    @Override
    public void retrieveOnce(ObjectCallBack<MailFirebase> ocb, String ID) {
        super.retrieveOnce(ocb, ID);
    }

    @Override
    public ArrayList<MailFirebase> retrieveListFromUser(String userID, String listName, ObjectCallBack<ArrayList<MailFirebase>> ocb) {
        return super.retrieveListFromUser(userID, listName, ocb);
    }

    @Override
    public void toggleRealtime(String ID, MailFirebase[] mutableObject) {
        super.toggleRealtime(ID, mutableObject);
    }
    @Override
    public FirebaseNode getFirebaseNode() {
        return FirebaseNode.MAIL_APPLY_COURSE;
    }
}
