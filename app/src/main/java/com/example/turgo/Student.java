package com.example.turgo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

public class Student extends User implements Serializable, RequireUpdate<Student, StudentFirebase>{
    private static final FirebaseNode fbn = FirebaseNode.STUDENT;
    private static final Class<StudentFirebase> fbc = StudentFirebase.class;
    private ArrayList<Course> courseTaken;
    private ArrayList<StudentCourse>studentCourseTaken;
    public static final String SERIALIZE_KEY_CODE = "students";
    private ArrayList<String> courseInterested; //coursetype
    private ArrayList<Course> courseRelated; //courses related to the coursetype
    private ArrayList<Meeting> preScheduledMeetings;
    private ArrayList<Meeting> meetingHistory;
    private ArrayList<Schedule> scheduleCompletedThisWeek;
    private double percentageCompleted;
    private Meeting nextMeeting;
    private ArrayList<Schedule> allSchedules;
    private LocalDate lastScheduled;
    private boolean hasScheduled;
    private int autoSchedule;
    private Duration notificationEarlyDuration; //how many minute before the meeting (notification)
    private ArrayList<Task> allTask;
    private ArrayList<Task> uncompletedTask;
    private ArrayList<Agenda> allAgendas;
    private int lateAttendance;
    private int lateSubmissions;
    private Context context;

