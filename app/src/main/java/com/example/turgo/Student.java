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
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class Student extends User implements Serializable, RequireUpdate<Student, StudentFirebase, StudentRepository>{
    private static final String CLOSEST_MEETING_TAG = "ClosestMeetingDebug";
    private static final FirebaseNode fbn = FirebaseNode.STUDENT;
    private static final Class<StudentFirebase> fbc = StudentFirebase.class;
    private ArrayList<Course> courseTaken;
    private ArrayList<String> courseTakenIds;
    private ArrayList<StudentCourse>studentCourseTaken;
    private ArrayList<String> studentCourseTakenIds;
    public static final String SERIALIZE_KEY_CODE = "students";
    private ArrayList<String> courseInterested; //coursetype
    private ArrayList<Course> courseRelated; //courses related to the coursetype
    private ArrayList<String> courseRelatedIds;
    private ArrayList<Meeting> preScheduledMeetings;
    private ArrayList<String> preScheduledMeetingsIds;
    private ArrayList<Meeting> meetingHistory;
    private ArrayList<String> meetingHistoryIds;
    private ArrayList<Schedule> scheduleCompletedThisWeek;
    private ArrayList<String> scheduleCompletedThisWeekIds;
    private String completionWeekKey;
    private double percentageCompleted;
    private Meeting nextMeeting;
    private String nextMeetingId;
    private String school;
    private String gradeLevel;
    private ArrayList<Schedule> allSchedules;
    private ArrayList<String> allSchedulesIds;
    private LocalDate lastScheduled;
    private boolean hasScheduled;
    private int autoSchedule;
    private Duration notificationEarlyDuration; //how many minute before the meeting (notification)
    private ArrayList<Task> allTask;
    private ArrayList<String> allTaskIds;
    private ArrayList<Task> uncompletedTask;
    private ArrayList<String> uncompletedTaskIds;
    private ArrayList<Task> manualCompletedTask;
    private ArrayList<String> manualCompletedTaskIds;
    private ArrayList<Task> manualMissedTask;
    private ArrayList<String> manualMissedTaskIds;
    private ArrayList<Agenda> allAgendas;
    private ArrayList<String> allAgendasIds;
    private int lateAttendance;
    private int lateSubmissions;

    public Student(String fullName, String gender, String birthDate, String nickname, String email, String phoneNumber) throws ParseException {
        super(UserType.STUDENT, gender, fullName, birthDate, nickname, email, phoneNumber);
        courseTaken = new ArrayList<>();
        courseTakenIds = new ArrayList<>();
        studentCourseTaken = new ArrayList<>();
        studentCourseTakenIds = new ArrayList<>();
        courseInterested = new ArrayList<>();
        courseRelated = new ArrayList<>();
        courseRelatedIds = new ArrayList<>();
        preScheduledMeetings = new ArrayList<>();
        preScheduledMeetingsIds = new ArrayList<>();
        allSchedules = new ArrayList<>();
        allSchedulesIds = new ArrayList<>();
        meetingHistory = new ArrayList<>();
        meetingHistoryIds = new ArrayList<>();
        scheduleCompletedThisWeek = new ArrayList<>();
        scheduleCompletedThisWeekIds = new ArrayList<>();
        completionWeekKey = getCurrentWeekKey();
        allTask = new ArrayList<>();
        allTaskIds = new ArrayList<>();
        uncompletedTask = new ArrayList<>();
        uncompletedTaskIds = new ArrayList<>();
        manualCompletedTask = new ArrayList<>();
        manualCompletedTaskIds = new ArrayList<>();
        manualMissedTask = new ArrayList<>();
        manualMissedTaskIds = new ArrayList<>();
        allAgendas = new ArrayList<>();
        allAgendasIds = new ArrayList<>();
        percentageCompleted = 0;
        school = "";
        gradeLevel = "";
        notificationEarlyDuration = Duration.ofMinutes(30);
        hasScheduled = false;
        autoSchedule = 1;
        nextMeeting = null;
        nextMeetingId = null;
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
    public ArrayList<Schedule>getSchedulesOfCourse(Course course){
        ArrayList<Schedule>schedules = new ArrayList<>();
        for(Schedule schedule : allSchedules){
            if(schedule.getOfCourse().equals(course.getCourseID())){
                schedules.add(schedule);
            }
        }
        return schedules;
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

    public Student(){
        this.courseTaken = new ArrayList<>();
        this.courseTakenIds = new ArrayList<>();
        this.studentCourseTaken = new ArrayList<>();
        this.studentCourseTakenIds = new ArrayList<>();
        this.courseInterested = new ArrayList<>();
        this.courseRelated = new ArrayList<>();
        this.courseRelatedIds = new ArrayList<>();
        this.preScheduledMeetings = new ArrayList<>();
        this.preScheduledMeetingsIds = new ArrayList<>();
        this.meetingHistory = new ArrayList<>();
        this.meetingHistoryIds = new ArrayList<>();
        this.scheduleCompletedThisWeek = new ArrayList<>();
        this.scheduleCompletedThisWeekIds = new ArrayList<>();
        this.completionWeekKey = getCurrentWeekKey();
        this.allSchedules = new ArrayList<>();
        this.allSchedulesIds = new ArrayList<>();
        this.allTask = new ArrayList<>();
        this.allTaskIds = new ArrayList<>();
        this.uncompletedTask = new ArrayList<>();
        this.uncompletedTaskIds = new ArrayList<>();
        this.manualCompletedTask = new ArrayList<>();
        this.manualCompletedTaskIds = new ArrayList<>();
        this.manualMissedTask = new ArrayList<>();
        this.manualMissedTaskIds = new ArrayList<>();
        this.allAgendas = new ArrayList<>();
        this.allAgendasIds = new ArrayList<>();
        this.nextMeetingId = null;
    }

    @Override
    public String getSerializeCode() {
        return SERIALIZE_KEY_CODE;
    }

    private interface Loader<T> {
        com.google.android.gms.tasks.Task<T> load(String id);
    }

    private <T extends RequireUpdate<?, ?, ?>> ArrayList<String> syncIdsFromObjects(ArrayList<String> ids, ArrayList<T> objects) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        if (objects != null) {
            for (T object : objects) {
                if (object == null || !Tool.boolOf(object.getID())) {
                    continue;
                }
                if (!ids.contains(object.getID())) {
                    ids.add(object.getID());
                }
            }
        }
        return ids;
    }

    private <T> com.google.android.gms.tasks.Task<ArrayList<T>> loadByIds(ArrayList<String> ids, Loader<T> loader) {
        TaskCompletionSource<ArrayList<T>> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(ids)) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }
        ArrayList<com.google.android.gms.tasks.Task<T>> tasks = new ArrayList<>();
        for (String id : ids) {
            if (!Tool.boolOf(id)) {
                continue;
            }
            tasks.add(loader.load(id).continueWith(task -> task.isSuccessful() ? task.getResult() : null));
        }
        if (tasks.isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }
        Tasks.whenAllComplete(tasks).addOnCompleteListener(done -> {
            ArrayList<T> result = new ArrayList<>();
            for (com.google.android.gms.tasks.Task<T> task : tasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    result.add(task.getResult());
                }
            }
            tcs.setResult(result);
        });
        return tcs.getTask();
    }

    public ArrayList<Task>getCompletedTask(){
        ArrayList<Task> completed = new ArrayList<>();
        if (allTask == null || allTask.isEmpty()) {
            return completed;
        }

        HashSet<String> uncompletedIds = new HashSet<>();
        HashSet<String> manualCompletedIds = new HashSet<>();
        HashSet<String> manualMissedIds = new HashSet<>();
        if (uncompletedTask != null) {
            for (Task task : uncompletedTask) {
                if (task != null && Tool.boolOf(task.getID())) {
                    uncompletedIds.add(task.getID());
                }
            }
        }
        if (manualCompletedTask != null) {
            for (Task task : manualCompletedTask) {
                if (task != null && Tool.boolOf(task.getID())) {
                    manualCompletedIds.add(task.getID());
                }
            }
        }
        if (manualMissedTask != null) {
            for (Task task : manualMissedTask) {
                if (task != null && Tool.boolOf(task.getID())) {
                    manualMissedIds.add(task.getID());
                }
            }
        }

        for (Task task : allTask) {
            if (task == null || !Tool.boolOf(task.getID())) {
                continue;
            }
            boolean withDropbox = Tool.boolOf(task.getDropbox());
            if (withDropbox && !uncompletedIds.contains(task.getID())) {
                completed.add(task);
            } else if (!withDropbox
                    && manualCompletedIds.contains(task.getID())
                    && !manualMissedIds.contains(task.getID())) {
                completed.add(task);
            }
        }
        return completed;
    }

    public TaskStatus resolveTaskStatus(Task task, LocalDateTime now){
        if (task == null || !Tool.boolOf(task.getID())) {
            return TaskStatus.UNCOMPLETED;
        }
        LocalDateTime refNow = now != null ? now : LocalDateTime.now();
        boolean isPast = task.getDueDate() != null && task.getDueDate().isBefore(refNow);
        String taskId = task.getID();
        boolean withDropbox = Tool.boolOf(task.getDropbox());

        if (withDropbox) {
            return isTaskInList(uncompletedTask, taskId) ? TaskStatus.UNCOMPLETED : TaskStatus.COMPLETED;
        }
        if (isTaskInList(manualCompletedTask, taskId)) {
            return TaskStatus.COMPLETED;
        }
        if (isTaskInList(manualMissedTask, taskId)) {
            return TaskStatus.UNCOMPLETED;
        }
        if (isPast) {
            return TaskStatus.UNMARKED;
        }
        return TaskStatus.UNCOMPLETED;
    }

    public ArrayList<Task> getCurrentTasks() {
        ArrayList<Task> currentTasks = new ArrayList<>();
        if (allTask == null) {
            return currentTasks;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Task task : allTask) {
            if (task == null) {
                continue;
            }
            if (task.getDueDate() == null || !task.getDueDate().isBefore(now)) {
                currentTasks.add(task);
            }
        }
        return currentTasks;
    }

    public ArrayList<Task> getPastTasks() {
        ArrayList<Task> pastTasks = new ArrayList<>();
        if (allTask == null) {
            return pastTasks;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Task task : allTask) {
            if (task == null || task.getDueDate() == null) {
                continue;
            }
            if (task.getDueDate().isBefore(now)) {
                pastTasks.add(task);
            }
        }
        return pastTasks;
    }

    public void markManualTaskStatus(Task task, boolean completed){
        if (task == null || !Tool.boolOf(task.getID())) {
            return;
        }
        if (manualCompletedTask == null) {
            manualCompletedTask = new ArrayList<>();
        }
        if (manualMissedTask == null) {
            manualMissedTask = new ArrayList<>();
        }
        String taskId = task.getID();
        manualCompletedTask.removeIf(t -> t != null && Tool.boolOf(t.getID()) && t.getID().equals(taskId));
        manualMissedTask.removeIf(t -> t != null && Tool.boolOf(t.getID()) && t.getID().equals(taskId));
        if (completed) {
            manualCompletedTask.add(task);
        } else {
            manualMissedTask.add(task);
        }
    }

    private boolean isTaskInList(ArrayList<Task> list, String taskId) {
        if (!Tool.boolOf(taskId) || list == null || list.isEmpty()) {
            return false;
        }
        for (Task task : list) {
            if (task != null && Tool.boolOf(task.getID()) && task.getID().equals(taskId)) {
                return true;
            }
        }
        return false;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getAllMeetingOfCourse(Course course) {
        return getAllMeetingOfCourse(course, 0);
    }

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getAllMeetingOfCourse(Course course, int limit){
        TaskCompletionSource<ArrayList<Meeting>> tcs = new TaskCompletionSource<>();

        if (course == null || !Tool.boolOf(course.getID())) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }

        String targetCourseId = course.getID();
        ArrayList<Meeting> meetingCandidates = new ArrayList<>();
        HashSet<String> seenMeetingIds = new HashSet<>();

        addMeetingCandidates(preScheduledMeetings, meetingCandidates, seenMeetingIds);
        addMeetingCandidates(meetingHistory, meetingCandidates, seenMeetingIds);

        if (meetingCandidates.isEmpty()) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }

        ArrayList<com.google.android.gms.tasks.Task<Meeting>> matchTasks = new ArrayList<>();
        for (Meeting meeting : meetingCandidates) {
            com.google.android.gms.tasks.Task<Meeting> matchTask = meeting.getMeetingOfCourse()
                    .continueWith(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("Student", "Skipping meeting with failed course lookup: " + meeting.getID(), task.getException());
                            return null;
                        }

                        Course meetingCourse = task.getResult();
                        if (meetingCourse == null || !Tool.boolOf(meetingCourse.getID())) {
                            return null;
                        }
                        if (targetCourseId.equals(meetingCourse.getID())) {
                            return meeting;
                        }
                        return null;
                    });
            matchTasks.add(matchTask);
        }

        Tasks.whenAllComplete(matchTasks).addOnCompleteListener(done -> {
            ArrayList<Meeting> meetings = new ArrayList<>();
            for (com.google.android.gms.tasks.Task<Meeting> task : matchTasks) {
                if (task.isSuccessful() && task.getResult() != null) {
                    meetings.add(task.getResult());
                } else if (task.getException() != null) {
                    Log.w("Student", "Meeting filter task failed", task.getException());
                }
            }
            meetings.sort((a, b) -> {
                LocalDate aDate = a != null ? a.getDateOfMeeting() : null;
                LocalDate bDate = b != null ? b.getDateOfMeeting() : null;
                if (aDate == null && bDate == null) {
                    LocalTime aStart = a != null ? a.getStartTimeChange() : null;
                    LocalTime bStart = b != null ? b.getStartTimeChange() : null;
                    if (aStart == null && bStart == null) {
                        return 0;
                    }
                    if (aStart == null) {
                        return 1;
                    }
                    if (bStart == null) {
                        return -1;
                    }
                    return bStart.compareTo(aStart);
                }
                if (aDate == null) {
                    return 1;
                }
                if (bDate == null) {
                    return -1;
                }
                int byDateDesc = bDate.compareTo(aDate);
                if (byDateDesc != 0) {
                    return byDateDesc;
                }
                LocalTime aStart = a.getStartTimeChange();
                LocalTime bStart = b.getStartTimeChange();
                if (aStart == null && bStart == null) {
                    return 0;
                }
                if (aStart == null) {
                    return 1;
                }
                if (bStart == null) {
                    return -1;
                }
                return bStart.compareTo(aStart);
            });

            if (limit > 0 && meetings.size() > limit) {
                meetings = new ArrayList<>(meetings.subList(0, limit));
            }
            tcs.setResult(meetings);
        });

        return tcs.getTask();
    }

    private void addMeetingCandidates(ArrayList<Meeting> source,
                                      ArrayList<Meeting> out,
                                      HashSet<String> seenMeetingIds) {
        if (source == null || source.isEmpty()) {
            return;
        }
        for (Meeting meeting : source) {
            if (meeting == null || !Tool.boolOf(meeting.getID())) {
                continue;
            }
            if (seenMeetingIds.add(meeting.getID())) {
                out.add(meeting);
            }
        }
    }
    public void calculatePercentageCompleted(){
        if (allSchedules == null || allSchedules.isEmpty()) {
            this.percentageCompleted = 0;
            return;
        }
        int amountOfSchedules = allSchedules.size();
        int completed = getCompletedScheduleCountThisWeek();
        this.percentageCompleted = (double) completed /amountOfSchedules * 100;
    }
    public void setNextMeeting(){
        Meeting closestUpcoming = null;
        if (preScheduledMeetings == null || preScheduledMeetings.isEmpty()) {
            this.nextMeeting = null;
            this.nextMeetingId = null;
            return;
        }
        for (Meeting meeting : preScheduledMeetings) {
            if (!isUpcomingMeeting(meeting)) {
                continue;
            }
            if (closestUpcoming == null || isEarlierMeeting(meeting, closestUpcoming)) {
                closestUpcoming = meeting;
            }
        }
        this.nextMeeting = closestUpcoming;
        this.nextMeetingId = closestUpcoming != null ? closestUpcoming.getID() : null;
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
        int safeAutoSchedule = Math.max(1, Math.min(autoSchedule, 52));
        if (safeAutoSchedule != autoSchedule) {
            Log.w("Student", "autoSchedule clamped from " + autoSchedule + " to " + safeAutoSchedule);
        }

        for (int i = 0; i < safeAutoSchedule; i++) {
            for (Schedule schedule : allSchedules) {
                if (schedule.isHasScheduled()) {
                    continue;
                }

                LocalDate meetingDate = today.plusWeeks(i).with(
                        TemporalAdjusters.nextOrSame(schedule.getDay())
                );
                if (meetingDate.getYear() < 0 || meetingDate.getYear() > 9999) {
                    Log.w("Student", "Skipping invalid meetingDate: " + meetingDate);
                    continue;
                }

                String meetingId = schedule.getID() + "_" + meetingDate;
                Meeting meeting = new Meeting(meetingId, schedule, meetingDate, this);

                try {
                    meeting.setMeetingEndAlarm(context, meeting.getDateOfMeeting(),
                            meeting.getEndTimeChange(), this);

                    MeetingRepository meetingRepo = new MeetingRepository(meetingId);
                    meetingRepo.save(meeting);

                    preScheduledMeetings.add(meeting);

                    // ✅ Use passed-in course and teacher (no Await.get needed!)
                    teacher.addScheduledMeeting(meeting);

                    schedule.getStudents().addOnSuccessListener(studentsList ->{
                        for (Student student : studentsList) {
                            student.preScheduledMeetings.add(meeting);
                            StudentRepository sr = new StudentRepository(student.getID());
                            sr.addStringToArrayAsync("preScheduledMeetings", meetingId);
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
        int safeAutoSchedule = Math.max(1, Math.min(autoSchedule, 52));
        if (safeAutoSchedule != autoSchedule) {
            Log.w("Student", "autoSchedule clamped from " + autoSchedule + " to " + safeAutoSchedule);
        }

        for (int i = 0; i < safeAutoSchedule; i++) {
            for (Schedule schedule : allSchedules) {
                if (schedule.isHasScheduled()) {
                    continue;
                }

                LocalDate meetingDate = today.plusWeeks(i).with(
                        TemporalAdjusters.nextOrSame(schedule.getDay())
                );
                if (meetingDate.getYear() < 0 || meetingDate.getYear() > 9999) {
                    Log.w("Student", "Skipping invalid meetingDate: " + meetingDate);
                    continue;
                }

                String meetingId = schedule.getID() + "_" + meetingDate;
                Meeting meeting = new Meeting(meetingId, schedule, meetingDate, this);

                try {
                    meeting.setMeetingEndAlarm(context, meeting.getDateOfMeeting(),
                            meeting.getEndTimeChange(), this);
                } catch (Exception e) {
                    return Tasks.forException(e);
                }

                MeetingRepository meetingRepo = new MeetingRepository(meetingId);
                tasks.add(logTask("meeting.save:" + meetingId, meetingRepo.saveAsync(meeting)));

                preScheduledMeetings.add(meeting);

                TeacherRepository teacherRepository = new TeacherRepository(teacher.getID());
                tasks.add(logTask("teacher.addScheduledMeeting:" + meetingId,
                        teacherRepository.addStringToArrayAsync("scheduledMeetings", meetingId)));
                teacher.addScheduledMeeting(meeting);

                com.google.android.gms.tasks.Task<List<Student>> studentsTask = schedule.getStudents();
                com.google.android.gms.tasks.Task<Void> addToStudentsTask = studentsTask.onSuccessTask(studentsList -> {
                    List<com.google.android.gms.tasks.Task<?>> studentTasks = new ArrayList<>();
                    for (Student student : studentsList) {
                        student.preScheduledMeetings.add(meeting);
                        StudentRepository sr = new StudentRepository(student.getID());
                        studentTasks.add(sr.addStringToArrayAsync("preScheduledMeetings", meetingId));
                    }
                    return Tasks.whenAll(studentTasks);
                });
                tasks.add(logTask("students.addPreScheduledMeeting:" + meetingId, addToStudentsTask));

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



    public com.google.android.gms.tasks.Task<LocalDate> getClosestMeetingOfCourse(Course course){
        Log.d(CLOSEST_MEETING_TAG, "getClosestMeetingOfCourse called. studentId=" + getID());
        if (course == null) {
            Log.w(CLOSEST_MEETING_TAG, "course is null");
            return Tasks.forResult(null);
        }
        Log.d(CLOSEST_MEETING_TAG, "courseId=" + course.getID() + ", courseName=" + course.getCourseName());

        ArrayList<String> courseScheduleIds = new ArrayList<>();
        if (course.getSchedules() != null) {
            for (Schedule schedule : course.getSchedules()) {
                if (schedule != null && Tool.boolOf(schedule.getID())) {
                    courseScheduleIds.add(schedule.getID());
                }
            }
        }
        Log.d(CLOSEST_MEETING_TAG, "courseScheduleIds=" + courseScheduleIds);

        if (courseScheduleIds.isEmpty()) {
            Log.w(CLOSEST_MEETING_TAG, "No schedule IDs found on course object. Resolving schedule IDs from DB by courseId.");
            return findClosestMeetingByCourseIdFromDatabase(course.getID());
        }

        if (preScheduledMeetings == null || preScheduledMeetings.isEmpty()) {
            Log.w(CLOSEST_MEETING_TAG, "preScheduledMeetings empty in-memory. Falling back to DB.");
            return findClosestMeetingByScheduleIdsFromDatabase(courseScheduleIds);
        }
        Log.d(CLOSEST_MEETING_TAG, "preScheduledMeetings count=" + preScheduledMeetings.size());

        LocalDate today = LocalDate.now();
        Meeting closest = null;

        for (Meeting meeting : preScheduledMeetings) {
            if (meeting == null || !Tool.boolOf(meeting.getOfSchedule()) || meeting.getDateOfMeeting() == null) {
                Log.d(CLOSEST_MEETING_TAG, "Skip meeting: null/invalid schedule/date");
                continue;
            }
            Log.d(CLOSEST_MEETING_TAG, "Check meeting id=" + meeting.getID()
                    + ", scheduleId=" + meeting.getOfSchedule()
                    + ", date=" + meeting.getDateOfMeeting()
                    + ", start=" + meeting.getStartTimeChange());
            if (!courseScheduleIds.contains(meeting.getOfSchedule())) {
                Log.d(CLOSEST_MEETING_TAG, "Skip meeting " + meeting.getID() + ": schedule not in course schedules");
                continue;
            }
            if (meeting.getDateOfMeeting().isBefore(today)) {
                Log.d(CLOSEST_MEETING_TAG, "Skip meeting " + meeting.getID() + ": date is in the past");
                continue;
            }
            if (closest == null || isEarlierMeeting(meeting, closest)) {
                Log.d(CLOSEST_MEETING_TAG, "New closest candidate: " + meeting.getID());
                closest = meeting;
            }
        }
        if (closest != null) {
            Log.d(CLOSEST_MEETING_TAG, "Resolved in-memory closest meeting date=" + closest.getDateOfMeeting()
                    + ", meetingId=" + closest.getID());
            return Tasks.forResult(closest.getDateOfMeeting());
        }
        Log.w(CLOSEST_MEETING_TAG, "No in-memory closest meeting found. Falling back to DB.");
        return findClosestMeetingByScheduleIdsFromDatabase(courseScheduleIds);

    }

    private com.google.android.gms.tasks.Task<LocalDate> findClosestMeetingByScheduleIdsFromDatabase(ArrayList<String> scheduleIds) {
        Log.d(CLOSEST_MEETING_TAG, "DB fallback started. scheduleIds=" + scheduleIds);
        TaskCompletionSource<LocalDate> tcs = new TaskCompletionSource<>();
        DatabaseReference meetingsRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.MEETING.getPath());
        String studentId = getID();
        LocalDate today = LocalDate.now();
        if (scheduleIds == null || scheduleIds.isEmpty()) {
            Log.w(CLOSEST_MEETING_TAG, "DB fallback aborted: empty scheduleIds");
            tcs.setResult(null);
            return tcs.getTask();
        }

        meetingsRef.get()
                .addOnSuccessListener(snapshot -> {
                    Log.d(CLOSEST_MEETING_TAG, "DB fallback meetings snapshot count=" + snapshot.getChildrenCount());
                    final LocalDate[] bestDate = new LocalDate[1];
                    final LocalTime[] bestStart = new LocalTime[1];

                    for (DataSnapshot child : snapshot.getChildren()) {
                        MeetingFirebase mf = child.getValue(MeetingFirebase.class);
                        if (mf == null || !Tool.boolOf(mf.getOfSchedule()) || !Tool.boolOf(mf.getDateOfMeeting())) {
                            continue;
                        }
                        String dbKey = child.getKey();

                        LocalDate date;
                        try {
                            date = LocalDate.parse(mf.getDateOfMeeting());
                        } catch (Exception ignored) {
                            Log.d(CLOSEST_MEETING_TAG, "Skip DB meeting " + dbKey + ": invalid date format " + mf.getDateOfMeeting());
                            continue;
                        }
                        if (date.isBefore(today)) {
                            continue;
                        }

                        boolean relatedByUser = studentId.equals(mf.getPreScheduledBy())
                                || (mf.getUsersRelated() != null && mf.getUsersRelated().contains(studentId));
                        if (!relatedByUser) {
                            continue;
                        }
                        if (!scheduleIds.contains(mf.getOfSchedule())) {
                            Log.d(CLOSEST_MEETING_TAG, "Skip DB meeting " + dbKey + ": schedule " + mf.getOfSchedule() + " not in course schedules");
                            continue;
                        }

                        LocalTime startTime;
                        try {
                            startTime = Tool.boolOf(mf.getStartTimeChange())
                                    ? LocalTime.parse(mf.getStartTimeChange())
                                    : LocalTime.MIN;
                        } catch (Exception ignored) {
                            startTime = LocalTime.MIN;
                        }
                        Log.d(CLOSEST_MEETING_TAG, "DB meeting candidate key=" + dbKey
                                + ", schedule=" + mf.getOfSchedule()
                                + ", date=" + date
                                + ", start=" + startTime);
                        if (bestDate[0] == null
                                || date.isBefore(bestDate[0])
                                || (date.equals(bestDate[0]) && startTime.isBefore(bestStart[0]))) {
                            bestDate[0] = date;
                            bestStart[0] = startTime;
                            Log.d(CLOSEST_MEETING_TAG, "DB closest updated to date=" + bestDate[0] + ", start=" + bestStart[0]);
                        }
                    }
                    Log.d(CLOSEST_MEETING_TAG, "DB fallback resolved bestDate=" + bestDate[0]);
                    tcs.setResult(bestDate[0]);
                })
                .addOnFailureListener(e -> {
                    Log.e(CLOSEST_MEETING_TAG, "DB fallback failed", e);
                    tcs.setException(e);
                });

        return tcs.getTask();
    }

    private com.google.android.gms.tasks.Task<LocalDate> findClosestMeetingByCourseIdFromDatabase(String courseId) {
        TaskCompletionSource<LocalDate> tcs = new TaskCompletionSource<>();
        if (!Tool.boolOf(courseId)) {
            tcs.setResult(null);
            return tcs.getTask();
        }

        FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.SCHEDULE.getPath())
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<String> scheduleIds = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        ScheduleFirebase scheduleFirebase = child.getValue(ScheduleFirebase.class);
                        if (scheduleFirebase == null) {
                            continue;
                        }
                        if (!courseId.equals(scheduleFirebase.getOfCourse())) {
                            continue;
                        }
                        String scheduleId = child.getKey();
                        if (Tool.boolOf(scheduleId)) {
                            scheduleIds.add(scheduleId);
                        }
                    }

                    if (scheduleIds.isEmpty()) {
                        Log.w(CLOSEST_MEETING_TAG, "No schedules found in DB for courseId=" + courseId);
                        tcs.setResult(null);
                        return;
                    }

                    findClosestMeetingByScheduleIdsFromDatabase(scheduleIds)
                            .addOnSuccessListener(tcs::setResult)
                            .addOnFailureListener(tcs::setException);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    private boolean isEarlierMeeting(Meeting candidate, Meeting current) {
        if (candidate.getDateOfMeeting().isBefore(current.getDateOfMeeting())) {
            return true;
        }
        if (candidate.getDateOfMeeting().isAfter(current.getDateOfMeeting())) {
            return false;
        }
        if (candidate.getStartTimeChange() == null) {
            return false;
        }
        if (current.getStartTimeChange() == null) {
            return true;
        }
        return candidate.getStartTimeChange().isBefore(current.getStartTimeChange());
    }


    public void completeMeeting(Meeting meeting){
        if (meeting == null || !Tool.boolOf(meeting.getID())) {
            return;
        }
        String meetingId = meeting.getID();
        StudentRepository studentRepository = new StudentRepository(getID());
        preScheduledMeetings.remove(meeting);
        if (!meetingHistory.contains(meeting)) {
            meetingHistory.add(meeting);
        }
        studentRepository.removeStringFromArrayAsync("preScheduledMeetings", meetingId);
        studentRepository.addStringToArrayAsync("meetingHistory", meetingId);
        MeetingRepository meetingRepository = new MeetingRepository(meetingId);
        meetingRepository.updateCompleted(true);
//        Schedule schedule = Await.get(meeting::getMeetingOfSchedule);
        meeting.getMeetingOfSchedule().addOnSuccessListener(schedule ->{
            if (schedule == null) {
                return;
            }
            addCompletedScheduleForCurrentWeek(schedule);
            studentRepository.addScheduleCompletedThisWeekForCurrentWeek(schedule.getID(), getCurrentWeekKey());

            schedule.getScheduleOfCourse().addOnSuccessListener(course -> {
                if (course == null || !Tool.boolOf(course.getTeacherId())) {
                    return;
                }
                TeacherRepository teacherRepository = new TeacherRepository(course.getTeacherId());
                teacherRepository.removeStringFromArrayAsync("scheduledMeetings", meetingId);
                teacherRepository.addStringToArrayAsync("completedMeetings", meetingId);
            });
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
        if (course == null || !Tool.boolOf(course.getID()) || !Tool.boolOf(getID())) {
            return Tasks.forException(new IllegalArgumentException("Invalid student/course while joining"));
        }

        tasks.add(logTask("course.addStudentAsync:" + course.getID(),
                course.addStudentAsync(this, paymentPreferences, privateOrGroup, payment, schedules, timeSlot)));
        Log.d(tag, "course add student started");

        StudentRepository studentRepository = new StudentRepository(getID());
        if (courseTaken == null) {
            courseTaken = new ArrayList<>();
        }
        boolean alreadyInMemory = false;
        for (Course taken : courseTaken) {
            if (taken != null && Tool.boolOf(taken.getID()) && taken.getID().equals(course.getID())) {
                alreadyInMemory = true;
                break;
            }
        }
        if (!alreadyInMemory) {
            courseTaken.add(course);
        }
        tasks.add(logTask("student.ensureCourseTaken:" + course.getID(),
                studentRepository.addStringToArrayAsync("courseTaken", course.getID())));

        if (schedules == null) {
            schedules = new ArrayList<>();
        }
        if (allSchedules == null) {
            allSchedules = new ArrayList<>();
        }
        allSchedules.addAll(schedules);
        for (Schedule schedule : schedules) {
            ScheduleRepository scheduleRepository = new ScheduleRepository(schedule.getID());

            tasks.add(logTask("student.addAllSchedules:" + schedule.getID(),
                    studentRepository.addStringToArrayAsync("allSchedules", schedule.getID())));
            tasks.add(logTask("schedule.save:" + schedule.getID(), scheduleRepository.saveAsync(schedule)));
            tasks.add(scheduleRepository.addStringToArrayAsync("students", this.getID()));
        }
        Log.d(tag, "all Schedules queued for database");

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
        getRepositoryInstance().addStringToArray("allAgendas", agenda.getID());
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

    public ArrayList<String> getStudentCourseTakenIds() {
        studentCourseTakenIds = syncIdsFromObjects(studentCourseTakenIds, studentCourseTaken);
        return studentCourseTakenIds;
    }

    public void setStudentCourseTakenIds(ArrayList<String> studentCourseTakenIds) {
        this.studentCourseTakenIds = studentCourseTakenIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<StudentCourse>> getStudentCourseTakenTask() {
        return loadByIds(getStudentCourseTakenIds(), id -> new StudentCourseRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> studentCourseTaken = objects);
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

    public ArrayList<String> getCourseTakenIds() {
        courseTakenIds = syncIdsFromObjects(courseTakenIds, courseTaken);
        return courseTakenIds;
    }

    public void setCourseTakenIds(ArrayList<String> courseTakenIds) {
        this.courseTakenIds = courseTakenIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Course>> getCourseTakenTask() {
        return loadByIds(getCourseTakenIds(), id -> new CourseRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> courseTaken = objects);
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

    public ArrayList<String> getCourseRelatedIds() {
        courseRelatedIds = syncIdsFromObjects(courseRelatedIds, courseRelated);
        return courseRelatedIds;
    }

    public void setCourseRelatedIds(ArrayList<String> courseRelatedIds) {
        this.courseRelatedIds = courseRelatedIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Course>> getCourseRelatedTask() {
        return loadByIds(getCourseRelatedIds(), id -> new CourseRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> courseRelated = objects);
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
        if (preScheduledMeetings == null) {
            preScheduledMeetings = new ArrayList<>();
            return preScheduledMeetings;
        }
        preScheduledMeetings.removeIf(meeting -> !isUpcomingMeeting(meeting));
        return preScheduledMeetings;
    }

    public ArrayList<String> getPreScheduledMeetingsIds() {
        preScheduledMeetingsIds = syncIdsFromObjects(preScheduledMeetingsIds, preScheduledMeetings);
        return preScheduledMeetingsIds;
    }

    public void setPreScheduledMeetingsIds(ArrayList<String> preScheduledMeetingsIds) {
        this.preScheduledMeetingsIds = preScheduledMeetingsIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getPreScheduledMeetingsTask() {
        return loadByIds(getPreScheduledMeetingsIds(), id -> new MeetingRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> preScheduledMeetings = objects);
    }

    public void setPreScheduledMeetings(ArrayList<Meeting> preScheduledMeetings) {
        this.preScheduledMeetings = preScheduledMeetings;
    }

    public ArrayList<Meeting> getMeetingHistory() {
        return meetingHistory;
    }

    public ArrayList<String> getMeetingHistoryIds() {
        meetingHistoryIds = syncIdsFromObjects(meetingHistoryIds, meetingHistory);
        return meetingHistoryIds;
    }

    public void setMeetingHistoryIds(ArrayList<String> meetingHistoryIds) {
        this.meetingHistoryIds = meetingHistoryIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Meeting>> getMeetingHistoryTask() {
        return loadByIds(getMeetingHistoryIds(), id -> new MeetingRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> meetingHistory = objects);
    }
    public void addMeetingHistory(Meeting meeting){
        this.meetingHistory.add(meeting);
    }

    public void setMeetingHistory(ArrayList<Meeting> meetingHistory) {
        this.meetingHistory = meetingHistory;
    }

    public ArrayList<Schedule> getAllSchedules() {
        return allSchedules;
    }

    public ArrayList<String> getAllSchedulesIds() {
        allSchedulesIds = syncIdsFromObjects(allSchedulesIds, allSchedules);
        return allSchedulesIds;
    }

    public void setAllSchedulesIds(ArrayList<String> allSchedulesIds) {
        this.allSchedulesIds = allSchedulesIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Schedule>> getAllSchedulesTask() {
        return loadByIds(getAllSchedulesIds(), id -> new ScheduleRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> allSchedules = objects);
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
            if (schedule == null) {
                return;
            }
            addCompletedScheduleForCurrentWeek(schedule);
            meeting.setCompleted(true);
        });
    }

    private void addCompletedScheduleForCurrentWeek(Schedule schedule) {
        if (schedule == null || !Tool.boolOf(schedule.getID())) {
            return;
        }
        if (scheduleCompletedThisWeek == null) {
            scheduleCompletedThisWeek = new ArrayList<>();
        }
        String currentWeek = getCurrentWeekKey();
        if (!Tool.boolOf(completionWeekKey) || !completionWeekKey.equals(currentWeek)) {
            completionWeekKey = currentWeek;
            scheduleCompletedThisWeek.clear();
            percentageCompleted = 0;
        }
        for (Schedule existing : scheduleCompletedThisWeek) {
            if (existing != null && Tool.boolOf(existing.getID()) && existing.getID().equals(schedule.getID())) {
                return;
            }
        }
        scheduleCompletedThisWeek.add(schedule);
    }

    public ArrayList<Schedule> getScheduleCompletedThisWeek() {
        refreshWeeklyCompletionState();
        if (allSchedules == null || allSchedules.isEmpty()) {
            scheduleCompletedThisWeek = new ArrayList<>();
            return scheduleCompletedThisWeek;
        }
        HashSet<String> completedIds = getCompletedScheduleIdsThisWeek();
        ArrayList<Schedule> rebuilt = new ArrayList<>();
        for (Schedule schedule : allSchedules) {
            if (schedule == null || !Tool.boolOf(schedule.getID())) {
                continue;
            }
            if (completedIds.contains(schedule.getID())) {
                rebuilt.add(schedule);
            }
        }
        scheduleCompletedThisWeek = rebuilt;
        return scheduleCompletedThisWeek;
    }

    public ArrayList<String> getScheduleCompletedThisWeekIds() {
        scheduleCompletedThisWeekIds = syncIdsFromObjects(scheduleCompletedThisWeekIds, scheduleCompletedThisWeek);
        return scheduleCompletedThisWeekIds;
    }

    public void setScheduleCompletedThisWeekIds(ArrayList<String> scheduleCompletedThisWeekIds) {
        this.scheduleCompletedThisWeekIds = scheduleCompletedThisWeekIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Schedule>> getScheduleCompletedThisWeekTask() {
        return loadByIds(getScheduleCompletedThisWeekIds(), id -> new ScheduleRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> scheduleCompletedThisWeek = objects);
    }

    public void setScheduleCompletedThisWeek(ArrayList<Schedule> scheduleCompletedThisWeek) {
        this.scheduleCompletedThisWeek = scheduleCompletedThisWeek != null
                ? scheduleCompletedThisWeek
                : new ArrayList<>();
    }

    public String getCompletionWeekKey() {
        refreshWeeklyCompletionState();
        return completionWeekKey;
    }

    public void setCompletionWeekKey(String completionWeekKey) {
        this.completionWeekKey = completionWeekKey;
    }

    public static String getCurrentWeekKey() {
        return getWeekKey(LocalDate.now());
    }

    private static String getWeekKey(LocalDate date) {
        if (date == null) {
            return "";
        }
        WeekFields wf = WeekFields.ISO;
        int weekBasedYear = date.get(wf.weekBasedYear());
        int week = date.get(wf.weekOfWeekBasedYear());
        return String.format(Locale.US, "%d-W%02d", weekBasedYear, week);
    }

    private void refreshWeeklyCompletionState() {
        if (scheduleCompletedThisWeek == null) {
            scheduleCompletedThisWeek = new ArrayList<>();
        }
        String currentWeek = getCurrentWeekKey();
        if (!Tool.boolOf(completionWeekKey) || !completionWeekKey.equals(currentWeek)) {
            completionWeekKey = currentWeek;
            scheduleCompletedThisWeek.clear();
            percentageCompleted = 0;
        }
    }

    public int getCompletedScheduleCountThisWeek() {
        refreshWeeklyCompletionState();
        return getCompletedScheduleIdsThisWeek().size();
    }

    public ArrayList<String> getCompletedScheduleIdsThisWeekList() {
        refreshWeeklyCompletionState();
        return new ArrayList<>(getCompletedScheduleIdsThisWeek());
    }

    private HashSet<String> getCompletedScheduleIdsThisWeek() {
        HashSet<String> ids = new HashSet<>();
        if (meetingHistory == null || meetingHistory.isEmpty()) {
            return ids;
        }
        String currentWeek = getCurrentWeekKey();
        for (Meeting meeting : meetingHistory) {
            if (meeting == null) {
                continue;
            }
            LocalDate meetingDate = meeting.getDateOfMeeting();
            if (meetingDate == null || !currentWeek.equals(getWeekKey(meetingDate))) {
                continue;
            }
            String scheduleId = meeting.getOfSchedule();
            if (Tool.boolOf(scheduleId)) {
                ids.add(scheduleId);
            }
        }
        return ids;
    }

    public Meeting getNextMeeting() {
        if (nextMeeting == null) {
            setNextMeeting();
            return nextMeeting;
        }
        if (!isUpcomingMeeting(nextMeeting)) {
            Meeting fallback = nextMeeting;
            setNextMeeting();
            if (nextMeeting == null) {
                nextMeeting = fallback;
            }
        }
        return nextMeeting;
    }

    public String getNextMeetingId() {
        if (!Tool.boolOf(nextMeetingId) && nextMeeting != null && Tool.boolOf(nextMeeting.getID())) {
            nextMeetingId = nextMeeting.getID();
        }
        return nextMeetingId;
    }

    public void setNextMeetingId(String nextMeetingId) {
        this.nextMeetingId = nextMeetingId;
    }

    public com.google.android.gms.tasks.Task<Meeting> getNextMeetingTask() {
        if (!Tool.boolOf(getNextMeetingId())) {
            return Tasks.forResult(null);
        }
        return new MeetingRepository(getNextMeetingId()).loadAsNormal().addOnSuccessListener(object -> nextMeeting = object);
    }

    public void setNextMeeting(Meeting nextMeeting) {
        this.nextMeeting = isUpcomingMeeting(nextMeeting) ? nextMeeting : null;
        this.nextMeetingId = this.nextMeeting != null ? this.nextMeeting.getID() : null;
    }

    private boolean isUpcomingMeeting(Meeting meeting) {
        if (meeting == null || meeting.isCompleted() || meeting.getDateOfMeeting() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (meeting.getDateOfMeeting().isAfter(today)) {
            return true;
        }
        if (meeting.getDateOfMeeting().isBefore(today)) {
            return false;
        }
        LocalTime end = meeting.getEndTimeChange();
        return end == null || end.isAfter(LocalTime.now());
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

    public ArrayList<String> getAllTaskIds() {
        allTaskIds = syncIdsFromObjects(allTaskIds, allTask);
        return allTaskIds;
    }

    public void setAllTaskIds(ArrayList<String> allTaskIds) {
        this.allTaskIds = allTaskIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Task>> getAllTaskTask() {
        return loadByIds(getAllTaskIds(), id -> new TaskRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> allTask = objects);
    }

    public void setAllTask(ArrayList<Task> allTask) {
        this.allTask = allTask;
    }

    public ArrayList<Task> getUncompletedTask() {
        if(!Tool.boolOf(uncompletedTask)){
            for (Task task : getAllTask()) {
                if(!task.isComplete(this)){
                    getRepositoryInstance().addStringToArray("uncompletedTask", task.getTaskID());
                    uncompletedTask.add(task);
                }
            }
        }
        if(uncompletedTask == null){
            uncompletedTask = new ArrayList<>();
        }
        return uncompletedTask;
    }

    public ArrayList<String> getUncompletedTaskIds() {
        uncompletedTaskIds = syncIdsFromObjects(uncompletedTaskIds, uncompletedTask);
        return uncompletedTaskIds;
    }

    public void setUncompletedTaskIds(ArrayList<String> uncompletedTaskIds) {
        this.uncompletedTaskIds = uncompletedTaskIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Task>> getUncompletedTaskTask() {
        return loadByIds(getUncompletedTaskIds(), id -> new TaskRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> uncompletedTask = objects);
    }

    public void setUncompletedTask(ArrayList<Task> uncompletedTask) {
        this.uncompletedTask = uncompletedTask;
    }

    public ArrayList<Task> getManualCompletedTask() {
        return manualCompletedTask;
    }

    public ArrayList<String> getManualCompletedTaskIds() {
        manualCompletedTaskIds = syncIdsFromObjects(manualCompletedTaskIds, manualCompletedTask);
        return manualCompletedTaskIds;
    }

    public void setManualCompletedTaskIds(ArrayList<String> manualCompletedTaskIds) {
        this.manualCompletedTaskIds = manualCompletedTaskIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Task>> getManualCompletedTaskTask() {
        return loadByIds(getManualCompletedTaskIds(), id -> new TaskRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> manualCompletedTask = objects);
    }

    public void setManualCompletedTask(ArrayList<Task> manualCompletedTask) {
        this.manualCompletedTask = manualCompletedTask;
    }

    public ArrayList<Task> getManualMissedTask() {
        return manualMissedTask;
    }

    public ArrayList<String> getManualMissedTaskIds() {
        manualMissedTaskIds = syncIdsFromObjects(manualMissedTaskIds, manualMissedTask);
        return manualMissedTaskIds;
    }

    public void setManualMissedTaskIds(ArrayList<String> manualMissedTaskIds) {
        this.manualMissedTaskIds = manualMissedTaskIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Task>> getManualMissedTaskTask() {
        return loadByIds(getManualMissedTaskIds(), id -> new TaskRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> manualMissedTask = objects);
    }

    public void setManualMissedTask(ArrayList<Task> manualMissedTask) {
        this.manualMissedTask = manualMissedTask;
    }
    public ArrayList<Task>getAllTaskOfCourse(Course course){
        if(uncompletedTask == null){
            uncompletedTask = new ArrayList<>();
            return uncompletedTask;
        }
        ArrayList<Task>tasks = new ArrayList<>();
        for(Task task : uncompletedTask){
            if (task != null
                    && course != null
                    && Tool.boolOf(task.getTaskOfCourse())
                    && task.getTaskOfCourse().equals(course.getID())) {
                tasks.add(task);
            }
        }
        return tasks;
    }
    public void getAgendaOfCourse(Course course, ObjectCallBack<ArrayList<Agenda>>callBack){
        ArrayList<Agenda> result = new ArrayList<>();
        String targetCourseId = course != null ? course.getID() : null;
        if (!Tool.boolOf(targetCourseId)) {
            try {
                callBack.onObjectRetrieved(result);
            } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        ArrayList<Agenda> source = new ArrayList<>();
        if (Tool.boolOf(allAgendas)) {
            source.addAll(allAgendas);
        } else if (course != null && Tool.boolOf(course.getAgendas())) {
            source.addAll(course.getAgendas());
        }

        LinkedHashSet<String> seenAgendaIds = new LinkedHashSet<>();
        String studentId = getID();
        for (Agenda agenda : source) {
            if (agenda == null || !Tool.boolOf(agenda.getID())) {
                continue;
            }

            String agendaCourseId = agenda.getOfCourseId();
            if (!Tool.boolOf(agendaCourseId) || !agendaCourseId.equals(targetCourseId)) {
                continue;
            }

            if (Tool.boolOf(studentId)
                    && agenda.getStudent() != null
                    && Tool.boolOf(agenda.getStudent().getID())
                    && !studentId.equals(agenda.getStudent().getID())) {
                continue;
            }

            if (seenAgendaIds.add(agenda.getID())) {
                result.add(agenda);
            }
        }

        result.sort(Comparator.comparing(Agenda::getDate, Comparator.nullsLast(LocalDate::compareTo)));
        try {
            callBack.onObjectRetrieved(result);
        } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
    public void addAgenda(Agenda agenda){
        allAgendas.add(agenda);
    }
    public ArrayList<Agenda> getAllAgendas() {
        return allAgendas;
    }

    public ArrayList<String> getAllAgendasIds() {
        allAgendasIds = syncIdsFromObjects(allAgendasIds, allAgendas);
        return allAgendasIds;
    }

    public void setAllAgendasIds(ArrayList<String> allAgendasIds) {
        this.allAgendasIds = allAgendasIds;
    }

    public com.google.android.gms.tasks.Task<ArrayList<Agenda>> getAllAgendasTask() {
        return loadByIds(getAllAgendasIds(), id -> new AgendaRepository(id).loadAsNormal())
                .addOnSuccessListener(objects -> allAgendas = objects);
    }

    public void setAllAgendas(ArrayList<Agenda> allAgendas) {
        this.allAgendas = allAgendas;
    }

    public void assignTask(Task task) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        allTask.add(task);
        uncompletedTask.add(task);
        if (manualCompletedTask != null) {
            manualCompletedTask.removeIf(t -> t != null && Tool.boolOf(t.getID()) && Tool.boolOf(task.getID()) && t.getID().equals(task.getID()));
        }
        if (manualMissedTask != null) {
            manualMissedTask.removeIf(t -> t != null && Tool.boolOf(t.getID()) && Tool.boolOf(task.getID()) && t.getID().equals(task.getID()));
        }
        StudentRepository sr = new StudentRepository(this.getID());
        sr.addStringToArray("allTask", task.getTaskID());
        sr.addStringToArray("uncompletedTask", task.getTaskID());
    }

    public void getLatestAgendaOfCourse(Course course, ObjectCallBack<Agenda>callBack){
        getAgendaOfCourse(course, new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(ArrayList<Agenda> object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                Agenda latest = null;
                for (Agenda agenda : object) {
                    if (agenda == null || agenda.getDate() == null) {
                        continue;
                    }
                    if (latest == null || latest.getDate() == null || agenda.getDate().isAfter(latest.getDate())) {
                        latest = agenda;
                    }
                }
                if (latest == null && Tool.boolOf(object)) {
                    latest = object.get(object.size() - 1);
                }
                callBack.onObjectRetrieved(latest);
            }

            @Override
            public void onError(DatabaseError error) {
                callBack.onError(error);
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
        if (!Tool.boolOf(getID())) {
            tcs.setResult(new ArrayList<>());
            return tcs.getTask();
        }

        DatabaseReference studentRef = FirebaseDatabase.getInstance()
                .getReference(FirebaseNode.STUDENT.getPath())
                .child(getID());

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<String> takenIds = new HashSet<>(extractIdList(snapshot.child("courseTaken")));
                ArrayList<String> interestedRaw = extractIdList(snapshot.child("courseInterested"));
                HashSet<String> interestedNames = new HashSet<>();
                for (String interest : interestedRaw) {
                    if (Tool.boolOf(interest)) {
                        interestedNames.add(interest.trim().toLowerCase(Locale.US));
                    }
                }

                loadExploreCoursesFromDb(interestedNames, takenIds, tcs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tcs.setException(error.toException());
            }
        });

        return tcs.getTask();
    }

    private void loadExploreCoursesFromDb(HashSet<String> interestedNames,
                                          HashSet<String> takenIds,
                                          TaskCompletionSource<ArrayList<Course>> tcs) {
        DatabaseReference courseTypeRef = FirebaseDatabase.getInstance()
                .getReference(CourseType.fbn.getPath());

        courseTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, String> typeNameById = new HashMap<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    CourseTypeFirebase ctf = ds.getValue(CourseTypeFirebase.class);
                    if (ctf == null || !Tool.boolOf(ctf.getID())) {
                        continue;
                    }
                    String typeName = ctf.getCourseType();
                    typeNameById.put(ctf.getID(), Tool.boolOf(typeName) ? typeName.trim().toLowerCase(Locale.US) : "");
                }

                DatabaseReference coursesRef = FirebaseDatabase.getInstance()
                        .getReference(FirebaseNode.COURSE.getPath());

                coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        LinkedHashSet<String> candidateCourseIds = new LinkedHashSet<>();
                        for (DataSnapshot courseSnap : snapshot.getChildren()) {
                            CourseFirebase cf = courseSnap.getValue(CourseFirebase.class);
                            if (cf == null) {
                                continue;
                            }

                            String courseId = Tool.boolOf(cf.getCourseID()) ? cf.getCourseID() : courseSnap.getKey();
                            if (!Tool.boolOf(courseId) || takenIds.contains(courseId)) {
                                continue;
                            }

                            if (!interestedNames.isEmpty()) {
                                String typeId = cf.getCourseType();
                                String typeName = Tool.boolOf(typeId) ? typeNameById.get(typeId) : "";
                                if (!Tool.boolOf(typeName) || !interestedNames.contains(typeName)) {
                                    continue;
                                }
                            }

                            candidateCourseIds.add(courseId);
                        }

                        if (candidateCourseIds.isEmpty()) {
                            tcs.setResult(new ArrayList<>());
                            return;
                        }

                        ArrayList<com.google.android.gms.tasks.Task<Course>> courseTasks = new ArrayList<>();
                        for (String courseId : candidateCourseIds) {
                            courseTasks.add(
                                    new CourseRepository(courseId).loadLite().continueWith(task -> {
                                        if (task.isSuccessful()) {
                                            return task.getResult();
                                        }
                                        Log.w("Student", "Skipping failed explore course load id=" + courseId, task.getException());
                                        return null;
                                    })
                            );
                        }

                        Tasks.whenAllComplete(courseTasks).addOnCompleteListener(done -> {
                            ArrayList<Course> result = new ArrayList<>();
                            for (com.google.android.gms.tasks.Task<Course> task : courseTasks) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    result.add(task.getResult());
                                } else if (task.getException() != null) {
                                    Log.w("Student", "Explore course task failed", task.getException());
                                }
                            }
                            tcs.setResult(result);
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
    }

    private ArrayList<String> extractIdList(DataSnapshot listSnapshot) {
        LinkedHashSet<String> deduped = new LinkedHashSet<>();
        if (listSnapshot == null || !listSnapshot.exists()) {
            return new ArrayList<>();
        }

        for (DataSnapshot child : listSnapshot.getChildren()) {
            Object raw = child.getValue();
            if (raw instanceof String && Tool.boolOf((String) raw)) {
                deduped.add((String) raw);
                continue;
            }
            String key = child.getKey();
            if (!Tool.boolOf(key)) {
                continue;
            }
            if (!isNumericKey(key) && isTruthyFlag(raw)) {
                deduped.add(key);
            }
        }
        return new ArrayList<>(deduped);
    }

    private boolean isNumericKey(String key) {
        if (!Tool.boolOf(key)) {
            return false;
        }
        for (int i = 0; i < key.length(); i++) {
            if (!Character.isDigit(key.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTruthyFlag(Object raw) {
        if (raw == null) {
            return true;
        }
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue() != 0;
        }
        String value = raw.toString();
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }



    public void submitTask(ArrayList<file>filesSelected, Task task) {
        Dropbox dropbox = task.getDropboxCached();
        if (dropbox == null) {
            throw new IllegalStateException("Task dropbox is not loaded yet.");
        }
        for(file file: filesSelected){
            file.setSubmitTime(LocalDateTime.now());
            file.setOfTask(task);
            if (!Tool.boolOf(file.getSecureURL())) {
                Log.e("Student", "Skipping file save because secureURL is empty for file=" + file.getID());
                continue;
            }

            FileRepository fileRepository = new FileRepository(file.getID());
            fileRepository.save(file);
            Submission slot = dropbox.getSubmissionSlot(this);
            if (slot == null) {
                Log.e("Student", "No submission slot found for student=" + getID() + ", task=" + task.getID());
                continue;
            }
            slot.setDropbox(dropbox.getID());
            if (slot.getOf() == null || !Tool.boolOf(slot.getOf().getID())) {
                slot.setOf(this);
            }
            SubmissionRepository slotRepository = new SubmissionRepository(slot.getID());
            slotRepository.setDropbox(dropbox.getID());
            slotRepository.setStudent(this);
            slot.isLate(file.getSubmitTime()).addOnSuccessListener(late-> {
                boolean safeLate = late != null && late;
                if (safeLate) {
                    lateSubmissions++;
                    getRepositoryInstance().incrementLateSubmissions(1);
                }
                task.submit(file, this, safeLate);
            }).addOnFailureListener(e -> Log.e("Student", "Failed to compute lateness for file=" + file.getID(), e));


        }
        ArrayList<String>fileNames = new ArrayList<>();
        boolean hasLateStatus = false;
        for(file file: filesSelected){
            fileNames.add(file.getFileName());
            LocalDateTime submitTime = file.getSubmitTime();
            if (submitTime == null) {
                submitTime = file.getFileCreateDate();
            }
            if (submitTime != null && task.getDueDate() != null && submitTime.isAfter(task.getDueDate())) {
                hasLateStatus = true;
            }
        }
        final boolean finalHasLateStatus = hasLateStatus;
        task.getTaskOfCourseObject().addOnSuccessListener(course -> {
            if (course == null) {
                return;
            }
            SubmissionDisplay sd = new SubmissionDisplay(
                    getFullName(),
                    task.getTitle(),
                    course.getCourseName(),
                    task.getDueDate() != null ? task.getDueDate().toString() : "",
                    fileNames,
                    finalHasLateStatus
            );
            course.getTeacher().addOnSuccessListener(teacher ->{
                if (teacher != null) {
                    String studentName = Tool.boolOf(getFullName()) ? getFullName() : "A student";
                    String taskTitle = Tool.boolOf(task.getTitle()) ? task.getTitle() : "a task";
                    String courseName = Tool.boolOf(course.getCourseName()) ? course.getCourseName() : "your course";
                    String title = "New Student Submission";
                    String content = studentName + " submitted '" + taskTitle + "' in " + courseName + ".";
                    NotificationService.notifyUser(teacher, this, title, content)
                            .addOnFailureListener(e -> Log.w("Student", "Failed sending teacher submission notification", e));
                    teacher.addLatestSubmission(sd);
                }
                uncompletedTask.removeIf(t ->
                        t != null
                                && Tool.boolOf(t.getTaskID())
                                && t.getTaskID().equals(task.getTaskID()));
                StudentRepository sr = new StudentRepository(getID());
                sr.removeUncompletedTask(task.getTaskID());
            });
        });

    }

    @Override
    public String toString() {
        return "Student{" +
                "courseTaken=" + courseTaken +
                ", studentCourseTaken=" + studentCourseTaken +
                ", courseInterested=" + courseInterested +
                ", courseRelated=" + courseRelated +
                ", preScheduledMeetings=" + preScheduledMeetings +
                ", meetingHistory=" + meetingHistory +
                ", scheduleCompletedThisWeek=" + scheduleCompletedThisWeek +
                ", percentageCompleted=" + percentageCompleted +
                ", nextMeeting=" + nextMeeting +
                ", school='" + school + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", allSchedules=" + allSchedules +
                ", lastScheduled=" + lastScheduled +
                ", hasScheduled=" + hasScheduled +
                ", autoSchedule=" + autoSchedule +
                ", notificationEarlyDuration=" + notificationEarlyDuration +
                ", allTask=" + allTask +
                ", uncompletedTask=" + uncompletedTask +
                ", manualCompletedTask=" + manualCompletedTask +
                ", manualMissedTask=" + manualMissedTask +
                ", allAgendas=" + allAgendas +
                ", lateAttendance=" + lateAttendance +
                ", lateSubmissions=" + lateSubmissions +
                '}';
    }
}


