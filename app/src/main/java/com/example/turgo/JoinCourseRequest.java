package com.example.turgo;

public class JoinCourseRequest {
    private Course ofCourse;
    private boolean approved;
    private TimeSlot timeSlot;
    private Schedule joinedSchedule;
    private boolean isPrivate;
    private int payment;
    private boolean per;

    public JoinCourseRequest(Course ofCourse, boolean approved, TimeSlot timeSlot, Schedule joinedSchedule, boolean isPrivate, int payment, boolean per) {
        this.ofCourse = ofCourse;
        this.approved = approved;
        this.timeSlot = timeSlot;
        this.joinedSchedule = joinedSchedule;
        this.isPrivate = isPrivate;
        this.payment = payment;
        this.per = per;
    }

    public Course getOfCourse() {
        return ofCourse;
    }

    public void setOfCourse(Course ofCourse) {
        this.ofCourse = ofCourse;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Schedule getJoinedSchedule() {
        return joinedSchedule;
    }

    public void setJoinedSchedule(Schedule joinedSchedule) {
        this.joinedSchedule = joinedSchedule;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public int getPayment() {
        return payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    public boolean isPer() {
        return per;
    }

    public void setPer(boolean per) {
        this.per = per;
    }
}
