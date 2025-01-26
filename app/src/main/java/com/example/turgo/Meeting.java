package com.example.turgo;


import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Meeting {
    private Schedule scheduleOf;
    private ArrayList<Student> studentsAttended;
    private LocalDate dateOfMeeting;
    private LocalTime startTimeChange;
    private Room roomChange;
    private boolean completed;

    public Meeting(Schedule scheduleOf, ArrayList<Student> studentsAttended, LocalDate dateOfMeeting){
        this.scheduleOf = scheduleOf;
        this.studentsAttended = studentsAttended;
        this.dateOfMeeting = dateOfMeeting;
        startTimeChange = null;
        roomChange = null;
        completed = false;
    }
    public void changeDay(LocalDate date){
        this.dateOfMeeting = date;
    }

    public Schedule getScheduleOf() {
        return scheduleOf;
    }

    public void setScheduleOf(Schedule scheduleOf) {
        this.scheduleOf = scheduleOf;
    }

    public ArrayList<Student> getStudentsAttended() {
        return studentsAttended;
    }

    public void setStudentsAttended(ArrayList<Student> studentsAttended) {
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

    public void addStudentAttendance(Student student){
        studentsAttended.add(student);
    }
}
