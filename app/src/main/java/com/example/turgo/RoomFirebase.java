package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class RoomFirebase implements FirebaseClass<Room>{
    private String roomId;
    private ArrayList<String> suitableCourseType; // Store course type IDs instead of full objects
    private boolean used;
    private String currentlyOccupiedBy; // Store meeting ID instead of full Meeting object
    private ArrayList<String> schedules; // Store schedule IDs instead of full objects

    public RoomFirebase() {
    }

    public RoomFirebase(String roomId, ArrayList<String> suitableCourseType,
                        boolean used, String currentlyOccupiedBy,
                        ArrayList<String> schedules) {
        this.roomId = roomId;
        this.suitableCourseType = suitableCourseType;
        this.used = used;
        this.currentlyOccupiedBy = currentlyOccupiedBy;
        this.schedules = schedules;
    }
    @Override
    public void importObjectData(Room from) {
        this.roomId = from.getID();
        this.suitableCourseType = convertToIdList(from.getSuitableCourseType());
        this.used = from.isUsed();
        // Store only Meeting ID instead of full object
        this.currentlyOccupiedBy = from.getCurrentlyOccupiedBy().getID();
        this.schedules = convertToIdList(from.getSchedules());
    }

    @Override
    public String getID() {
        return roomId;
    }
    @Override
    public void convertToNormal(ObjectCallBack<Room> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Room.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Room) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public ArrayList<String> getSuitableCourseType() {
        return suitableCourseType;
    }

    public void setSuitableCourseType(ArrayList<String> suitableCourseType) {
        this.suitableCourseType = suitableCourseType;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getCurrentlyOccupiedBy() {
        return currentlyOccupiedBy;
    }

    public void setCurrentlyOccupiedBy(String currentlyOccupiedBy) {
        this.currentlyOccupiedBy = currentlyOccupiedBy;
    }

    public ArrayList<String> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<String> schedules) {
        this.schedules = schedules;
    }
}
