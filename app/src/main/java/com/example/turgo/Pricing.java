package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class Pricing implements RequireUpdate<Pricing, PricingFirebase>{
    private final FirebaseNode fbn = FirebaseNode.PRICING;
    private final Class<PricingFirebase> fbc = PricingFirebase.class;
    private String pricing_ID;
    private boolean privateOrGroup;
    private double payment;
    private Course ofCourse;
    private boolean perMeetingOrMonth;
    private Pricing(boolean privateOrGroup, double payment, Course ofCourse, boolean perMeetingOrMonth){
        pricing_ID = UUID.randomUUID().toString();
        this.privateOrGroup = privateOrGroup;
        this.payment = payment;
        this.ofCourse = ofCourse;
        this.perMeetingOrMonth = perMeetingOrMonth;
    }

    public String getPricing_ID() {
        return pricing_ID;
    }

    public void setPricing_ID(String pricing_ID) {
        this.pricing_ID = pricing_ID;
    }

    public boolean isPrivateOrGroup() {
        return privateOrGroup;
    }

    public void setPrivateOrGroup(boolean privateOrGroup) {
        this.privateOrGroup = privateOrGroup;
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }

    public Course getOfCourse() {
        return ofCourse;
    }

    public void setOfCourse(Course ofCourse) {
        this.ofCourse = ofCourse;
    }

    public boolean isPerMeetingOrMonth() {
        return perMeetingOrMonth;
    }

    public void setPerMeetingOrMonth(boolean perMeetingOrMonth) {
        this.perMeetingOrMonth = perMeetingOrMonth;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<PricingFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return pricing_ID;
    }
}
