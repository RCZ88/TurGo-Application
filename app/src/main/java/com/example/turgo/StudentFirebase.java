package com.example.turgo;

import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

public class StudentFirebase extends UserFirebase implements FirebaseClass<Student>{

    private double percentageCompleted;
    private String nextMeetingID;
    private LocalDate lastScheduled;
    private int autoSchedule;
    private int notificationEarlyDuration;
    private int lateAttendance;
    private int lateSubmissions;

    private ArrayList<String> courseTakenIds;
    private ArrayList<String> studentCourseTakenIds;
    private ArrayList<String> courseInterested; // already a list of types (Strings)
    private ArrayList<String> courseRelatedIds;
    private ArrayList<String> preScheduledMeetingIds;
    private ArrayList<String> meetingHistoryIds;
    private ArrayList<String> scheduleCompletedThisWeekIds;
    private ArrayList<String> allScheduleIds;
    private ArrayList<String> allTaskIds;
    private ArrayList<String> uncompletedTaskIds;
    private ArrayList<String> allAgendaIds;

    public StudentFirebase(){
        super(UserType.STUDENT.type());
    }
    public void importObjectData(Student from) {
        // Basic fields
        setID(from.getID());
        setFullName(from.getFullName());
        setNickname(from.getNickname());
        setBirthdate(from.getBirthDate());
        setAge(from.getAge());
        setEmail(from.getEmail());
        setGender(from.getGender());
        setPhoneNumber(from.getPhoneNumber());
        setLanguangeID(from.getLanguage().getDisplayName());
        setTheme(from.getTheme().getTheme());
        setInboxIDs(convertToIdList(from.getInbox()));
        setOutboxIDs(convertToIdList(from.getOutbox()));
        setNotificationIDs(convertToIdList(from.getNotifications()));

        // Progress / scheduling
        percentageCompleted = from.getPercentageCompleted();
        nextMeetingID = (from.getNextMeeting() != null) ? from.getNextMeeting().getID() : null;
        lastScheduled = from.getLastScheduled();
        autoSchedule = from.getAutoSchedule();
        notificationEarlyDuration = (from.getNotificationEarlyDuration() != null)
                ? (int) from.getNotificationEarlyDuration().toMinutes()
                : 0;

        // Penalties / counts
        lateAttendance = from.getLateAttendance();
        lateSubmissions = from.getLateSubmissions();

        // Lists -> ID lists (use convertToIdList helper for object lists)
        courseTakenIds = convertToIdList(from.getCourseTaken());
        studentCourseTakenIds = convertToIdList(from.getStudentCourseTaken());
        courseInterested = from.getCourseInterested(); // already a list of strings
        courseRelatedIds = convertToIdList(from.getCourseRelated());
        preScheduledMeetingIds = convertToIdList(from.getPreScheduledMeetings());
        meetingHistoryIds = convertToIdList(from.getMeetingHistory());
        scheduleCompletedThisWeekIds = convertToIdList(from.getScheduleCompletedThisWeek());
        allScheduleIds = convertToIdList(from.getAllSchedules());
        allTaskIds = convertToIdList(from.getAllTask());
        uncompletedTaskIds = convertToIdList(from.getUncompletedTask());
        allAgendaIds = convertToIdList(from.getAllAgendas());
    }

    @Override
    public String getID() {
        return super.getID();
    }
    @Override
    public Student convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Student) constructClass(Student.class, super.getID());
    }


    public double getPercentageCompleted() {
        return percentageCompleted;
    }

    public void setPercentageCompleted(double percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public String getNextMeetingID() {
        return nextMeetingID;
    }

    public void setNextMeetingID(String nextMeetingID) {
        this.nextMeetingID = nextMeetingID;
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

    public int getNotificationEarlyDuration() {
        return notificationEarlyDuration;
    }

    public void setNotificationEarlyDuration(int notificationEarlyDuration) {
        this.notificationEarlyDuration = notificationEarlyDuration;
    }

    public int getLateAttendance() {
        return lateAttendance;
    }

    public void setLateAttendance(int lateAttendance) {
        this.lateAttendance = lateAttendance;
    }

    public int getLateSubmissions() {
        return lateSubmissions;
    }

    public void setLateSubmissions(int lateSubmissions) {
        this.lateSubmissions = lateSubmissions;
    }

    public ArrayList<String> getCourseTakenIds() {
        return courseTakenIds;
    }

    public void setCourseTakenIds(ArrayList<String> courseTakenIds) {
        this.courseTakenIds = courseTakenIds;
    }

    public ArrayList<String> getStudentCourseTakenIds() {
        return studentCourseTakenIds;
    }

    public void setStudentCourseTakenIds(ArrayList<String> studentCourseTakenIds) {
        this.studentCourseTakenIds = studentCourseTakenIds;
    }

    public ArrayList<String> getCourseInterested() {
        return courseInterested;
    }

    public void setCourseInterested(ArrayList<String> courseInterested) {
        this.courseInterested = courseInterested;
    }

    public ArrayList<String> getCourseRelatedIds() {
        return courseRelatedIds;
    }

    public void setCourseRelatedIds(ArrayList<String> courseRelatedIds) {
        this.courseRelatedIds = courseRelatedIds;
    }

    public ArrayList<String> getPreScheduledMeetingIds() {
        return preScheduledMeetingIds;
    }

    public void setPreScheduledMeetingIds(ArrayList<String> preScheduledMeetingIds) {
        this.preScheduledMeetingIds = preScheduledMeetingIds;
    }

    public ArrayList<String> getMeetingHistoryIds() {
        return meetingHistoryIds;
    }

    public void setMeetingHistoryIds(ArrayList<String> meetingHistoryIds) {
        this.meetingHistoryIds = meetingHistoryIds;
    }

    public ArrayList<String> getScheduleCompletedThisWeekIds() {
        return scheduleCompletedThisWeekIds;
    }

    public void setScheduleCompletedThisWeekIds(ArrayList<String> scheduleCompletedThisWeekIds) {
        this.scheduleCompletedThisWeekIds = scheduleCompletedThisWeekIds;
    }

    public ArrayList<String> getAllScheduleIds() {
        return allScheduleIds;
    }

    public void setAllScheduleIds(ArrayList<String> allScheduleIds) {
        this.allScheduleIds = allScheduleIds;
    }

    public ArrayList<String> getAllTaskIds() {
        return allTaskIds;
    }

    public void setAllTaskIds(ArrayList<String> allTaskIds) {
        this.allTaskIds = allTaskIds;
    }

    public ArrayList<String> getUncompletedTaskIds() {
        return uncompletedTaskIds;
    }

    public void setUncompletedTaskIds(ArrayList<String> uncompletedTaskIds) {
        this.uncompletedTaskIds = uncompletedTaskIds;
    }

    public ArrayList<String> getAllAgendaIds() {
        return allAgendaIds;
    }

    public void setAllAgendaIds(ArrayList<String> allAgendaIds) {
        this.allAgendaIds = allAgendaIds;
    }
}
