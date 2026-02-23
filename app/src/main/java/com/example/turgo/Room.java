package com.example.turgo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Room implements Serializable, RequireUpdate<Room, RoomFirebase, RoomRepository>{
    private final FirebaseNode fbn = FirebaseNode.ROOM;
    private final Class<RoomFirebase> fbc = RoomFirebase.class;
    private static RTDBManager<RoomFirebase>roomRTDB;
    private String roomId;
    private String roomTag;
    private ArrayList<CourseType>suitableCourseType;
    private boolean used;
    private Meeting currentlyOccupiedBy;
    private int capacity;
    private ArrayList<Schedule>schedules;//schedules that uses this room;


    public Room(String roomTag, int capacity, ArrayList<CourseType>suitableCourseType){
        this.roomId = UUID.randomUUID().toString();
        this.roomTag = roomTag;
        this.capacity = capacity;
        this.suitableCourseType = suitableCourseType;
        this.used = false;
        this.currentlyOccupiedBy = null;
        this.schedules = new ArrayList<>();
    }
    public Room(){}

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public static void assignRoomForSchedule(Room room, Schedule schedule){
        room.schedules.add(schedule);
    }

    public static void useRoom(Room room, Meeting meeting){
        room.setCurrentlyOccupiedBy(meeting);
    }

//    public static Task<List<Room>> getRooms(){
//        TaskCompletionSource<List<Room>> taskSource = new TaskCompletionSource<>();
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(FirebaseNode.ROOM.getPath());
//        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<Task<Room>> tasks = new ArrayList<>();
//                for (DataSnapshot child : snapshot.getChildren()) {
//                    RoomRepository rr = new RoomRepository(child.getKey());
//                    tasks.add(rr.loadAsNormal());
//                }
//                Tasks.whenAllSuccess(tasks)
//                        .addOnSuccessListener(results -> {
//                            ArrayList<Room> rooms = new ArrayList<>();
//                            for (Object result : results) {
//                                if (result instanceof Room) {
//                                    rooms.add((Room) result);
//                                }
//                            }
//                            taskSource.setResult(rooms);
//                        })
//                        .addOnFailureListener(taskSource::setException);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                taskSource.setException(error.toException());
//            }
//        });
//        return taskSource.getTask();
//    }
    public static void getEmptyRoom(LocalTime timeStart, LocalTime timeEnd, DayOfWeek day, ObjectCallBack<Room> callback) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(FirebaseNode.ROOM.getPath());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Room", "getEmptyRoom(): using repository-based room loading (no direct Room.class deserialization)");
                ArrayList<Task<Room>> roomTasks = new ArrayList<>();
                for (DataSnapshot s : snapshot.getChildren()) {
                    String roomId = s.getKey();
                    if (!Tool.boolOf(roomId)) {
                        continue;
                    }
                    RoomRepository rr = new RoomRepository(roomId);
                    roomTasks.add(rr.loadAsNormal());
                }
                if (roomTasks.isEmpty()) {
                    safeReturnRoom(callback, null);
                    return;
                }

                Tasks.whenAllComplete(roomTasks).addOnCompleteListener(task -> {
                    ArrayList<Room> rooms = new ArrayList<>();
                    for (Task<Room> roomTask : roomTasks) {
                        if (roomTask.isSuccessful() && roomTask.getResult() != null) {
                            rooms.add(roomTask.getResult());
                        } else if (roomTask.getException() != null) {
                            Log.w("Room", "Skipping room due to load error", roomTask.getException());
                        }
                    }
                    findSuitableRoomRecursive(rooms, 0, timeStart, timeEnd, day, callback);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Room", "Database error: " + error.getMessage());
                callback.onError(error);
            }
        });
    }

    private interface SuitabilityCallback {
        void onResult(boolean suitable);
    }

    private static void findSuitableRoomRecursive(ArrayList<Room> rooms,
                                                  int roomIndex,
                                                  LocalTime timeStart,
                                                  LocalTime timeEnd,
                                                  DayOfWeek day,
                                                  ObjectCallBack<Room> callback) {
        if (roomIndex >= rooms.size()) {
            safeReturnRoom(callback, null);
            return;
        }

        Room room = rooms.get(roomIndex);
        if (room == null) {
            findSuitableRoomRecursive(rooms, roomIndex + 1, timeStart, timeEnd, day, callback);
            return;
        }

        if (room.getSchedules() == null || room.getSchedules().isEmpty()) {
            safeReturnRoom(callback, room);
            return;
        }

        isRoomSuitableForSchedules(room, 0, timeStart, timeEnd, day, suitable -> {
            if (suitable) {
                safeReturnRoom(callback, room);
            } else {
                findSuitableRoomRecursive(rooms, roomIndex + 1, timeStart, timeEnd, day, callback);
            }
        });
    }

    private static void isRoomSuitableForSchedules(Room room,
                                                   int scheduleIndex,
                                                   LocalTime timeStart,
                                                   LocalTime timeEnd,
                                                   DayOfWeek day,
                                                   SuitabilityCallback callback) {
        ArrayList<Schedule> schedules = room.getSchedules();
        if (schedules == null || scheduleIndex >= schedules.size()) {
            callback.onResult(true);
            return;
        }

        Schedule schedule = schedules.get(scheduleIndex);
        if (schedule == null) {
            isRoomSuitableForSchedules(room, scheduleIndex + 1, timeStart, timeEnd, day, callback);
            return;
        }

        if (Tool.isTimeOccupied(timeStart, timeEnd, day, schedule)) {
            callback.onResult(false);
            return;
        }

        schedule.getScheduleOfCourse()
                .addOnSuccessListener(course -> {
                    if (course == null
                            || room.getSuitableCourseType() == null
                            || !room.getSuitableCourseType().contains(course.getCourseType())) {
                        callback.onResult(false);
                        return;
                    }
                    isRoomSuitableForSchedules(room, scheduleIndex + 1, timeStart, timeEnd, day, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e("Room", "Error getting course for schedule", e);
                    callback.onResult(false);
                });
    }

    private static void safeReturnRoom(ObjectCallBack<Room> callback, Room room) {
        try {
            callback.onObjectRetrieved(room);
        } catch (ParseException | InvocationTargetException | NoSuchMethodException
                 | IllegalAccessException | InstantiationException e) {
            Log.e("Room", "Error in room callback", e);
            callback.onError(DatabaseError.fromException(e));
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomTag() {
        return roomTag;
    }

    public void setRoomTag(String roomTag) {
        this.roomTag = roomTag;
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

    @Override
    public String toString() {
        return "Room{" +
                "schedules=" + schedules +
                ", capacity=" + capacity +
                ", currentlyOccupiedBy=" + currentlyOccupiedBy +
                ", used=" + used +
                ", suitableCourseType=" + suitableCourseType +
                ", roomTag='" + roomTag + '\'' +
                ", roomId='" + roomId + '\'' +
                '}';
    }
}
