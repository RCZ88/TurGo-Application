package com.example.turgo;

import java.util.ArrayList;

public class ScheduleToDisplay {
    String day;
    ArrayList<Schedule> schedules;
    public ScheduleToDisplay(String day, ArrayList<Schedule> schedules) {
        this.day = day;
        this.schedules = schedules;
    }
}
