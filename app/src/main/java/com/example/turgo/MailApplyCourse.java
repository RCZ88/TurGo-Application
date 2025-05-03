package com.example.turgo;

import java.util.ArrayList;

public class MailApplyCourse extends Mail{
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
}
