package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;

public class MACFirebase extends MailFirebase implements FirebaseClass<Mail>{
    private ArrayList<String> schedulesIDs;
    private Course course;
    private String reasonForJoining;
    private String school;
    private String grade;
    public MACFirebase(){

    }

    @Override
    public void importObjectData(Mail from) {
        setMailID(from.getID());
        setFromId(from.getFrom().getUID());
        setToId(from.getTo().getUID());
        setTimeSent(from.getTimeSent().toString());
        setTimeOpened(from.getTimeOpened().toString());
        setHeader(from.getHeader());
        setBody(from.getBody());
        setOpened(from.isOpened());
        MailApplyCourse fromMAC = (MailApplyCourse)from;

        schedulesIDs = convertToIdList(fromMAC.getSchedules());
        course = fromMAC.getCourse();
        reasonForJoining = fromMAC.getReasonForJoining();
        school = fromMAC.getSchool();
        grade = fromMAC.getGrade();
    }

    @Override
    public MailApplyCourse convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (MailApplyCourse) constructClass(MailApplyCourse.class, getID());
    }

    public ArrayList<String> getSchedulesIDs() {
        return schedulesIDs;
    }

    public void setSchedulesIDs(ArrayList<String> schedulesIDs) {
        this.schedulesIDs = schedulesIDs;
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
