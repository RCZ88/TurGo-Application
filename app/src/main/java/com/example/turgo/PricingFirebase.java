package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class PricingFirebase implements FirebaseClass<Pricing>{
    private String pricing_ID;
    private boolean privateOrGroup;
    private double payment;
    private String ofCourse;       // Reference to Course by ID
    private boolean perMeetingOrMonth;

    @Override
    public void importObjectData(Pricing pricing) {
        this.pricing_ID = pricing.getID();
        this.privateOrGroup = pricing.isPrivateOrGroup();
        this.payment = pricing.getPayment();
        this.ofCourse = (pricing.getOfCourse() != null) ? pricing.getOfCourse().getCourseID() : null;
        this.perMeetingOrMonth = pricing.isPerMeetingOrMonth();
    }

    @Override
    public String getID() {
        return pricing_ID;
    }

    @Override
    public void convertToNormal(ObjectCallBack<Pricing> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(Pricing.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((Pricing) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
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

    public String getOfCourse() {
        return ofCourse;
    }

    public void setOfCourse(String ofCourse) {
        this.ofCourse = ofCourse;
    }

    public boolean isPerMeetingOrMonth() {
        return perMeetingOrMonth;
    }

    public void setPerMeetingOrMonth(boolean perMeetingOrMonth) {
        this.perMeetingOrMonth = perMeetingOrMonth;
    }
}
