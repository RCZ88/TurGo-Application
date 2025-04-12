package com.example.turgo;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Meeting implements Serializable {
    private final String meetingID;
    private static final String FIREBASE_DB_REFERENCE = "Meetings";
    private final RTDBManager<Meeting> rtdbManager;
    private Schedule meetingOfSchedule;
    private HashMap<Student, LocalTime> studentsAttended;
    private User preScheduledBy;
    private LocalDate dateOfMeeting;
    private LocalTime startTimeChange;
    private LocalTime endTimeChange;
    private Room roomChange;
    private boolean completed;
    private final Context context;


    public Meeting(Schedule meetingOfSchedule, LocalDate dateOfMeeting, User preScheduledBy, Context context){
        meetingID = UUID.randomUUID().toString();
        this.meetingOfSchedule = meetingOfSchedule;
        studentsAttended = new HashMap<>();
        this.dateOfMeeting = dateOfMeeting;
        startTimeChange = meetingOfSchedule.getMeetingStart(); // no time change
        endTimeChange = meetingOfSchedule.getMeetingEnd();
        this.preScheduledBy = preScheduledBy;
        roomChange = null;
        completed = false;
        rtdbManager = new RTDBManager<>();
        this.context = context;
        assignAlarmNotification(this.meetingOfSchedule.getStudents());
    }

    public void assignAlarmNotification(ArrayList<Student>students){
        LocalDateTime ldt = LocalDateTime.of(this.dateOfMeeting, startTimeChange);
        for(Student student : students){
            MeetingAlarm.setMeetingAlarm(this.context, ldt, this.getMeetingOfSchedule().getScheduleOfCourse(), student);
        }
    }

    public void doTimeChanges(LocalTime start, LocalTime end){
        this.startTimeChange = start;
        this.endTimeChange = end;
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void setMeetingEndAlarm(Context context, LocalDate date, LocalTime endTime, Student student, Meeting meeting){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        LocalDateTime dateTime = LocalDateTime.of(date, endTime);
        long millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Intent intent = new Intent(context, MeetingAlarmReciever.class);
        intent.putExtra("Student", student);
        intent.putExtra("Meeting", meeting);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    public void updateDB(Meeting meeting){
        rtdbManager.storeData(FIREBASE_DB_REFERENCE, meetingID, meeting, "Meeting", "Meeting");
    }
    public void changeDay(LocalDate date){
        this.dateOfMeeting = date;
    }

    public Schedule getMeetingOfSchedule() {
        return meetingOfSchedule;
    }

    public void setMeetingOfSchedule(Schedule meetingOfSchedule) {
        this.meetingOfSchedule = meetingOfSchedule;
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
                objectCallBack.onObjectRetrieved(meeting);
                Log.d("Firebase Data Retrieval", "Successfully Retrieved Meeting!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Database Error", "Error Retrieving Data of Meeting: "+ error);
            }
        });
    }
}
