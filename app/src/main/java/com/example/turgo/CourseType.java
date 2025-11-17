package com.example.turgo;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class CourseType implements RequireUpdate<CourseType, CourseTypeFirebase>{
    private FirebaseNode fbn = FirebaseNode.COURSETYPE;
    private Class<CourseTypeFirebase> fbc = CourseTypeFirebase.class;
    private String courseType_ID;
    private String courseType;
    public CourseType(String courseType) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        courseType_ID = UUID.randomUUID().toString();
        this.courseType = courseType;
        updateDB();
    }
    public CourseType(){

    }
    public String getCourseType(){
        return courseType;
    }

    @Override
    public FirebaseNode getFirebaseNode() {
        return fbn;
    }

    @Override
    public Class<CourseTypeFirebase> getFirebaseClass() {
        return fbc;
    }


    @Override
    public String getID() {
        return courseType_ID;
    }

    @NonNull
    public String toString(){
        return courseType;
    }
}
