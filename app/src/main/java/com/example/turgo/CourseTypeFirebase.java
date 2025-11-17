package com.example.turgo;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public class CourseTypeFirebase implements FirebaseClass<CourseType>{
    private String courseType_ID;
    private String courseType;

    @Override
    public void importObjectData(CourseType from) {
        courseType_ID = from.getID();
        courseType = from.getCourseType();
    }

    @Override
    public String getID() {
        return courseType_ID;
    }

    @Override
    public CourseType convertToNormal() throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        return (CourseType) constructClass(CourseType.class, courseType_ID);
    }

    public String getCourseType_ID() {
        return courseType_ID;
    }

    public void setCourseType_ID(String courseType_ID) {
        this.courseType_ID = courseType_ID;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }
}
