package com.example.turgo;

import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

public class Teacher extends User implements Serializable, RequireUpdate<Teacher, TeacherFirebase> {
    private static final FirebaseNode fbn = FirebaseNode.TEACHER;
    private static final int MAX_LATEST_SUBMISSION_SIZE = 3;
    private static final Class<TeacherFirebase> fbc = TeacherFirebase.class;
    public static final String SERIALIZE_KEY_CODE = "teacherObj";
    private String profileImageCloudinary;
    private ArrayList<Course> coursesTeach;
    private ArrayList<SubmissionDisplay> latestSubmission;
    private ArrayList<String> courseTypeTeach;
    private ArrayList<Meeting> scheduledMeetings;
    private ArrayList<Meeting> completedMeetings;
    private ArrayList<Agenda> agendas;
    private ArrayList<DayTimeArrangement> timeArrangements; //one object for each day of the week.
    private String teacherResume;
    private int teachYearExperience;
    public Teacher(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.TEACHER, gender, fullName, birthDate, nickname, email, phoneNumber);
        scheduledMeetings = new ArrayList<>();
        coursesTeach = new ArrayList<>();
        profileImageCloudinary = "https://res.cloudinary.com/daccry0jr/image/upload/v1761196379/islooktidmooszzfrga3.png";
        courseTypeTeach = new ArrayList<>();
        agendas = new ArrayList<>();
        completedMeetings = new ArrayList<>();
        latestSubmission = new ArrayList<>();
    }


    @Override
    public void updateUserDB() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        updateDB();
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<TeacherFirebase> getFirebaseClass() {
        return fbc;
    }

    @Override
    public String getID() {
        Log.d("Teacher", "getID: " + getUid());
        return getUid();
    }
    public void addLatestSubmission(SubmissionDisplay submission){
        latestSubmission.add(0, submission);
        if(latestSubmission.size() > MAX_LATEST_SUBMISSION_SIZE){
            latestSubmission.remove(latestSubmission.size() - 1);
        }
    }

    public ArrayList<SubmissionDisplay> getLatestSubmission() {
        return latestSubmission;
    }

    public void setLatestSubmission(ArrayList<SubmissionDisplay> latestSubmission) {
        this.latestSubmission = latestSubmission;
    }

    public String getProfileImageCloudinary() {
        return profileImageCloudinary;
    }

    public void setProfileImageCloudinary(String profileImageCloudinary) {
        this.profileImageCloudinary = profileImageCloudinary;
    }
    public ArrayList<Schedule>getAllSchedule(){
        ArrayList< Schedule> allSchedule = new ArrayList<>();
        for(DayTimeArrangement dta : timeArrangements){
            allSchedule.addAll(dta.getOccupied());
        }
        return allSchedule;
    }

    public static String getSerializeKeyCode() {
        return SERIALIZE_KEY_CODE;
    }

    public Class<TeacherFirebase> getFbc() {
        return fbc;
    }

    public FirebaseNode getFbn() {
        return fbn;
    }

    public Teacher(){}

    @Override
    public String getSerializeCode() {
        return SERIALIZE_KEY_CODE;
    }

    public void createAgenda(String contents, LocalDate date, Meeting ofMeeting, Student student, Course ofCourse) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        Agenda agenda = new Agenda(contents, date, ofMeeting, this, student, ofCourse);
        this.agendas.add(agenda);
        updateUserDB();
        student.addAgenda(agenda);
        student.getStudentCourseFromCourse(ofCourse);
        student.updateUserDB();
    }
    public void addTask(Task task, Course course) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        for(Student student : task.getStudentsAssigned()){
            student.getStudentCourseFromCourse(course).getTasks().add(task);
            student.updateUserDB();
        }

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

    public ArrayList<DayTimeArrangement> getTimeArrangements() {
        return timeArrangements;
    }

    public void setTimeArrangements(ArrayList<DayTimeArrangement> timeArrangements) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        this.timeArrangements = timeArrangements;
    }

    public void addDTA(DayTimeArrangement dta) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        timeArrangements.add(dta);
        updateUserDB();
    }

    public void setTeachYearExperience(int teachYearExperience) {
        this.teachYearExperience = teachYearExperience;
    }

    public String getProfileImage() {
        return profileImageCloudinary;
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

    public ArrayList<Meeting> getCompletedMeetings() {
        return completedMeetings;
    }

    public void setCompletedMeetings(ArrayList<Meeting> completedMeetings) {
        this.completedMeetings = completedMeetings;
    }

    public TeacherMini toTM(){
        ArrayList<String>courseNames = new ArrayList<>();
        for(Course course : coursesTeach){
            courseNames.add(course.getCourseName());
        }
        return new TeacherMini(this.getFullName(), String.join(", ", courseNames), this.getPfpCloudinary(), this.getID());
    }

    public ArrayList<Schedule> getSchedulesOfDay(DayOfWeek day){
        for(DayTimeArrangement dta : timeArrangements){
            if(dta.getDay() == day){
                return dta.getOccupied();
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return super.toString() + "Teacher{" +
                "courseTypeTeach=" + courseTypeTeach +
                '}';
    }


}
