package com.example.turgo;

import java.util.ArrayList;

public class MeetingToDisplay {
    String day;
    ArrayList<Meeting> schedules;
    public MeetingToDisplay(String day, ArrayList<Meeting> schedules) {
        this.day = day;
        this.schedules = schedules;
    }
}