    public Student(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.STUDENT, gender, fullName, birthDate, nickname, email, phoneNumber);
        courseTaken = new ArrayList<>();
        courseInterested = new ArrayList<>();
        courseRelated = new ArrayList<>();
        preScheduledMeetings = new ArrayList<>();
        allSchedules = new ArrayList<>();
        meetingHistory = new ArrayList<>();
        scheduleCompletedThisWeek = new ArrayList<>();
        allAgendas = new ArrayList<>();
        percentageCompleted = 0;
        notificationEarlyDuration = Duration.ofMinutes(30);
        hasScheduled = false;
        autoSchedule = 1;
        nextMeeting = null;
        lateAttendance = 0;
        lateSubmissions = 0;
        findCourseRelated();
    }
    public void getStudentCourseFromCourse(Course course, ObjectCallBack<StudentCourse>callback){
        for(StudentCourse sc:studentCourseTaken){
            sc.getOfCourse(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(Course object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    if (object == course) {
                        callback.onObjectRetrieved(sc);
                    }
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });

        }
    }


    @Override
    public String getID() {
        return getUid();
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<StudentFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public void updateUserDB() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        updateDB();

    }

    public Student(){
        courseTaken = new ArrayList<>();
        courseInterested = new ArrayList<>();
        courseRelated = new ArrayList<>();
        preScheduledMeetings = new ArrayList<>();
        allSchedules = new ArrayList<>();
        meetingHistory = new ArrayList<>();
        scheduleCompletedThisWeek = new ArrayList<>();
        allAgendas = new ArrayList<>();
    }

    @Override
    public String getSerializeCode() {
        return SERIALIZE_KEY_CODE;
    }

    public ArrayList<Task>getCompletedTask(){
        ArrayList<Task>uncompleted = new ArrayList<>();
        for(Task task:allTask){
            if(!uncompletedTask.contains(task)){
                uncompleted.add(task);
            }
        }
        return uncompleted;
    }

    public ArrayList<Meeting> getAllMeetingOfCourse(Course course){
        ArrayList<Meeting>meetings = new ArrayList<>();
        for(Meeting meeting : this.meetingHistory){
            Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
            Course c = Await.get(schedule::getScheduleOfCourse);
            if(c==course){
                meetings.add(meeting);
            }


        }
        return meetings;
    }
    public void calculatePercentageCompleted(){
        if(allSchedules!= null){
            this.percentageCompleted = -1;
        }
        int amountOfSchedules = allSchedules.size();
        int completed = scheduleCompletedThisWeek.size();
        this.percentageCompleted = (double) completed /amountOfSchedules * 100;
    }
    public void setNextMeeting(){
        this.nextMeeting = preScheduledMeetings.get(0);
    }

    public void attendMeeting(Meeting meeting){
        meetingHistory.add(meeting);
        meeting.addStudentAttendance(this, LocalTime.now());
        if(meeting.getStudentsAttended().get(this).isAfter(meeting.getStartTimeChange())){
            lateAttendance++;
        }
    }
    public void addScheduledMeeting() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        LocalDate today = LocalDate.now();
        for(int i = 0; i<autoSchedule; i++){
            for(int j = 0; j<allSchedules.size(); j++){
                if(!allSchedules.get(j).isHasScheduled()){
                    LocalDate meetingDate= today.plusWeeks(i-1).with(TemporalAdjusters.nextOrSame(allSchedules.get(j).getDay()));
                    Meeting meeting = new Meeting(allSchedules.get(j), meetingDate, this, context);
                    Meeting.setMeetingEndAlarm(context, meeting.getDateOfMeeting(), meeting.getEndTimeChange(), this, meeting);
                    preScheduledMeetings.add(meeting);
                    Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
                    Course course = Await.get(schedule::getScheduleOfCourse);
                    Teacher teacher = Await.get(course::getTeacher);
                    teacher.addScheduledMeeting(meeting);
                    meeting.updateDB();

                    for(Student student: Await.get(allSchedules.get(j)::getStudents)){
                        student.preScheduledMeetings.add(meeting);
                        student.updateUserDB();
                    }
                    allSchedules.get(i).setScheduler(this);

                }
            }
        }
        lastScheduled = today;
    }

    private ArrayList<Schedule> updateSchedule(){ //updates schedule based on course's schedule
        ArrayList<Schedule>schedules = new ArrayList<>();
        for(StudentCourse course : studentCourseTaken){
            schedules.addAll(course.getSchedulesOfCourse());
        }
        //sort here
        Schedule []scheduleArr = new Schedule[schedules.size()];
        for(int i = 0; i<schedules.size(); i++){
            scheduleArr[i] = schedules.get(i);
        }
        sortSchedule(schedules);
        return schedules;
    }

    private void sortSchedule(ArrayList<Schedule> schedules){
        schedules.sort(Comparator.comparing(Schedule::getDay).thenComparing(Schedule::getMeetingStart));
    }

    public LocalDate getClosestMeetingOfCourse(Course course){
        int minDistance = Integer.MAX_VALUE;
        //Schedule closestMeeting;
        int today = LocalDate.now().getDayOfWeek().getValue();
        if(course.getSchedules()!=null){
            for(Schedule schedule : course.getSchedules()){
                int day = schedule.getDay().getValue();
                int distance = (day - today + 7) % 7;
                if(distance < minDistance){
                    minDistance = distance;
                    //closestMeeting = schedule;
                }
            }
            return LocalDate.now().plusDays(minDistance);
        }
        return null;

    }

    public void completeMeeting(Meeting meeting){
        preScheduledMeetings.remove(meeting);
        Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
        scheduleCompletedThisWeek.add(schedule);
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
    public void joinCourse(Course course, boolean paymentPreferences, boolean privateOrGroup, int payment, ArrayList<Schedule>schedules) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        course.addStudent(this, paymentPreferences, privateOrGroup, payment, schedules);
        course.getSchedules().addAll(schedules);
        allSchedules = updateSchedule();
        addScheduledMeeting();
    }
    public void assignAgenda(Agenda agenda){
        allAgendas.add(agenda);
    }



    public ArrayList<StudentCourse> getStudentCourseTaken() {
        return studentCourseTaken;
    }

    public void setStudentCourseTaken(ArrayList<StudentCourse> studentCourseTaken) {
        this.studentCourseTaken = studentCourseTaken;
    }

    public Duration getNotificationEarlyDuration() {
        return notificationEarlyDuration;
    }

    public void setNotificationEarlyDuration(Duration notificationEarlyDuration) {
        this.notificationEarlyDuration = notificationEarlyDuration;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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

    public boolean isHasScheduled() {
        return hasScheduled;
    }

    public void setHasScheduled(boolean hasScheduled) {
        this.hasScheduled = hasScheduled;
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

    public void addMeetingCompleted(Meeting meeting){
        Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
        scheduleCompletedThisWeek.add(schedule);
        meeting.setCompleted(true);
    }

    public ArrayList<Schedule> getScheduleCompletedThisWeek() {
        return scheduleCompletedThisWeek;
    }

    public void setScheduleCompletedThisWeek(ArrayList<Schedule> scheduleCompletedThisWeek) {
        this.scheduleCompletedThisWeek = scheduleCompletedThisWeek;
    }

    public Meeting getNextMeeting() {
        return nextMeeting;
    }

    public void setNextMeeting(Meeting nextMeeting) {
        this.nextMeeting = nextMeeting;
    }

    public double getPercentageCompleted() {
        calculatePercentageCompleted();
        return Tool.safeDouble(percentageCompleted);
    }

    public void setPercentageCompleted(double percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public ArrayList<Task> getAllTask() {
        return allTask;
    }

    public void setAllTask(ArrayList<Task> allTask) {
        this.allTask = allTask;
    }

    public ArrayList<Task> getUncompletedTask() {
        return uncompletedTask;
    }

    public void setUncompletedTask(ArrayList<Task> uncompletedTask) {
        this.uncompletedTask = uncompletedTask;
    }
    public ArrayList<Task>getAllTaskOfCourse(Course course){
        ArrayList<Task>tasks = new ArrayList<>();
        for(Task task : uncompletedTask){
            if(task.getTaskOfCourse() == course){
                tasks.add(task);
            }
        }
        return tasks;
    }
    public void getAgendaOfCourse(Course course, ObjectCallBack<ArrayList<Agenda>>callBack){
        ArrayList<Agenda>a = new ArrayList<>();
        AtomicInteger completed = new AtomicInteger(0);
        for(Agenda agenda : allAgendas){
            agenda.getOfCourse(new ObjectCallBack<>() {
                @Override
                public void onObjectRetrieved(Course object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                    if (object == course) {
                        a.add(agenda);
                    }
                    int current = completed.get();
                    current++;
                    if(current == allAgendas.size()){
                        callBack.onObjectRetrieved(a);
                    }
                }

                @Override
                public void onError(DatabaseError error) {

                }
            });
        }
    }
    public void addAgenda(Agenda agenda){
        allAgendas.add(agenda);
    }
    public ArrayList<Agenda> getAllAgendas() {
        return allAgendas;
    }

    public void setAllAgendas(ArrayList<Agenda> allAgendas) {
        this.allAgendas = allAgendas;
    }

    public void assignTask(Task task) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        allTask.add(task);
        uncompletedTask.add(task);
        updateUserDB();
    }

    public void getLatestAgendaOfCourse(Course course, ObjectCallBack<Agenda>callBack){
        getAgendaOfCourse(course, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(ArrayList<Agenda> object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                int size = object.size();
                callBack.onObjectRetrieved(object.get(size - 1));
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
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
    public void getExploreCourse(ObjectCallBack<ArrayList<Course>>callback){
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.COURSE.getPath());

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CourseFirebase> allCourses = new ArrayList<>();
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    CourseFirebase course = courseSnap.getValue(CourseFirebase.class);
                    if (course != null) {
                        allCourses.add(course);
                    }
                }
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference(CourseType.fbn.getPath());
                dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<CourseFirebase> courseInterested = new ArrayList<>();
                        for(DataSnapshot ds : snapshot.getChildren()){
                            CourseTypeFirebase ctf = ds.getValue(CourseTypeFirebase.class);
                            for(CourseFirebase cf : allCourses){
                                if(cf.getCourseType().equals(ctf.getID())){
                                    courseInterested.add(cf);
                                    Log.d("Student", "Course Interest Added: " + cf);
                                }
                            }
                        }

                        Tool.<CourseFirebase, Course>convertFirebaseListToNormal(courseInterested, new Tool.ConvertToNormalCallback<>() {
                            @Override
                            public void onAllConverted(ArrayList<Course> normalList) {
                                try {
                                    callback.onObjectRetrieved(normalList);
                                } catch (ParseException | InvocationTargetException |
                                         NoSuchMethodException | IllegalAccessException |
                                         InstantiationException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            public void onError(DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });
    }

    public void submitTask(ArrayList<file>filesSelected, Task task) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        for(file file: filesSelected){
            file.setSubmitTime(LocalDateTime.now());
            task.submit(file, this);

            if(task.getDropbox().getSubmissionSlot(this).isLate(file.getSubmitTime())){
                this.lateSubmissions ++;
            }
            String folderPath = "turgo/submissions/"+task.getTaskOfCourse().getCourseID()+"/"+task.getTaskID()+"/"+ getUid();
            task.getDropbox().updateDB();
        }
        ArrayList<String>fileNames = new ArrayList<>();
        for(file file: filesSelected){
            fileNames.add(file.getFileName());
        }
        SubmissionDisplay sd = new SubmissionDisplay(getFullName(), task.getTitle(), task.getTaskOfCourse().getCourseName(), task.getDueDate().toString(), fileNames);
        Teacher teacher = Await.get(task.getTaskOfCourse()::getTeacher);
        teacher.addLatestSubmission(sd);
        uncompletedTask.remove(task);


    }

    @Override
    public String toString() {
        return super.toString() + "Student{" +
                "courseInterested=" + courseInterested +
                '}';
    }
}
