package com.example.turgo;

public class CourseType {
    private String courseType;
    private static RTDBManager<CourseType> courseTypeManager;
    private static final String FIREBASE_DB_REFERENCE = "CourseTypes";
    public CourseType(String courseType){
        this.courseType = courseType;
        updateToDB(this);
    }
    public String getCourseType(){
        return courseType;
    }
    public static void updateToDB(CourseType courseType){
        courseTypeManager.storeData(FIREBASE_DB_REFERENCE, courseType.getCourseType(), courseType, "CourseType", "CourseType");
    }
}
