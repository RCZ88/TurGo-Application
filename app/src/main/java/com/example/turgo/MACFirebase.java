package com.example.turgo;

import com.google.firebase.database.DatabaseError;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class MACFirebase extends MailFirebase implements FirebaseClass<Mail>{
    private ArrayList<String> schedules;
    private Course course;
    private String reasonForJoining;
    private String school;
    private String grade;
    public MACFirebase(){

    }

    @Override
    public void importObjectData(Mail from) {
        setMailID(from.getID());
        setFrom(from.getFromId());
        setTo(from.getToId());
        setTimeSent(from.getTimeSent().toString());
        setTimeOpened(from.getTimeOpened() != null ? from.getTimeOpened().toString() : null);
        setHeader(from.getHeader());
        setBody(from.getBody());
        setOpened(from.isOpened());
        MailApplyCourse fromMAC = (MailApplyCourse)from;

        schedules = convertToIdList(fromMAC.getSchedules());
        course = fromMAC.getCourse();
        reasonForJoining = fromMAC.getReasonForJoining();
        school = fromMAC.getSchool();
        grade = fromMAC.getGrade();
    }


    public void convertToNormal(ObjectCallBack<Mail> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(MailApplyCourse.class, getID(), new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((MailApplyCourse) object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
    }

    public ArrayList<String> getSchedules() {
        return schedules;
    }

    public void setSchedules(ArrayList<String> schedules) {
        this.schedules = schedules;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getReasonForJoining() {
        return reasonForJoining;
    }

    public void setReasonForJoining(String reasonForJoining) {
        this.reasonForJoining = reasonForJoining;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
