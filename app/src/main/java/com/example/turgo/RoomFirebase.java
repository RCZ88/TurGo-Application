package com.example.turgo;

import java.util.ArrayList;

public class RoomFirebase implements FirebaseClass<Room>{
    private String roomId;
    private ArrayList<String> suitableCourseTypeIds; // Store course type IDs instead of full objects
    private boolean used;
    private String currentlyOccupiedMeetingId; // Store meeting ID instead of full Meeting object
    private ArrayList<String> scheduleIds; // Store schedule IDs instead of full objects

    public RoomFirebase() {
    }

    public RoomFirebase(String roomId, ArrayList<String> suitableCourseTypeIds,
                        boolean used, String currentlyOccupiedMeetingId,
                        ArrayList<String> scheduleIds) {
        this.roomId = roomId;
        this.suitableCourseTypeIds = suitableCourseTypeIds;
        this.used = used;
        this.currentlyOccupiedMeetingId = currentlyOccupiedMeetingId;
        this.scheduleIds = scheduleIds;
    }
    @Override
    public void importObjectData(Room from) {
        this.roomId = from.getID();
        this.suitableCourseTypeIds = convertToIdList(from.getSuitableCourseType());
        this.used = from.isUsed();
        // Store only Meeting ID instead of full object
        this.currentlyOccupiedMeetingId = from.getCurrentlyOccupiedBy().getID();
        this.scheduleIds = convertToIdList(from.getSchedules());
    }

    @Override
    public String getID() {
        return roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public ArrayList<String> getSuitableCourseTypeIds() {
        return suitableCourseTypeIds;
    }

    public void setSuitableCourseTypeIds(ArrayList<String> suitableCourseTypeIds) {
        this.suitableCourseTypeIds = suitableCourseTypeIds;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getCurrentlyOccupiedMeetingId() {
        return currentlyOccupiedMeetingId;
    }

    public void setCurrentlyOccupiedMeetingId(String currentlyOccupiedMeetingId) {
        this.currentlyOccupiedMeetingId = currentlyOccupiedMeetingId;
    }

    public ArrayList<String> getScheduleIds() {
        return scheduleIds;
    }

    public void setScheduleIds(ArrayList<String> scheduleIds) {
        this.scheduleIds = scheduleIds;
    }
}
