package com.example.turgo;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Schedule implements Serializable, RequireUpdate<Schedule, ScheduleFirebase, ScheduleRepository> {
    private final FirebaseNode fbn = FirebaseNode.SCHEDULE;
    private final Class<ScheduleFirebase>fbc = ScheduleFirebase.class;
    public final String scheduleID;
    public static final String SERIALIZE_KEY_CODE = "scheduleObj";
    public static final LocalDate NEVER_SCHEDULED = LocalDate.of(1000, 1, 1);
    private int numberOfStudents;
    private boolean isPrivate;
    private boolean hasScheduled;
    private User scheduler;
    private String ofCourse;
    private LocalTime meetingStart;
    private LocalTime meetingEnd;
    private int duration;
    private DayOfWeek day;
    private String room;
    public ArrayList<String>students;

    public Schedule(LocalTime meetingStart, int duration, DayOfWeek day,  boolean isPrivate, String ofCourse){
        this.scheduleID = UUID.randomUUID().toString();
        this.meetingStart = meetingStart;
        this.duration = duration;
        this.ofCourse = ofCourse;
        this.day = day;
        this.meetingEnd = meetingStart.plus(Duration.ofMinutes(duration));
        this.isPrivate = isPrivate;
        this.hasScheduled = false;
        this.students = new ArrayList<>();
    }
    public LocalDate getNextMeetingDate(){
        DayOfWeek day = this.day;
        LocalDate today = LocalDate.now();
        LocalDate nextMeeting = today.with(TemporalAdjusters.nextOrSame(day)); //gets the nearest date of day
        if(nextMeeting.equals(today) && LocalTime.now().isAfter(meetingEnd)){
            nextMeeting = nextMeeting.plusWeeks(1);
        }
        return nextMeeting;
    }

    public Schedule(){scheduleID = "";}
    public Schedule(String id){
        scheduleID = id;
    }
    public boolean isPrivate(){
        return this.isPrivate;
    }

    public static int maxStudentsOfSchedules(ArrayList<Schedule>schedules){
        int maxStudents = 0;
        if(schedules.isEmpty()){
            return 1;
        }
        for(Schedule schedule : schedules){
            if(schedule.numberOfStudents > maxStudents){
                maxStudents = schedule.numberOfStudents;
            }
        }
        return maxStudents;
    }
    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public LocalTime getMeetingStart() {
        return meetingStart;
    }

    public void setMeetingStart(LocalTime meetingStart) {
        this.meetingStart = meetingStart;
    }

    public LocalTime getMeetingEnd() {
        return meetingEnd;
    }

    public void setMeetingEnd(LocalTime meetingEnd) {
        this.meetingEnd = meetingEnd;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void getRoom(ObjectCallBack<Room>callBack) {
        try {
            findAggregatedObject( Room.class, "schedules", callBack);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public Task<Room> getRoom(){
        if (!Tool.boolOf(room)) {
            return Tasks.forResult(null);
        }
        RoomRepository roomRepository = new RoomRepository(room);
        return roomRepository.loadAsNormal();
    }
    public String getRoomId(){
        return room;
    }

    public String getOfCourse() {
        return ofCourse;
    }

    public void setOfCourse(String ofCourse) {
        this.ofCourse = ofCourse;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Task<Course> getScheduleOfCourse(){
        if (!Tool.boolOf(ofCourse)) {
            return Tasks.forResult(null);
        }
        CourseRepository courseRepository = new CourseRepository(ofCourse);
        return courseRepository.loadAsNormal();
    }

    public int getNumberOfStudents() {
        if(numberOfStudents == 0 && !students.isEmpty()){
            getRepositoryInstance().incrementNumberOfStudents(students.size());
            numberOfStudents = students.size();
        }
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public String getScheduleID() {
        return scheduleID;
    }

    public static void sortSchedule(ArrayList<Schedule> schedules){
        schedules.sort(Comparator.comparing(Schedule::getDay).thenComparing(Schedule::getMeetingStart));
    }

//    public void getStudents(ObjectCallBack<ArrayList<Student>>callBack) {
//        try {
//            findAllAggregatedObjects(Student.class, "allSchedules", callBack);
//        } catch (IllegalAccessException | InstantiationException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public Task<List<Student>>getStudents(){
        if (students == null || students.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        List<Task<Student>> studentTask = new ArrayList<>();
        for (String studentId : students) {
            if (!Tool.boolOf(studentId)) {
                continue;
            }
            StudentRepository studentRepository = new StudentRepository(studentId);
            studentTask.add(studentRepository.loadAsNormal());
        }

        if (studentTask.isEmpty()) {
            return Tasks.forResult(new ArrayList<>());
        }

        return Tasks.whenAllSuccess(studentTask);
    }




    public boolean isHasScheduled() {
        return hasScheduled;
    }

    public void setHasScheduled(boolean hasScheduled) {
        this.hasScheduled = hasScheduled;
    }

    public User getScheduler() {
        return scheduler;
    }

    public void setScheduler(User scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<ScheduleRepository> getRepositoryClass() {
        return ScheduleRepository.class;
    }

    @Override
    public Class<ScheduleFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return this.scheduleID;
    }

}
