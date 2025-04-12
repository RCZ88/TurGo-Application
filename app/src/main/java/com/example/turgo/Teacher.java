package com.example.turgo;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;

public class Teacher extends User implements Serializable {
    private Drawable profileImage;
    private Context context;
    private ArrayList<Course> coursesTeach;
    private ArrayList<String> courseTypeTeach;
    private ArrayList<Meeting> scheduledMeetings;
    private ArrayList<Agenda> agendas;
    private ArrayList<DayTimeArrangement>availableTimes;
    private String teacherResume;
    private int teachYearExperience;
    public Teacher(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber, Context context) throws ParseException {
        super("TEACHER", gender, fullName, birthDate, nickname, email, phoneNumber, "teaObj");
        scheduledMeetings = new ArrayList<>();
        coursesTeach = new ArrayList<>();
        profileImage = ContextCompat.getDrawable(context, R.drawable.chalkboard_user);
        courseTypeTeach = new ArrayList<>();
        agendas = new ArrayList<>();
        this.context = context;
    }
    public Teacher(){}

    public void createAgenda(String contents, LocalDate date, Meeting ofMeeting, Student student, Course ofCourse){
        Agenda agenda = new Agenda(contents, date, ofMeeting, this, student, ofCourse);
        this.agendas.add(agenda);
        student.addAgenda(agenda);
    }

    public void setProfileImage(Drawable profileImage){
        this.profileImage = profileImage;
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

    public int getTeachYearExperience() {
        return teachYearExperience;
    }

    public ArrayList<DayTimeArrangement> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(ArrayList<DayTimeArrangement> availableTimes) {
        this.availableTimes = availableTimes;
    }

    public void setTeachYearExperience(int teachYearExperience) {
        this.teachYearExperience = teachYearExperience;
    }

    public Drawable getProfileImage() {
        return profileImage;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCoursesTeach(ArrayList<Course> coursesTeach) {
        this.coursesTeach = coursesTeach;
    }

    public void setCourseTypeTeach(ArrayList<String> courseTypeTeach) {
        this.courseTypeTeach = courseTypeTeach;
    }

    public void setScheduledMeetings(ArrayList<Meeting> scheduledMeetings) {
        this.scheduledMeetings = scheduledMeetings;
    }

    public ArrayList<Agenda> getAgendas() {
        return agendas;
    }

    public void setAgendas(ArrayList<Agenda> agendas) {
        this.agendas = agendas;
    }

    public String getTeacherResume() {
        return teacherResume;
    }

    public void setTeacherResume(String teacherResume) {
        this.teacherResume = teacherResume;
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
