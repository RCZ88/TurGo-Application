package com.example.turgo;

public class ApplyCourse extends Mail{
    private Schedule schedule;
    public ApplyCourse(User from, User to, Schedule schedule) {
        super(from, to);
        this.schedule = schedule;
    }
}
