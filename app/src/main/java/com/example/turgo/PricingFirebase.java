package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class PricingFirebase implements FirebaseClass<Pricing>{
    private String pricing_ID;
    private boolean privateOrGroup;
    private double payment;
    private String ofCourseID;       // Reference to Course by ID
    private boolean perMeetingOrMonth;

    @Override
    public void importObjectData(Pricing pricing) {
        this.pricing_ID = pricing.getID();
        this.privateOrGroup = pricing.isPrivateOrGroup();
        this.payment = pricing.getPayment();
        this.ofCourseID = (pricing.getOfCourse() != null) ? pricing.getOfCourse().getCourseID() : null;
        this.perMeetingOrMonth = pricing.isPerMeetingOrMonth();
    }

    @Override
    public String getID() {
        return pricing_ID;
    }

    @Override
    public Pricing convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (Pricing) constructClass(Pricing.class, pricing_ID);
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

    public String getOfCourseID() {
        return ofCourseID;
    }

    public void setOfCourseID(String ofCourseID) {
        this.ofCourseID = ofCourseID;
    }

    public boolean isPerMeetingOrMonth() {
        return perMeetingOrMonth;
    }

    public void setPerMeetingOrMonth(boolean perMeetingOrMonth) {
        this.perMeetingOrMonth = perMeetingOrMonth;
    }
}
