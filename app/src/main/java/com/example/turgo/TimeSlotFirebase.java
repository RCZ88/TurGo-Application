package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;

public class TimeSlotFirebase implements FirebaseClass<TimeSlot>{
    /*
    private final String timeSlotId;
    private DayOfWeek day;
    private LocalTime start;
    private LocalTime end;
    private Duration time;
    private ArrayList<Schedule>schedules;
    private int minuteIncrement;
    */

    private String timeSlotId;
    private String day;
    private String start;
    private String end;
    private String time;
    private ArrayList<String> schedules;
    private int minuteIncrement;
    @Override
    public void importObjectData(TimeSlot from) {
        timeSlotId = from.getID();
        day = Tool.boolOf(from.getDay()) ? from.getDay().toString() : "";
        start = Tool.boolOf(from.getStart()) ? from.getStart().toString() : "";
        end = Tool.boolOf(from.getEnd()) ? from.getEnd().toString() : "";
        time = Tool.boolOf(from.getTime()) ? from.getTime().toString() : "";
        schedules = Tool.boolOf(from.getSchedules()) ? convertToIdList(from.getSchedules()) : new ArrayList<>();
        minuteIncrement = from.getMinuteIncrement();
    }
    public TimeSlotFirebase(){}

    @Override
    public String getID() {
        return timeSlotId;
    }

    @Override
    public void convertToNormal(ObjectCallBack<TimeSlot> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(TimeSlot.class, timeSlotId, new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((TimeSlot) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public String getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(String timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ArrayList<String> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<String> schedules) {
        this.schedules = schedules;
    }

    public int getMinuteIncrement() {
        return minuteIncrement;
    }

    public void setMinuteIncrement(int minuteIncrement) {
        this.minuteIncrement = minuteIncrement;
    }
}
