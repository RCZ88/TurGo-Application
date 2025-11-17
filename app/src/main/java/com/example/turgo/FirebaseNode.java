package com.example.turgo;

public enum FirebaseNode implements FirebasePathEnum{
    STUDENT("users/students"),
    TEACHER("users/teachers"),
    PARENT("users/parents"),
    ADMIN("users/admins"),
    USERIDROLES("users/roles"),
    AGENDA("agendas"),
    ROOM("rooms"),
    DTA("day-time-arrangement"),
    COURSE("courses"),
    STUDENTCOURSE("student-course"),
    COURSETYPE("course-types"),
    PRICING("prices"),
    FILE("files"),
    SCHEDULE("schedules"),
    MEETING("meetings"),
    SUBMISSION("submissions"),
    TASK("tasks"),
    DROPBOX("dropboxes"),
    NOTIFICATION("notifications"),
    MAIL("mails"),
    MAIL_APPLY_COURSE("mails/apply-course"),
    BUILT_IN_COURSE_LOGO("course-logo/built-in"),
    BUILT_IN_COURSE_BANNER("course-banner/built-in"),
    UPLOADED_COURSE_LOGO("course-logo/uploaded"),
    UPLOADED_COURSE_BANNER("course-banner/uploaded"),
    COURSE_IMAGES("course-images"),
    USER_STATUS("user-status");




    private final String path;
    FirebaseNode(String path){
        this.path = path;
    }

    public String getPath(){
        return path;
    }
}
