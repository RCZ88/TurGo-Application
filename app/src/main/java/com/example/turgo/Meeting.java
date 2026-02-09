package com.example.turgo;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Meeting implements Serializable, RequireUpdate<Meeting, MeetingFirebase, MeetingRepository> {
    private final FirebaseNode fbn = FirebaseNode.MEETING;
    private final Class<MeetingFirebase> fbc = MeetingFirebase.class;
    private final String meetingID;
    private static final String FIREBASE_DB_REFERENCE = "Meetings";
    private HashMap<Student, LocalTime> studentsAttended;
    private User preScheduledBy;
    private String ofSchedule;
    private LocalDate dateOfMeeting;
    private LocalTime startTimeChange;
    private LocalTime endTimeChange;
    private Room roomChange;
    private boolean completed;
    private boolean alarmAssigned = false;  // NEW: Track if alarm set
    private LocalDateTime alarmAssignedAt;  // NEW: When alarm was assigned


    public Meeting(Schedule meetingOfSchedule, LocalDate dateOfMeeting, User preScheduledBy, String ofSchedule){
        meetingID = UUID.randomUUID().toString();
        studentsAttended = new HashMap<>();
        this.dateOfMeeting = dateOfMeeting;
        startTimeChange = meetingOfSchedule.getMeetingStart(); // no time change
        endTimeChange = meetingOfSchedule.getMeetingEnd();
        this.preScheduledBy = preScheduledBy;
        this.ofSchedule = ofSchedule;
        roomChange = null;
        completed = false;
    }
    public Meeting(String meetingID, Schedule meetingOfSchedule, LocalDate dateOfMeeting, User preScheduledBy){
        this.meetingID = meetingID;
        studentsAttended = new HashMap<>();
        this.dateOfMeeting = dateOfMeeting;
        startTimeChange = meetingOfSchedule.getMeetingStart(); // no time change
        endTimeChange = meetingOfSchedule.getMeetingEnd();
        this.preScheduledBy = preScheduledBy;
        roomChange = null;
        completed = false;
    }
    public Meeting(){
        meetingID = UUID.randomUUID().toString();

    }
    public static Task<Meeting> getClosestMeetingToSchedule(Schedule schedule) {
        TaskCompletionSource<Meeting> tcs = new TaskCompletionSource<>();

        MeetingRepository mr;
        try {
            mr = MeetingRepository.class.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            tcs.setException(e);
            return tcs.getTask();
        }

        mr.loadAll(new ObjectCallBack<>() {
            @Override
            public void onObjectRetrieved(ArrayList<MeetingFirebase> list) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {

                if (list.isEmpty()) {
                    tcs.setResult(null);
                    return;
                }

                AtomicReference<Meeting> closest = new AtomicReference<>(null);
                AtomicInteger pending = new AtomicInteger(list.size());
                AtomicBoolean failed = new AtomicBoolean(false);

                for (MeetingFirebase mf : list) {
                    mf.convertToNormal(new ObjectCallBack<>() {
                        @Override
                        public void onObjectRetrieved(Meeting meeting) {

                            meeting.getMeetingOfSchedule()
                                    .addOnSuccessListener(mos -> {

                                        if (mos == schedule) {
                                            Meeting current = closest.get();
                                            if (current == null ||
                                                    current.getDateOfMeeting()
                                                            .isAfter(meeting.getDateOfMeeting())) {
                                                closest.set(meeting);
                                            }
                                        }

                                        finishOne();
                                    })
                                    .addOnFailureListener(e -> failOnce(e));
                        }

                        @Override
                        public void onError(DatabaseError error) {
                            failOnce(error.toException());
                        }

                        private void finishOne() {
                            if (pending.decrementAndGet() == 0 && !failed.get()) {
                                tcs.setResult(closest.get());
                            }
                        }

                        private void failOnce(Exception e) {
                            if (failed.compareAndSet(false, true)) {
                                tcs.setException(e);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(DatabaseError error) {
                tcs.setException(error.toException());
            }
        });

        return tcs.getTask();
    }




    public String getOfSchedule() {
        return ofSchedule;
    }

    public void setOfSchedule(String ofSchedule) {
        this.ofSchedule = ofSchedule;
    }

    //students of the schedule.
    public void assignAlarmNotification(ArrayList<Student>students, Context context){
        LocalDateTime ldt = LocalDateTime.of(this.dateOfMeeting, startTimeChange);
        for(Student student : students){
//            Schedule schedule = Await.get(this::getMeetingOfSchedule);
//            Course course = Await.get(schedule::getScheduleOfCourse);
            AtomicReference<Schedule> schedule = new AtomicReference<>();
            getMeetingOfSchedule().continueWithTask(task ->{
                schedule.set(task.getResult());
                return schedule.get().getScheduleOfCourse();
            }).addOnSuccessListener(course ->{
                MeetingAlarm.setMeetingAlarm(context, ldt, course, student, schedule.get());
            });
        }
    }

    public Task<Schedule> getMeetingOfSchedule(){
        ScheduleRepository scheduleRepository = new ScheduleRepository(ofSchedule);
        return scheduleRepository.loadAsNormal();
    }

    public Task<Course> getMeetingOfCourse(){
        TaskCompletionSource<Course>taskCourse = new TaskCompletionSource<>();
        getMeetingOfSchedule().addOnSuccessListener(schedule->{
            schedule.getScheduleOfCourse().addOnSuccessListener(taskCourse::setResult);
        });
        return taskCourse.getTask();
    }

    public void doTimeChanges(LocalTime start, LocalTime end){
        this.startTimeChange = start;
        this.endTimeChange = end;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void setMeetingEndAlarm(Context context, LocalDate date, LocalTime endTime, Student student){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        LocalDateTime dateTime = LocalDateTime.of(date, endTime);
        long millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Intent intent = new Intent(context, MeetingAlarmReciever.class);
        intent.putExtra("Student", student);
        intent.putExtra("Meeting", this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE  // ✅ Fixed!
        );


        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis , pendingIntent);
        }
    }


    public User getPreScheduledBy() {
        return preScheduledBy;
    }

    public void setPreScheduledBy(User preScheduledBy) {
        this.preScheduledBy = preScheduledBy;
    }

    public LocalTime getEndTimeChange() {
        return endTimeChange;
    }

    public void setEndTimeChange(LocalTime endTimeChange) {
        this.endTimeChange = endTimeChange;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<MeetingRepository> getRepositoryClass() {
        return MeetingRepository.class;
    }

    @Override
    public Class<MeetingFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return this.meetingID;
    }

    public FirebaseNode getFbn() {
        return fbn;
    }

    public Class<MeetingFirebase> getFbc() {
        return fbc;
    }

    public boolean isAlarmAssigned() {
        return alarmAssigned;
    }

    public void setAlarmAssigned(boolean alarmAssigned) {
        this.alarmAssigned = alarmAssigned;
    }

    public LocalDateTime getAlarmAssignedAt() {
        return alarmAssignedAt;
    }

    public void setAlarmAssignedAt(LocalDateTime alarmAssignedAt) {
        this.alarmAssignedAt = alarmAssignedAt;
    }

    public void changeDay(LocalDate date){
        this.dateOfMeeting = date;
    }

//    public void getMeetingOfSchedule(ObjectCallBack<Schedule> callBack) {
//        // Try finding in Student's preScheduledMeetings or meetingHistory
//        // Since meetings can be in either Student or Teacher lists, we search both
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.STUDENT.getPath());
//        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean found = false;
//                for(DataSnapshot studentSnapshot : snapshot.getChildren()){
//                    // Check preScheduledMeetings
//                    DataSnapshot prescheduled = studentSnapshot.child("preScheduledMeetings");
//                    if(searchMeetingInList(prescheduled, callBack)){
//                        found = true;
//                        break;
//                    }
//                    // Check meetingHistory
//                    DataSnapshot history = studentSnapshot.child("meetingHistory");
//                    if(searchMeetingInList(history, callBack)){
//                        found = true;
//                        break;
//                    }
//                }
//                if(!found){
//                    // Search in Teacher if not found in Student
//                    searchInTeacher(callBack);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Meeting(getMeetingOfSchedule)", "Error finding Schedule: " + error);
//            }
//        });
//    }
    
    private boolean searchMeetingInList(DataSnapshot listSnapshot, ObjectCallBack<Schedule> callBack){
        for(DataSnapshot meetingSnapshot : listSnapshot.getChildren()){
            String meetingId = meetingSnapshot.child("meetingID").getValue(String.class);
            if(meetingId != null && meetingId.equals(this.meetingID)){
                DataSnapshot scheduleSnapshot = meetingSnapshot.child("meetingOfSchedule");
                Schedule schedule = scheduleSnapshot.getValue(Schedule.class);
                if(schedule != null){
                    try {
                        callBack.onObjectRetrieved(schedule);
                    } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                             IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    private void searchInTeacher(ObjectCallBack<Schedule> callBack){
        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.TEACHER.getPath());
        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot teacherSnapshot : snapshot.getChildren()){
                    DataSnapshot scheduled = teacherSnapshot.child("scheduledMeetings");
                    if(searchMeetingInList(scheduled, callBack)){
                        return;
                    }
                    DataSnapshot completed = teacherSnapshot.child("completedMeetings");
                    if(searchMeetingInList(completed, callBack)){
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Meeting(searchInTeacher)", "Error finding Schedule: " + error);
            }
        });
    }

    public HashMap<Student, LocalTime> getStudentsAttended() {
        return studentsAttended;
    }

    public void setStudentsAttended(HashMap<Student, LocalTime> studentsAttended) {
        this.studentsAttended = studentsAttended;
    }

    public LocalDate getDateOfMeeting() {
        return dateOfMeeting;
    }

    public void setDateOfMeeting(LocalDate dateOfMeeting) {
        this.dateOfMeeting = dateOfMeeting;
    }

    public LocalTime getStartTimeChange() {
        return startTimeChange;
    }

    public void setStartTimeChange(LocalTime startTimeChange) {
        this.startTimeChange = startTimeChange;
    }

    public Room getRoomChange() {
        return roomChange;
    }

    public void setRoomChange(Room roomChange) {
        this.roomChange = roomChange;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void addStudentAttendance(Student student, LocalTime time){
        studentsAttended.put(student, time);
    }
    public String getMeetingID(){
        return meetingID;
    }

    public static void getMeetingFromDB(String UID, ObjectCallBack<Meeting> objectCallBack){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FIREBASE_DB_REFERENCE).child(UID);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Meeting meeting = snapshot.getValue(Meeting.class);
                try {
                    objectCallBack.onObjectRetrieved(meeting);
                } catch (ParseException | InvocationTargetException | NoSuchMethodException |
                         IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
                Log.d("Firebase Data Retrieval", "Successfully Retrieved Meeting!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Database Error", "Error Retrieving Data of Meeting: "+ error);
            }
        });
    }
}
