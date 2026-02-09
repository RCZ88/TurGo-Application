package com.example.turgo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

public class Room implements Serializable, RequireUpdate<Room, RoomFirebase, RoomRepository>{
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


    public static void getEmptyRoom(LocalTime timeStart, LocalTime timeEnd, DayOfWeek day, ObjectCallBack<Room> callback) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseNode.ROOM.getPath());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> snapshots = snapshot.getChildren();
                boolean roomFound = false;

                // Iterate through all rooms
                for (DataSnapshot s : snapshots) {
                    Room room = s.getValue(Room.class);
                    if (room == null) {
                        continue;
                    }

                    // Check if room has no schedules
                    if (room.getSchedules() == null || room.getSchedules().isEmpty()) {
                        try {
                            callback.onObjectRetrieved(room);
                        } catch (ParseException | InvocationTargetException |
                                 NoSuchMethodException | IllegalAccessException |
                                 InstantiationException e) {
                            // Handle exception properly
                            try {
                                // Try to call error callback if possible
                                // This depends on your ObjectCallBack implementation
                                Log.e("Room", "Error in callback for empty room", e);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        return; // Found a room, exit early
                    }

                    // Check if room has suitable schedules
                    boolean roomIsSuitable = true;
                    for (Schedule schedule : room.getSchedules()) {
                        if (Tool.isTimeOccupied(timeStart, timeEnd, day, schedule)) {
                            // Time is occupied, room not suitable
                            roomIsSuitable = false;
                            break;
                        }

                        // Check course type compatibility
                        try {
                            Course course = Await.get(schedule::getScheduleOfCourse);
                            if (course == null || !room.suitableCourseType.contains(course.getCourseType())) {
                                roomIsSuitable = false;
                                break;
                            }
                        } catch (Exception e) {
                            Log.e("Room", "Error getting course for schedule", e);
                            roomIsSuitable = false;
                            break;
                        }
                    }

                    // If room is suitable, return it
                    if (roomIsSuitable) {
                        try {
                            callback.onObjectRetrieved(room);
                        } catch (ParseException | InvocationTargetException |
                                 NoSuchMethodException | IllegalAccessException |
                                 InstantiationException e) {
                            Log.e("Room", "Error in callback for suitable room", e);
                        }
                        return; // Found a suitable room, exit
                    }
                }

                // If we get here, no suitable room was found
                // IMPORTANT: Call callback with null or handle appropriately
                try {
                    callback.onObjectRetrieved(null); // Or you might want to create an error
                } catch (ParseException | InvocationTargetException |
                         NoSuchMethodException | IllegalAccessException |
                         InstantiationException e) {
                    Log.e("Room", "Error in no-room-found callback", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // IMPORTANT: Call the error callback
                Log.e("Room", "Database error: " + error.getMessage());
                // This depends on your ObjectCallBack implementation
                // If it has onError(DatabaseError), call it:
                callback.onError(error);
                // If not, you might need to handle differently
            }
        });
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
    public Class<RoomRepository> getRepositoryClass() {
        return RoomRepository.class;
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
