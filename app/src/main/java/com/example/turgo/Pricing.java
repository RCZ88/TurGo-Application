package com.example.turgo;

public class Pricing {
    private boolean privateOrGroup;
    private double payment;
    private Course ofCourse;
    private boolean perMeetingOrMonth;
    private Pricing(boolean privateOrGroup, double payment, Course ofCourse, boolean perMeetingOrMonth){
        this.privateOrGroup = privateOrGroup;
        this.payment = payment;
        this.ofCourse = ofCourse;
        this.perMeetingOrMonth = perMeetingOrMonth;
    }
}
