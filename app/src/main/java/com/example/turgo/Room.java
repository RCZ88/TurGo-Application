package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

public class Room implements RequireUpdate<Room, RoomFirebase>{
    private final FirebaseNode fbn = FirebaseNode.ROOM;
    private final Class<RoomFirebase> fbc = RoomFirebase.class;
    private static RTDBManager<RoomFirebase>roomRTDB;
    private String roomId;
    private ArrayList<CourseType>suitableCourseType;
    private boolean used;
    private Meeting currentlyOccupiedBy;
    private ArrayList<Schedule>schedules;//schedules that uses this room;


    public Room(String roomId){
        this.roomId = roomId;
        this.suitableCourseType = new ArrayList<>();
        this.used = false;
        this.currentlyOccupiedBy = null;
        this.schedules = new ArrayList<>();
    }

    public static void assignRoomForSchedule(Room room, Schedule schedule){
        room.schedules.add(schedule);
    }

    public static void useRoom(Room room, Meeting meeting){
        room.setCurrentlyOccupiedBy(meeting);
    }


    public static Room getEmptyRoom(LocalTime timeStart, LocalTime timeEnd, DayOfWeek day){

        for(Room room : ObjectManager.ROOMS){
            if(!room.getSchedules().isEmpty()){
                for(Schedule schedule : room.getSchedules()){
                    if(!Tool.isTimeOccupied(timeStart, timeEnd, day, schedule)){
                        if(room.suitableCourseType.contains(schedule.getScheduleOfCourse().getCourseType())){
                            return room;
                        }
                    }
                }
            }
            return room;
        }
        return null;
    }

    public Meeting getCurrentlyOccupiedBy() {
        return currentlyOccupiedBy;
    }

    public void setCurrentlyOccupiedBy(Meeting currentlyOccupiedBy) {
        this.currentlyOccupiedBy = currentlyOccupiedBy;
    }

    public ArrayList<CourseType> getSuitableCourseType() {
        return suitableCourseType;
    }

    public void setSuitableCourseType(ArrayList<CourseType> suitableCourseType) {
        this.suitableCourseType = suitableCourseType;
    }

    public ArrayList<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<Schedule> schedules) {
        this.schedules = schedules;
    }


    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }


    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<RoomFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return roomId;
    }
}
