package com.example.turgo;

public enum PageNames {
    TEACHER_DASHBOARD("teacherDashboard"),
    STUDENT_DASHBOARD("studentDashboard");
    private final String pageName;
    PageNames(String pageName){
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }
}
