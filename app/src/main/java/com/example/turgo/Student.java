package com.example.turgo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.compose.runtime.snapshots.Snapshot;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

public class Student extends User implements Serializable, RequireUpdate<Student, StudentFirebase, StudentRepository>{
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
    private String school;
    private String gradeLevel;
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
        school = "";
        gradeLevel = "";
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
    public Class<StudentRepository> getRepositoryClass() {
        return StudentRepository.class;
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
        this.courseTaken = new ArrayList<>();
        this.studentCourseTaken = new ArrayList<>();
        this.courseInterested = new ArrayList<>();
        this.courseRelated = new ArrayList<>();
        this.preScheduledMeetings = new ArrayList<>();
        this.meetingHistory = new ArrayList<>();
        this.scheduleCompletedThisWeek = new ArrayList<>();
        this.allSchedules = new ArrayList<>();
        this.allTask = new ArrayList<>();
        this.uncompletedTask = new ArrayList<>();
        this.allAgendas = new ArrayList<>();
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

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getAllMeetingOfCourse(Course course){
        List<com.google.android.gms.tasks.Task<Course>>tasks = new ArrayList<>();
        ArrayList<Meeting>meetings = new ArrayList<>();
        for(Meeting meeting : this.meetingHistory){
            com.google.android.gms.tasks.Task<Course>task =  meeting.getMeetingOfCourse();
            tasks.add(task);
            task.addOnSuccessListener(c->{
                if(c==course){
                    meetings.add(meeting);
                }
            });
        }
        TaskCompletionSource<ArrayList<Meeting>>tcs = new TaskCompletionSource<>();
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(courses ->{
            tcs.setResult(meetings);
        });
        return tcs.getTask();
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
    // In Student.java
    public void addScheduledMeeting(Context context,  Teacher teacher) {
        LocalDate today = LocalDate.now();
        StudentRepository studentRepository = new StudentRepository(getID());

        for (int i = 0; i < autoSchedule; i++) {
            for (Schedule schedule : allSchedules) {
                if (schedule.isHasScheduled()) {
                    continue;
                }

                LocalDate meetingDate = today.plusWeeks(i).with(
                        TemporalAdjusters.nextOrSame(schedule.getDay())
                );

                Meeting meeting = new Meeting(schedule, meetingDate, this, schedule.getID());

                try {
                    meeting.setMeetingEndAlarm(context, meeting.getDateOfMeeting(),
                            meeting.getEndTimeChange(), this);

                    MeetingRepository meetingRepo = new MeetingRepository(meeting.getMeetingID());
                    meetingRepo.save(meeting);

                    preScheduledMeetings.add(meeting);

                    // ✅ Use passed-in course and teacher (no Await.get needed!)
                    teacher.addScheduledMeeting(meeting);

                    schedule.getStudents().addOnSuccessListener(studentsList ->{
                        for (Student student : studentsList) {
                            student.preScheduledMeetings.add(meeting);
                            studentRepository.addPreScheduledMeeting(meeting);
                        }
                    });

                    schedule.setScheduler(this);
                    schedule.setHasScheduled(true);

                } catch (Exception e) {
                    Log.e("Student", "Failed to process meeting", e);
                }
            }
        }

        lastScheduled = today;
        studentRepository.updateLastScheduled(today);
    }
    public com.google.android.gms.tasks.Task<Void> addScheduledMeetingAsync(Context context, Teacher teacher) {
        LocalDate today = LocalDate.now();
        StudentRepository studentRepository = new StudentRepository(getID());
        List<com.google.android.gms.tasks.Task<?>> tasks = new ArrayList<>();

        for (int i = 0; i < autoSchedule; i++) {
            for (Schedule schedule : allSchedules) {
                if (schedule.isHasScheduled()) {
                    continue;
                }

                LocalDate meetingDate = today.plusWeeks(i).with(
                        TemporalAdjusters.nextOrSame(schedule.getDay())
                );

                Meeting meeting = new Meeting(schedule, meetingDate, this, schedule.getID());

                try {
                    meeting.setMeetingEndAlarm(context, meeting.getDateOfMeeting(),
                            meeting.getEndTimeChange(), this);
                } catch (Exception e) {
                    return Tasks.forException(e);
                }

                MeetingRepository meetingRepo = new MeetingRepository(meeting.getMeetingID());
                tasks.add(logTask("meeting.save:" + meeting.getMeetingID(), meetingRepo.saveAsync(meeting)));

                preScheduledMeetings.add(meeting);

                TeacherRepository teacherRepository = new TeacherRepository(teacher.getID());
                tasks.add(logTask("teacher.addScheduledMeeting:" + meeting.getID(),
                        teacherRepository.addStringToArrayAsync("scheduledMeetings", meeting.getID())));
                teacher.addScheduledMeeting(meeting);

                com.google.android.gms.tasks.Task<List<Student>> studentsTask = schedule.getStudents();
                com.google.android.gms.tasks.Task<Void> addToStudentsTask = studentsTask.onSuccessTask(studentsList -> {
                    List<com.google.android.gms.tasks.Task<?>> studentTasks = new ArrayList<>();
                    for (Student student : studentsList) {
                        student.preScheduledMeetings.add(meeting);
                        StudentRepository sr = new StudentRepository(student.getID());
                        studentTasks.add(sr.addStringToArrayAsync("preScheduledMeetings", meeting.getID()));
                    }
                    return Tasks.whenAll(studentTasks);
                });
                tasks.add(logTask("students.addPreScheduledMeeting:" + meeting.getID(), addToStudentsTask));

                schedule.setScheduler(this);
                schedule.setHasScheduled(true);
                ScheduleRepository scheduleRepository = new ScheduleRepository(schedule.getID());
                tasks.add(logTask("schedule.save:" + schedule.getID(), scheduleRepository.saveAsync(schedule)));
            }
        }

        lastScheduled = today;
        tasks.add(logTask("student.updateLastScheduled:" + getID(),
                studentRepository.getDbReference().child("lastScheduled").setValue(today.toString())));

        if (tasks.isEmpty()) {
            return Tasks.forResult(null);
        }
        return Tasks.whenAll(tasks);
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
        Schedule.sortSchedule(schedules);
        return schedules;
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
        StudentRepository studentRepository = new StudentRepository(getID());
        preScheduledMeetings.remove(meeting);
        studentRepository.removePreScheduledMeetingCompletely(meeting);
//        Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
        meeting.getMeetingOfSchedule().addOnSuccessListener(schedule ->{
            scheduleCompletedThisWeek.add(schedule);
            studentRepository.addScheduleCompletedThisWeek(schedule);
        });

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
    public com.google.android.gms.tasks.Task<Void> joinCourse(Course course, boolean paymentPreferences, boolean privateOrGroup, int payment, ArrayList<Schedule>schedules, ArrayList<TimeSlot> timeSlot, Context context) {
        String tag = "JoinCourse";
        List<com.google.android.gms.tasks.Task<?>> tasks = new ArrayList<>();

        tasks.add(logTask("course.addStudentAsync:" + course.getID(),
                course.addStudentAsync(this, paymentPreferences, privateOrGroup, payment, schedules, timeSlot)));
        Log.d(tag, "course add student started");

        StudentRepository studentRepository = new StudentRepository(getID());
        allSchedules.addAll(schedules);
        for (Schedule schedule : schedules) {
            ScheduleRepository scheduleRepository = new ScheduleRepository(schedule.getID());
            tasks.add(logTask("student.addAllSchedules:" + schedule.getID(),
                    studentRepository.addStringToArrayAsync("allSchedules", schedule.getID())));
            tasks.add(logTask("schedule.save:" + schedule.getID(), scheduleRepository.saveAsync(schedule)));
        }
        Log.d(tag, "all Schedules queued for database");

        com.google.android.gms.tasks.Task<Void> teacherTask = course.getTeacher().onSuccessTask(teacher -> {
            List<com.google.android.gms.tasks.Task<?>> teacherTasks = new ArrayList<>();

            teacherTasks.add(logTask("student.addScheduledMeetingAsync:" + getID(),
                    addScheduledMeetingAsync(context, teacher)));

            for (Schedule schedule : schedules) {
                DayOfWeek dow = schedule.getDay();
                boolean found = false;
                for (DayTimeArrangement dta : teacher.getTimeArrangements()) {
                    if (dta.getDay() == dow) {
                        dta.getOccupied().add(schedule);
                        DTARepository dtaRepository = new DTARepository(dta.getID());
                        teacherTasks.add(logTask("dta.addOccupied:" + dta.getID() + ":" + schedule.getID(),
                                dtaRepository.addStringToArrayAsync("occupied", schedule.getID())));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    DayTimeArrangement dta = new DayTimeArrangement();
                    dta.getOccupied().add(schedule);
                    DTARepository dtaRepository = new DTARepository(dta.getID());
                    teacherTasks.add(logTask("dta.save:" + dta.getID(), dtaRepository.saveAsync(dta)));
                    teacherTasks.add(logTask("dta.addOccupied:" + dta.getID() + ":" + schedule.getID(),
                            dtaRepository.addStringToArrayAsync("occupied", schedule.getID())));
                    TeacherRepository teacherRepository = new TeacherRepository(teacher.getID());
                    teacherTasks.add(logTask("teacher.addTimeArrangement:" + dta.getID(),
                            teacherRepository.addStringToArrayAsync("timeArrangements", dta.getID())));
                }
            }
            return Tasks.whenAll(teacherTasks);
        });
        tasks.add(logTask("course.getTeacher:" + course.getID(), teacherTask));

        if (tasks.isEmpty()) {
            return Tasks.forResult(null);
        }
        return Tasks.whenAll(tasks);
    }

    private static <T> com.google.android.gms.tasks.Task<T> logTask(String label, com.google.android.gms.tasks.Task<T> task) {
        task.addOnSuccessListener(result -> Log.d("JoinCourseTask", "SUCCESS: " + label));
        task.addOnFailureListener(e -> Log.e("JoinCourseTask", "FAIL: " + label, e));
        return task;
    }
    public void assignAgenda(Agenda agenda){
        allAgendas.add(agenda);
    }



    public ArrayList<StudentCourse> getStudentCourseTaken() {
        if(studentCourseTaken == null){
            studentCourseTaken = new ArrayList<>();
        }
        return studentCourseTaken;
    }

    public void setStudentCourseTaken(ArrayList<StudentCourse> studentCourseTaken) {
        this.studentCourseTaken = studentCourseTaken;
    }

    public Duration getNotificationEarlyDuration() {
        return notificationEarlyDuration;
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

    public void setNotificationEarlyDuration(Duration notificationEarlyDuration) {
        this.notificationEarlyDuration = notificationEarlyDuration;
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
        meeting.getMeetingOfSchedule().addOnSuccessListener(schedule->{
            scheduleCompletedThisWeek.add(schedule);
            meeting.setCompleted(true);
        });
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
        if(uncompletedTask == null){
            uncompletedTask = new ArrayList<>();
            return uncompletedTask;
        }
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
        if (allAgendas == null || allAgendas.isEmpty()) {
            try {
                callBack.onObjectRetrieved(a);
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
            return;
        }
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
                if (size == 0) {
                callBack.onObjectRetrieved(null);
                return;
            }
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
    public com.google.android.gms.tasks.Task<ArrayList<Course>> getExploreCourse() {
        TaskCompletionSource<ArrayList<Course>> tcs = new TaskCompletionSource<>();

        DatabaseReference coursesRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.COURSE.getPath());

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

                DatabaseReference courseTypeRef = FirebaseDatabase.getInstance()
                        .getReference(CourseType.fbn.getPath());

                courseTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<CourseFirebase> courseInterested = new ArrayList<>();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            CourseTypeFirebase ctf = ds.getValue(CourseTypeFirebase.class);
                            if (ctf == null) continue;

                            for (CourseFirebase cf : allCourses) {
                                if (cf.getCourseType().equals(ctf.getID())) {
                                    courseInterested.add(cf);
                                    Log.d("Student", "Course Interest Added: " + cf);
                                }
                            }
                        }

                        if (courseInterested.isEmpty()) {
                            tcs.setResult(new ArrayList<>());
                            return;
                        }

                        DatabaseReference courseJoinedRef = FirebaseDatabase.getInstance()
                                .getReference(FirebaseNode.STUDENT.getPath())
                                .child(getID())
                                .child("courseTaken");

                        courseJoinedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> courseExisted = new ArrayList<>();

                                for (DataSnapshot s : snapshot.getChildren()) {
                                    courseExisted.add(s.getValue(String.class));
                                }

                                courseInterested.removeIf(cf -> courseExisted.contains(cf.getCourseID()));

                                if (courseInterested.isEmpty()) {
                                    tcs.setResult(new ArrayList<>());
                                    return;
                                }

                                convertCourses(courseInterested, tcs);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                tcs.setException(error.toException());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tcs.setException(error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tcs.setException(error.toException());
            }
        });

        return tcs.getTask();
    }
    private void convertCourses(ArrayList<CourseFirebase> courseFirebases,
                                TaskCompletionSource<ArrayList<Course>> tcs) {

        ArrayList<Course> result = new ArrayList<>();
        int total = courseFirebases.size();
        final int[] completed = {0};

        for (CourseFirebase cf : courseFirebases) {
            try {
                cf.convertToNormal(new ObjectCallBack<>() {
                    @Override
                    public void onObjectRetrieved(Course course) {
                        result.add(course);
                        completed[0]++;

                        if (completed[0] == total) {
                            result.removeIf(c -> courseTaken.contains(c));
                            tcs.setResult(result);
                        }
                    }

                    @Override
                    public void onError(DatabaseError error) {
                        tcs.setException(error.toException());
                    }
                });
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }



    public void submitTask(ArrayList<file>filesSelected, Task task) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        for(file file: filesSelected){
            file.setSubmitTime(LocalDateTime.now());
            task.submit(file, this);

            if(task.getDropbox().getSubmissionSlot(this).isLate(file.getSubmitTime())){
                this.lateSubmissions ++;
            }
            String folderPath = "turgo/submissions/"+task.getTaskOfCourse().getCourseID()+"/"+task.getTaskID()+"/"+ getUid();
//            task.getDropbox().updateDB();
        }
        ArrayList<String>fileNames = new ArrayList<>();
        for(file file: filesSelected){
            fileNames.add(file.getFileName());
        }
        SubmissionDisplay sd = new SubmissionDisplay(getFullName(), task.getTitle(), task.getTaskOfCourse().getCourseName(), task.getDueDate().toString(), fileNames);
//            Teacher teacher = Await.get(task.getTaskOfCourse()::getTeacher);
        task.getTaskOfCourse().getTeacher().addOnSuccessListener(teacher ->{
            teacher.addLatestSubmission(sd);
            uncompletedTask.remove(task);
        });

    }

    @Override
    public String toString() {
        return super.toString() + "Student{" +
                "courseInterested=" + courseInterested +
                '}';
    }
}

