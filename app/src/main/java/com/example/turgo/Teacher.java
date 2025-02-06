package com.example.turgo;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class Teacher extends User implements Serializable {
    private ArrayList<Course> coursesTeach;
    private ArrayList<String> courseTypeTeach;
    private ArrayList<Meeting> scheduledMeetings;
    private ArrayList<Agenda> agendas;
    public Teacher(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super("TEACHER", gender, fullName, birthDate, nickname, email, phoneNumber, "teaObj");
        scheduledMeetings = new ArrayList<>();
        coursesTeach = new ArrayList<>();
        courseTypeTeach = new ArrayList<>();
        agendas = new ArrayList<>();
    }
    public Teacher(){}

    public void createAgenda(String contents, LocalDate date, Meeting ofMeeting, Student student, Course ofCourse){
        Agenda agenda = new Agenda(contents, date, ofMeeting, this, student, ofCourse);
        this.agendas.add(agenda);
        student.addAgenda(agenda);
    }

    public void addCourse(Course course){
        coursesTeach.add(course);
    }
    public ArrayList<Course> getCoursesTeach(){
        return coursesTeach;
    }

    public void addCourseTeach(String courseType){
        courseTypeTeach.add(courseType);
    }

    public ArrayList<String> getCourseTypeTeach() {
        return courseTypeTeach;
    }

    public ArrayList<Meeting> getScheduledMeetings(){
        return scheduledMeetings;
    }

    public void addScheduledMeeting(Meeting meeting){
        scheduledMeetings.add(meeting);
    }
    @Override
    public String toString() {
        return super.toString() + "Teacher{" +
                "courseTypeTeach=" + courseTypeTeach +
                '}';
    }
}
