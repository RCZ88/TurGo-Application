package com.example.turgo;

public enum ScheduleQuality {
    PRIVATE_ONLY("PRIVATE_ONLY"),
    GROUP_ONLY("GROUP_ONLY"),
    FLEXIBLE("FLEXIBLE");
    private final String scheduleQuality;
    ScheduleQuality(String type){
        scheduleQuality = type;
    }

    public String getScheduleQuality() {
        return scheduleQuality;
    }
}
