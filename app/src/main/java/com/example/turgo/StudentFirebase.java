package com.example.turgo;

import android.util.Log;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class StudentFirebase extends UserFirebase implements FirebaseClass<Student>{

    private double percentageCompleted;
    private String nextMeeting;
    private String lastScheduled;
    private int autoSchedule;
    private int notificationEarlyDuration;
    private int lateAttendance;
    private int lateSubmissions;
    private boolean hasScheduled;
    private String completionWeekKey;
    private String school;
    private String gradeLevel;
    private ArrayList<String> courseTaken;
    private ArrayList<String> studentCourseTaken;
    private ArrayList<String> courseInterested; // already a list of types (Strings)
    private ArrayList<String> courseRelated;
    private ArrayList<String> preScheduledMeetings;
    private ArrayList<String> meetingHistory;
    private ArrayList<String> scheduleCompletedThisWeek;
    private ArrayList<String> allSchedules;
    private ArrayList<String> allTask;
    private ArrayList<String> uncompletedTask;
    private ArrayList<String> manualCompletedTask;
    private ArrayList<String> manualMissedTask;
    private ArrayList<String> allAgendas;

    public StudentFirebase(){
        super(UserType.STUDENT.type());
    }
    public void importObjectData(Student from) {
        // Basic fields
        setID(from.getID());
        setFullName(from.getFullName());
        setNickname(from.getNickname());
        setBirthDate(from.getBirthDate());
        setAge(from.getAge());
        setEmail(from.getEmail());
        setGender(from.getGender());
        setPhoneNumber(from.getPhoneNumber());
        setLanguage(from.getLanguage().getDisplayName());
        setTheme(from.getTheme().getTheme());
        setInbox(from.getInboxIds());
        setOutbox(from.getOutboxIds());
        setNotifications(from.getNotificationsIds());
        setSchool(from.getSchool());
        setGradeLevel(from.getGradeLevel());

        percentageCompleted = from.getPercentageCompleted();
        completionWeekKey = from.getCompletionWeekKey();
        nextMeeting = from.getNextMeetingId();
        hasScheduled = from.isHasScheduled();
        lastScheduled = from.getLastScheduled() != null ? from.getLastScheduled().toString() : "";
        autoSchedule = from.getAutoSchedule();

        notificationEarlyDuration = 0;
        if (from.getNotificationEarlyDuration() != null) {
            notificationEarlyDuration = (int) from.getNotificationEarlyDuration().toMinutes();
        }

        // Penalties / counts
        lateAttendance = from.getLateAttendance();
        lateSubmissions = from.getLateSubmissions();

        // Lists -> ID lists (use convertToIdList helper for object lists)
        courseTaken = from.getCourseTakenIds() != null ? from.getCourseTakenIds() : new ArrayList<>();
        studentCourseTaken = from.getStudentCourseTakenIds() != null ? from.getStudentCourseTakenIds() : new ArrayList<>();
        courseInterested = from.getCourseInterested() != null ? from.getCourseInterested() : new ArrayList<>();
        courseRelated = from.getCourseRelatedIds() != null ? from.getCourseRelatedIds() : new ArrayList<>();
        preScheduledMeetings = from.getPreScheduledMeetingsIds() != null ? from.getPreScheduledMeetingsIds() : new ArrayList<>();
        meetingHistory = from.getMeetingHistoryIds() != null ? from.getMeetingHistoryIds() : new ArrayList<>();
        scheduleCompletedThisWeek = from.getScheduleCompletedThisWeekIds() != null ? from.getScheduleCompletedThisWeekIds() : new ArrayList<>();
        allSchedules = from.getAllSchedulesIds() != null ? from.getAllSchedulesIds() : new ArrayList<>();
        allTask = from.getAllTaskIds() != null ? from.getAllTaskIds() : new ArrayList<>();
        uncompletedTask = from.getUncompletedTaskIds() != null ? from.getUncompletedTaskIds() : new ArrayList<>();
        manualCompletedTask = from.getManualCompletedTaskIds() != null ? from.getManualCompletedTaskIds() : new ArrayList<>();
        manualMissedTask = from.getManualMissedTaskIds() != null ? from.getManualMissedTaskIds() : new ArrayList<>();
        allAgendas = from.getAllAgendasIds() != null ? from.getAllAgendasIds() : new ArrayList<>();
    }

    @Override
    public String getID() {
        return super.getID();
    }
    @Override
    public void convertToNormal(ObjectCallBack<Student> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Student.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Student) object);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("StudentFirebase", "convertToNormal failed for id=" + getID() + ": " + error.getMessage());
                objectCallBack.onError(error);
            }
        });
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public boolean isHasScheduled() {
        return hasScheduled;
    }

    public void setHasScheduled(boolean hasScheduled) {
        this.hasScheduled = hasScheduled;
    }

    public String getCompletionWeekKey() {
        return completionWeekKey;
    }

    public void setCompletionWeekKey(String completionWeekKey) {
        this.completionWeekKey = completionWeekKey;
    }

    public double getPercentageCompleted() {
        return percentageCompleted;
    }

    public void setPercentageCompleted(double percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public String getNextMeeting() {
        return nextMeeting;
    }

    public void setNextMeeting(String nextMeeting) {
        this.nextMeeting = nextMeeting;
    }

    public String getLastScheduled() {
        return lastScheduled;
    }

    public void setLastScheduled(String lastScheduled) {
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

    public ArrayList<String> getCourseTaken() {
        return courseTaken;
    }

    public void setCourseTaken(ArrayList<String> courseTaken) {
        this.courseTaken = courseTaken;
    }

    public ArrayList<String> getStudentCourseTaken() {
        return studentCourseTaken;
    }

    public void setStudentCourseTaken(ArrayList<String> studentCourseTaken) {
        this.studentCourseTaken = studentCourseTaken;
    }

    public ArrayList<String> getCourseInterested() {
        return courseInterested;
    }

    public void setCourseInterested(ArrayList<String> courseInterested) {
        this.courseInterested = courseInterested;
    }

    public ArrayList<String> getCourseRelated() {
        return courseRelated;
    }

    public void setCourseRelated(ArrayList<String> courseRelated) {
        this.courseRelated = courseRelated;
    }

    public ArrayList<String> getPreScheduledMeetings() {
        return preScheduledMeetings;
    }

    public void setPreScheduledMeetings(ArrayList<String> preScheduledMeetings) {
        this.preScheduledMeetings = preScheduledMeetings;
    }

    public ArrayList<String> getMeetingHistory() {
        return meetingHistory;
    }

    public void setMeetingHistory(ArrayList<String> meetingHistory) {
        this.meetingHistory = meetingHistory;
    }

    public ArrayList<String> getScheduleCompletedThisWeek() {
        return scheduleCompletedThisWeek;
    }

    public void setScheduleCompletedThisWeek(ArrayList<String> scheduleCompletedThisWeek) {
        this.scheduleCompletedThisWeek = scheduleCompletedThisWeek;
    }

    public ArrayList<String> getAllSchedules() {
        return allSchedules;
    }

    public void setAllSchedules(ArrayList<String> allSchedules) {
        this.allSchedules = allSchedules;
    }

    public ArrayList<String> getAllTask() {
        return allTask;
    }

    public void setAllTask(ArrayList<String> allTask) {
        this.allTask = allTask;
    }

    public ArrayList<String> getUncompletedTask() {
        return uncompletedTask;
    }

    public void setUncompletedTask(ArrayList<String> uncompletedTask) {
        this.uncompletedTask = uncompletedTask;
    }

    public ArrayList<String> getManualCompletedTask() {
        return manualCompletedTask;
    }

    public void setManualCompletedTask(ArrayList<String> manualCompletedTask) {
        this.manualCompletedTask = manualCompletedTask;
    }

    public ArrayList<String> getManualMissedTask() {
        return manualMissedTask;
    }

    public void setManualMissedTask(ArrayList<String> manualMissedTask) {
        this.manualMissedTask = manualMissedTask;
    }

    public ArrayList<String> getAllAgendas() {
        return allAgendas;
    }

    public void setAllAgendas(ArrayList<String> allAgendas) {
        this.allAgendas = allAgendas;
    }
}
