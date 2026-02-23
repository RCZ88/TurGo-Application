package com.example.turgo;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class CourseType implements Serializable, RequireUpdate<CourseType, CourseTypeFirebase, CourseTypeRepository> {
    public static final FirebaseNode fbn = FirebaseNode.COURSE_TYPE;
    private Class<CourseTypeFirebase> fbc = CourseTypeFirebase.class;
    private String courseType_ID;
    private String courseType;
    public CourseType(String courseType){
        courseType_ID = UUID.randomUUID().toString();
        this.courseType = courseType;
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
    public Class<CourseTypeRepository> getRepositoryClass() {
        return CourseTypeRepository.class;
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

    public static ArrayList<String> getCourseTypeString(ArrayList<CourseType>courseTypes){
        return Tool.streamToArray(courseTypes.stream().map(CourseType::getCourseType));
    }
}
