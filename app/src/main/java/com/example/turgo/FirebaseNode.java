package com.example.turgo;

import java.io.Serializable;

public enum FirebaseNode implements FirebasePathEnum, Serializable {
    USER("users", User.class),
    STUDENT("users/students", Student.class),
    TEACHER("users/teachers", Teacher.class),
    PARENT("users/parents", Parent.class),
    ADMIN("users/admins", Admin.class),
    USER_ID_ROLES("users/roles", String.class),
    RICH_BODY("rich-body", RichBody.class),
    TEXT_STYLE_RANGE("text-style-range", TextStyleRange.class),
    AGENDA("agendas", Agenda.class),
    ROOM("rooms", Room.class),
    DTA("day-time-arrangement", DayTimeArrangement.class),
    COURSE("courses", Course.class),
    STUDENT_COURSE("student-course", StudentCourse.class),
    COURSE_TYPE("course-types", CourseType.class),
    FILE("files", file.class),
    TIME_SLOT("time-slot", TimeSlot.class),
    SCHEDULE("schedules", Schedule.class),
    MEETING("meetings", Meeting.class),
    SUBMISSION("submissions", Submission.class),
    SUBMISSION_DISPLAY("submission-display", SubmissionDisplay.class),
    TASK("tasks", Task.class),
    DROPBOX("dropboxes", Dropbox.class),
    NOTIFICATION("notifications", Notification.class),
    MAIL("mails", Mail.class),
    MAIL_APPLY_COURSE("mails/apply-course", MailApplyCourse.class),
    BUILT_IN_COURSE_LOGO("course-logo/built-in", String.class),
    BUILT_IN_COURSE_BANNER("course-banner/built-in", String.class),
    UPLOADED_COURSE_LOGO("course-logo/uploaded", String.class),
    UPLOADED_COURSE_BANNER("course-banner/uploaded", String.class),
    COURSE_IMAGES("course-images", String.class),
    USER_STATUS("user-status", UserStatus.class);




    private final String path;
    private final Class<?> clazz;
    FirebaseNode(String path, Class<?> clazz){
        this.path = path;
        this.clazz = clazz;
    }

    public String getPath(){
        return path;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
