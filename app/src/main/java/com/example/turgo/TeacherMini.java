package com.example.turgo;

import android.net.Uri;

public class TeacherMini {
    private String teacherName, subjectTeaching, teacherPfp, realTeacherID;

    public TeacherMini(String teacherName, String subjectTeaching, String teacherPfp, String realTeacherID) {
        this.teacherName = teacherName;
        this.subjectTeaching = subjectTeaching;
        this.teacherPfp = teacherPfp;
        this.realTeacherID = realTeacherID;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSubjectTeaching() {
        return subjectTeaching;
    }

    public void setSubjectTeaching(String subjectTeaching) {
        this.subjectTeaching = subjectTeaching;
    }

    public String getTeacherPfp() {
        return teacherPfp;
    }

    public void setTeacherPfp(String teacherPfp) {
        this.teacherPfp = teacherPfp;
    }

    public String getRealTeacherID() {
        return realTeacherID;
    }

    public void setRealTeacherID(String realTeacherID) {
        this.realTeacherID = realTeacherID;
    }

    @Override
    public String toString() {
        return "TeacherMini{" +
                "teacherName='" + teacherName + '\'' +
                ", subjectTeaching='" + subjectTeaching + '\'' +
                ", teacherPfp='" + teacherPfp + '\'' +
                ", realTeacherID='" + realTeacherID + '\'' +
                '}';
    }
}
