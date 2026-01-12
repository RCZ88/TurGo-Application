package com.example.turgo;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseError;

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
    public void convertToNormal(ObjectCallBack<CourseType> objectCallBack) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        constructClass(CourseType.class, courseType_ID, new ConstructClassCallback() {
            @Override
            public void onSuccess(Object object) throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
                objectCallBack.onObjectRetrieved((CourseType)object);
            }

            @Override
            public void onError(DatabaseError error) {

            }
        });
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
    @NonNull
    public String toString(){
        return courseType;
    }
}
