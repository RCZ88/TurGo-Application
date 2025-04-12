package com.example.turgo;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

public class DayTimeArrangement {
    private Teacher ofTeacher;
    private Course atCourse;
    private DayOfWeek day;
    private LocalTime start;//start teaching
    private LocalTime end;//end teaching for the day
    private ArrayList<Schedule> occupied;
    private int maxMeeting;

    public DayTimeArrangement(Teacher ofTeacher, Course atCourse, DayOfWeek day, LocalTime start, LocalTime end, int maxMeeting) {
        this.ofTeacher = ofTeacher;
        this.atCourse = atCourse;
        this.day = day;
        this.start = start;
        this.end = end;
        occupied = atCourse.getScheduleOfDay(day);
        this.maxMeeting = maxMeeting;
    }
    public ArrayList<TimeSlot> findFreeSlots(boolean isPrivate, int maxPeople, int duration){
        ArrayList<TimeSlot> freeSlots = new ArrayList<>();

        occupied.sort(Comparator.comparing(Schedule::getMeetingStart));

        LocalTime currentTime = start;

        for(Schedule s : occupied){
            if(isPrivate){
                if(currentTime.isBefore(s.getMeetingStart())){
                    freeSlots.add(new TimeSlot(day, currentTime, s.getMeetingStart(), null));
                }
            }else{
                if(currentTime.isBefore(s.getMeetingStart())) {
                    freeSlots.add(new TimeSlot(day, currentTime, s.getMeetingStart(), null));
                    if(s.getNumberOfStudents() < maxPeople && !s.isPrivate()){
                        freeSlots.add(new TimeSlot(day, s.getMeetingStart(), s.getMeetingEnd(), s));
                    }
                }
            }
            currentTime = s.getMeetingEnd();
        }

        if(currentTime.isBefore(end)){
            freeSlots.add(new TimeSlot(day, currentTime, end, null));
        }
        for(TimeSlot ts : freeSlots){
            if(Tool.isShorter(ts.getTime(), Duration.ofMinutes(duration))){
                freeSlots.remove(ts);
            }
        }
        return freeSlots;
    }
    public void applySchedule(LocalTime start, int duration, Student student, boolean paymentPreferences, int cost){//private
        ArrayList<TimeSlot> freeSlots = findFreeSlots(true, 1, duration);
        for(TimeSlot ts : freeSlots){
            if(isTimeEqualOrBetween(start, start.plus(Duration.ofMinutes(duration)), ts.getStart(), ts.getEnd())){
                Schedule schedule = new Schedule(atCourse, start, duration ,day, Room.getEmptyRoom(start, start.plus(Duration.ofMinutes(duration)), day), true);
                schedule.addStudent(student);
                atCourse.addStudent(student, paymentPreferences, false, cost, schedule);
                atCourse.addSchedule(schedule);
                occupied.add(schedule);
                break;
            }
        }
    }

    public void applySchedule(LocalTime start, int duration, int maxPeople, Student student, boolean paymentPreferences, int cost){
        ArrayList<TimeSlot>freeSlots = findFreeSlots(false, maxPeople, duration);
        for(TimeSlot timeSlot : freeSlots){
            if(isTimeEqualOrBetween(start, start.plus(Duration.ofMinutes(duration)), timeSlot.getStart(), timeSlot.getEnd())){
                boolean found = false;
                for(Schedule schedule : occupied){
                    if(!(schedule.isPrivate()) && schedule.getMeetingStart().equals(start)){
                        System.out.println("Schedule found! Adding student...");
                        schedule.addStudent(student);
                        atCourse.addStudent(student, paymentPreferences, true, cost, schedule);
                        System.out.println("Student Successfully Added");
                        found = true;
                        break;
                    }
                }
                if(!found){
                    System.out.println("No Existing Schedule found of that Time! Creating a new Schedule");
                    Schedule newSchedule = new Schedule(atCourse, start, duration, day, Room.getEmptyRoom(start, start.plus(Duration.ofMinutes(duration)), day), false);
                    atCourse.addStudent(student, paymentPreferences, true, cost, newSchedule);
                    newSchedule.addStudent(student);
                    atCourse.addSchedule(newSchedule);
                    occupied.add(newSchedule);
                    System.out.println("Schedule successfully created!");
                    break;
                }
            }
        }
    }
    public ArrayList<TimeSlot>splitSlots(int duration, ArrayList<TimeSlot>emptySlots){
        Duration d = Duration.ofMinutes(duration);
        ArrayList<TimeSlot>timeSlots = new ArrayList<>();
        for(TimeSlot ts : emptySlots){
            if(Tool.isShorter(d, ts.getTime())){
                int amount = (int) (ts.getTime().getSeconds()/d.getSeconds());
                LocalTime currentStart = ts.getStart();
                for(int i = 0; i<amount; i++){
                    LocalTime end = currentStart.plus(Duration.ofMinutes(duration));
                    timeSlots.add(new TimeSlot(day, currentStart, end, ts.getExistingSchedule()));
                    currentStart = end;
                }
            }
        }
        return timeSlots;
    }
    private boolean isTimeEqualOrBetween(LocalTime checkStart, LocalTime checkEnd, LocalTime start, LocalTime end){
        return ((checkStart.equals(start) || (checkStart.isAfter(start) && checkStart.isBefore(end))) && (checkEnd.isBefore(end) || checkEnd.equals(end) && checkEnd.isAfter(start)));
    }

    public ArrayList<Schedule> getOccupied() {
        return occupied;
    }

    public Teacher getOfTeacher() {
        return ofTeacher;
    }

    public void setOfTeacher(Teacher ofTeacher) {
        this.ofTeacher = ofTeacher;
    }

    public Course getAtCourse() {
        return atCourse;
    }

    public void setAtCourse(Course atCourse) {
        this.atCourse = atCourse;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }

    public int getMaxMeeting() {
        return maxMeeting;
    }

    public void setMaxMeeting(int maxMeeting) {
        this.maxMeeting = maxMeeting;
    }
}
