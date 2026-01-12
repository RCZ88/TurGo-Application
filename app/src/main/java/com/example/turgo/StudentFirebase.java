package com.example.turgo;

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
        setInbox(convertToIdList(from.getInbox()));
        setOutbox(convertToIdList(from.getOutbox()));
        setNotifications(convertToIdList(from.getNotifications()));

        percentageCompleted = from.getPercentageCompleted();
        nextMeeting = (from.getNextMeeting() != null && from.getNextMeeting().getID() != null) ? from.getNextMeeting().getID() : null;
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
        courseTaken = from.getCourseTaken() != null ? convertToIdList(from.getCourseTaken()) : new ArrayList<>();
        studentCourseTaken = from.getStudentCourseTaken() != null ? convertToIdList(from.getStudentCourseTaken()) : new ArrayList<>();
        courseInterested = from.getCourseInterested() != null ? from.getCourseInterested() : new ArrayList<>();
        courseRelated = from.getCourseRelated() != null ? convertToIdList(from.getCourseRelated()) : new ArrayList<>();
        preScheduledMeetings = from.getPreScheduledMeetings() != null ? convertToIdList(from.getPreScheduledMeetings()) : new ArrayList<>();
        meetingHistory = from.getMeetingHistory() != null ? convertToIdList(from.getMeetingHistory()) : new ArrayList<>();
        scheduleCompletedThisWeek = from.getScheduleCompletedThisWeek() != null ? convertToIdList(from.getScheduleCompletedThisWeek()) : new ArrayList<>();
        allSchedules = from.getAllSchedules() != null ? convertToIdList(from.getAllSchedules()) : new ArrayList<>();
        allTask = from.getAllTask() != null ? convertToIdList(from.getAllTask()) : new ArrayList<>();
        uncompletedTask = from.getUncompletedTask() != null ? convertToIdList(from.getUncompletedTask()) : new ArrayList<>();
        allAgendas = from.getAllAgendas() != null ? convertToIdList(from.getAllAgendas()) : new ArrayList<>();
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

            }
        });
    }

    public boolean isHasScheduled() {
        return hasScheduled;
    }

    public void setHasScheduled(boolean hasScheduled) {
        this.hasScheduled = hasScheduled;
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

    public ArrayList<String> getAllAgendas() {
        return allAgendas;
    }

    public void setAllAgendas(ArrayList<String> allAgendas) {
        this.allAgendas = allAgendas;
    }
}
