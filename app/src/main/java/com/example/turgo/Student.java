package com.example.turgo;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;

public class Student extends User implements Serializable {
    private ArrayList<Course> courseTaken;
    private ArrayList<String> courseInterested; //coursetype
    private ArrayList<Course> courseRelated; //courses related to the coursetype
    private ArrayList<Meeting> preScheduledMeetings;
    private ArrayList<Meeting> meetingHistory;
    private ArrayList<Schedule> allSchedules;
    private LocalDate lastScheduled;
    private int autoSchedule;
    public Student(String fullName, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super("STUDENT", fullName, birthDate, nickname, email, phoneNumber, "stuOBJ");
        courseTaken = new ArrayList<>();
        courseInterested = new ArrayList<>();
        courseRelated = new ArrayList<>();
        preScheduledMeetings = new ArrayList<>();
        allSchedules = new ArrayList<>();
        meetingHistory = new ArrayList<>();
        autoSchedule = 1;
        findCourseRelated();
    }
    public Student(){}

    public void attendMeeting(Meeting meeting){
        meetingHistory.add(meeting);
        meeting.addStudentAttendance(this);
    }
    public void addScheduledMeeting(){
        LocalDate today = LocalDate.now();
        for(int i = 0; i<autoSchedule; i++){
            for(int j = 0; j<allSchedules.size(); j++){
                LocalDate meetingDate= today.plusWeeks(i-1).with(TemporalAdjusters.nextOrSame(allSchedules.get(j).getDay()));
                preScheduledMeetings.add(new Meeting(allSchedules.get(j), new ArrayList<>(), meetingDate));
            }
        }
        lastScheduled = today;
    }
    private ArrayList<Schedule> getAllSchedule(){
        ArrayList<Schedule>schedules = new ArrayList<>();
        for(Course course : courseTaken){
            schedules.addAll(course.getSchedules());
        }
        //sort here
        Schedule []scheduleArr = new Schedule[schedules.size()];
        for(int i = 0; i<schedules.size(); i++){
            scheduleArr[i] = schedules.get(i);
        }
        schedules = new ArrayList<>(Arrays.asList(sortSchedule(scheduleArr)));
        return schedules;
    }
    private Schedule[] sortSchedule(Schedule[] scheduleArr){
        int len = scheduleArr.length;
        for(int i = 0; i<len-1; i++){
            Schedule s = scheduleArr[i];
            for(int j = i+1; j<len; j++){
                if(scheduleArr[j].getDay().ordinal() > scheduleArr[j+1].getDay().ordinal()){
                    Schedule temp = scheduleArr[j];
                    scheduleArr[j] = scheduleArr[j+1];
                    scheduleArr[j+1] = temp;
                }
            }
        }
        return scheduleArr;
    }
    private boolean checkAllComplete(){
        if(preScheduledMeetings.isEmpty()){
            return true;
        }
        for(Meeting meeting : preScheduledMeetings){
            if(!meeting.isCompleted()){
                return false;
            }
        }
        return true;
    }
    private void findCourseRelated(){
        for(String ci: courseInterested){
            for(Course course : CoursesData.getCourses()){
                if(course.getCourseName().equals(ci)){
                    courseRelated.add(course);
                }
            }
        }
    }
    public void joinCourse(Course course){
        courseTaken.add(course);
        course.addStudent(this);
        allSchedules = getAllSchedule();
        addScheduledMeeting();
    }
    public ArrayList<Course> getCourseTaken(){
        return courseTaken;
    }

    public void addCourseInterest(String courseType){
        courseInterested.add(courseType);
    }

    public void setCourseTaken(ArrayList<Course> courseTaken) {
        this.courseTaken = courseTaken;
    }

    public ArrayList<String> getCourseInterested() {
        return courseInterested;
    }

    public void setCourseInterested(ArrayList<String> courseInterested) {
        this.courseInterested = courseInterested;
    }

    public ArrayList<Course> getCourseRelated() {
        return courseRelated;
    }

    public void setCourseRelated(ArrayList<Course> courseRelated) {
        this.courseRelated = courseRelated;
    }

    public ArrayList<Meeting> getPreScheduledMeetings() {
        return preScheduledMeetings;
    }

    public void setPreScheduledMeetings(ArrayList<Meeting> preScheduledMeetings) {
        this.preScheduledMeetings = preScheduledMeetings;
    }

    public ArrayList<Meeting> getMeetingHistory() {
        return meetingHistory;
    }

    public void setMeetingHistory(ArrayList<Meeting> meetingHistory) {
        this.meetingHistory = meetingHistory;
    }

    public ArrayList<Schedule> getAllSchedules() {
        return allSchedules;
    }

    public void setAllSchedules(ArrayList<Schedule> allSchedules) {
        this.allSchedules = allSchedules;
    }

    public LocalDate getLastScheduled() {
        return lastScheduled;
    }

    public void setLastScheduled(LocalDate lastScheduled) {
        this.lastScheduled = lastScheduled;
    }

    public int getAutoSchedule() {
        return autoSchedule;
    }

    public void setAutoSchedule(int autoSchedule) {
        this.autoSchedule = autoSchedule;
    }

    @Override
    public String toString() {
        return super.toString() + "Student{" +
                "courseInterested=" + courseInterested +
                '}';
    }
}
