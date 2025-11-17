package com.example.turgo;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
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
import java.util.Map;

public class Student extends User implements Serializable, RequireUpdate<Student, StudentFirebase>{
    private final FirebaseNode fbn = FirebaseNode.STUDENT;
    private final Class<StudentFirebase> fbc = StudentFirebase.class;
    private ArrayList<Course> courseTaken;
    private ArrayList<StudentCourse>studentCourseTaken;
    public static final String SERIALIZE_KEY_CODE = "students";
    protected static final String FIREBASE_DB_REFERENCE = User.FIREBASE_DB_REFERENCE + "/" + SERIALIZE_KEY_CODE;
    private ArrayList<String> courseInterested; //coursetype
    private ArrayList<Course> courseRelated; //courses related to the coursetype
    private ArrayList<Meeting> preScheduledMeetings;
    private ArrayList<Meeting> meetingHistory;
    private ArrayList<Schedule> scheduleCompletedThisWeek;
    private double percentageCompleted;
    private Meeting nextMeeting;
    private ArrayList<Schedule> allSchedules;
    private LocalDate lastScheduled;
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
        autoSchedule = 1;
        nextMeeting = null;
        lateAttendance = 0;
        lateSubmissions = 0;
        findCourseRelated();
    }
    public StudentCourse getStudentCourseFromCourse(Course course){
        for(StudentCourse sc:studentCourseTaken){
            if(sc.getOfCourse() == course){
                return sc;
            }
        }
        return null;
    }


    @Override
    public String getID() {
        return getUID();
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

    public Student(){}

    public ArrayList<Meeting> getAllMeetingOfCourse(Course course){
        ArrayList<Meeting>meetings = new ArrayList<>();
        for(Meeting meeting : this.meetingHistory){
            if(meeting.getMeetingOfSchedule().getScheduleOfCourse() == course){
                meetings.add(meeting);
            }
        }
        return meetings;
    }
    public void calculatePercentageCompleted(){
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
                    meeting.getMeetingOfSchedule()
                            .getScheduleOfCourse()
                            .getTeacher()
                            .addScheduledMeeting(meeting);
                    meeting.updateDB();

                    for(Student student: allSchedules.get(j).getStudents()){
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

    public void completeMeeting(Meeting meeting){
        preScheduledMeetings.remove(meeting);
        scheduleCompletedThisWeek.add(meeting.getMeetingOfSchedule());
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
        scheduleCompletedThisWeek.add(meeting.getMeetingOfSchedule());
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
        return percentageCompleted;
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
    public ArrayList<Agenda> getAgendaOfCourse(Course course){
        ArrayList<Agenda>a = new ArrayList<>();
        for(Agenda agenda : allAgendas){
            if(agenda.getOfCourse() == course){
                a.add(agenda);
            }
        }
        return a;
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

    public Agenda getLatestAgendaOfCourse(Course course){
        int size = getAgendaOfCourse(course).size();
        return getAgendaOfCourse(course).get(size-1);
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
    public ArrayList<Course>getExploreCourse(){
        DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.COURSE.getPath());
        ArrayList<Course>ci = new ArrayList<>();

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
                for(CourseFirebase cf: allCourses){
                    CourseType c = new CourseType();
                    c.retrieveOnce(new ObjectCallBack<CourseTypeFirebase>() {
                        @Override
                        public void onObjectRetrieved(CourseTypeFirebase object) {
                            if(courseInterested.contains(object.getCourseType())){
                                //course type is interested by the student
                                try {
                                    Course courseInterested = cf.convertToNormal();
                                    ci.add(courseInterested);
                                } catch (ParseException | InvocationTargetException |
                                         NoSuchMethodException | IllegalAccessException |
                                         InstantiationException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        @Override
                        public void onError(DatabaseError error) {

                        }
                    }, cf.getCourseTypeID());

                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });
        return ci;
    }

    public void submitTask(ArrayList<file>filesSelected, Task task) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        for(file file: filesSelected){
            file.setSubmitTime(LocalDateTime.now());
            task.getDropbox().getSubmissionSlot(this).addFile(file);

            if(task.getDropbox().getSubmissionSlot(this).isLate(file.getSubmitTime())){
                this.lateSubmissions ++;
            }
            String folderPath = "turgo/submissions/"+task.getTaskOfCourse().getCourseID()+"/"+task.getTaskID()+"/"+getUID();
            task.getDropbox().updateDB();
        }
        ArrayList<String>fileNames = new ArrayList<>();
        for(file file: filesSelected){
            fileNames.add(file.getFileName());
        }
        SubmissionDisplay sd = new SubmissionDisplay(getFullName(), task.getTitle(), task.getTaskOfCourse().getCourseName(), task.getDueDate().toString(), fileNames);
        task.getTaskOfCourse().getTeacher().addLatestSubmission(sd);

        uncompletedTask.remove(task);
    }

    @Override
    public String toString() {
        return super.toString() + "Student{" +
                "courseInterested=" + courseInterested +
                '}';
    }
}
