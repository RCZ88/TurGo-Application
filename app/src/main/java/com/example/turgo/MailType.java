package com.example.turgo;

public enum MailType {
    REQUEST("JoinCourseRequest");

    public String mailType;

    MailType(String mailType) {
        this.mailType = mailType;
    }

    public String getMailType(){
        return mailType;
    }
}
